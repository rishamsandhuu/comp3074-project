package com.example.huntquest

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RatingBar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.huntquest.ui.home.HomeViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class EditPoiFragment : Fragment() {

    private val vm: HomeViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    private var poiId: Long = -1

    private lateinit var etName: EditText
    private lateinit var etAddress: MaterialAutoCompleteTextView
    private lateinit var ratingBar: RatingBar
    private lateinit var chipGroup: ChipGroup
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private lateinit var placesClient: PlacesClient
    private val sessionToken by lazy { AutocompleteSessionToken.newInstance() }
    private lateinit var adapter: ArrayAdapter<String>
    private var predictions: List<AutocompletePrediction> = emptyList()

    private var selectedLat: Double? = null
    private var selectedLon: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        poiId = requireArguments().getLong("poiId", -1L)

        if (!Places.isInitialized()) {
            val appInfo = requireContext().packageManager.getApplicationInfo(
                requireContext().packageName,
                PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
            Places.initialize(requireContext().applicationContext, apiKey)
        }
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_edit_poi, container, false)

        etName = v.findViewById(R.id.etName)
        etAddress = v.findViewById(R.id.etAddress)
        ratingBar = v.findViewById(R.id.ratingBar)
        chipGroup = v.findViewById(R.id.chipGroupTags)
        btnSave = v.findViewById(R.id.btnSave)
        btnCancel = v.findViewById(R.id.btnCancel)

        // Prefill current values from DB
        vm.observePoi(poiId).observe(viewLifecycleOwner) { poi ->
            poi ?: return@observe
            etName.setText(poi.name)
            etAddress.setText(poi.address.orEmpty())
            ratingBar.rating = poi.rating
            selectedLat = poi.latitude
            selectedLon = poi.longitude

            val selected = poi.tagsCsv
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()

            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                chip.isChecked = chip.text.toString() in selected
            }
        }

        setupAddressAutocomplete()
        setupButtons(v)

        return v
    }

    private fun setupAddressAutocomplete() {
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        etAddress.setAdapter(adapter)

        etAddress.addTextChangedListener { text ->
            val query = text?.toString()?.trim().orEmpty()
            // user is typing something new -> clear previous lat/lon until we confirm a place
            selectedLat = null
            selectedLon = null

            if (query.isEmpty()) {
                adapter.clear()
                return@addTextChangedListener
            }

            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    predictions = response.autocompletePredictions
                    adapter.clear()
                    adapter.addAll(predictions.map { it.getFullText(null).toString() })
                    adapter.notifyDataSetChanged()
                    etAddress.showDropDown()
                }
        }

        etAddress.setOnItemClickListener { _, _, position, _ ->
            val pred = predictions.getOrNull(position) ?: return@setOnItemClickListener

            val fetch = FetchPlaceRequest.builder(
                pred.placeId,
                listOf(
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
                )
            ).setSessionToken(sessionToken).build()

            placesClient.fetchPlace(fetch)
                .addOnSuccessListener { resp ->
                    val place = resp.place
                    etAddress.setText(place.address ?: pred.getFullText(null).toString())
                    selectedLat = place.latLng?.latitude
                    selectedLon = place.latLng?.longitude
                }
                .addOnFailureListener {
                    Snackbar.make(etAddress, "Failed to fetch place details", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupButtons(root: View) {
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim().ifEmpty { null }
            val rating = ratingBar.rating

            val tagsCsv = buildList {
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    if (chip.isChecked) add(chip.text.toString())
                }
            }.joinToString(",")

            if (poiId <= 0L) {
                Snackbar.make(root, "Oops: invalid POI id", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (name.isEmpty()) {
                Snackbar.make(root, "Please enter a name", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            vm.updatePoi(
                poiId = poiId,
                name = name,
                rating = rating,
                address = address,
                tagCsv = tagsCsv,
                latitude = selectedLat,        // may be null if user didn't change address
                longitude = selectedLon
            )

            Snackbar.make(root, "POI updated", Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        btnCancel.setOnClickListener { findNavController().popBackStack() }
    }
}
