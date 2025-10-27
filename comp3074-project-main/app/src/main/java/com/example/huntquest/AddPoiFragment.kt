package com.example.huntquest

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddPoiFragment : Fragment() {

    private val vm: HomeViewModel by activityViewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    private lateinit var placesClient: PlacesClient
    private lateinit var etAddress: MaterialAutoCompleteTextView
    private lateinit var etTask: EditText // new

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var predictions: List<AutocompletePrediction> = emptyList()
    private var queryJob: Job? = null
    private val sessionToken by lazy { AutocompleteSessionToken.newInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_add_poi, container, false)

        val etName: EditText = v.findViewById(R.id.etName)
        etAddress = v.findViewById(R.id.etAddress)
        etTask = v.findViewById(R.id.etTask) // new field in layout
        val ratingBar: RatingBar = v.findViewById(R.id.ratingBar)
        val chipGroup: ChipGroup = v.findViewById(R.id.chipGroupTags)
        val btnSave: MaterialButton = v.findViewById(R.id.btnSave)
        val btnCancel: MaterialButton = v.findViewById(R.id.btnCancel)

        ratingBar.rating = 0f

        // Initialize Places API
        if (!Places.isInitialized()) {
            val appInfo = requireContext().packageManager.getApplicationInfo(
                requireContext().packageName,
                PackageManager.GET_META_DATA
            )
            val apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY") ?: ""
            Places.initialize(requireContext().applicationContext, apiKey)
        }
        placesClient = Places.createClient(requireContext())

        // Autocomplete search
        etAddress.addTextChangedListener { text ->
            val query = text?.toString()?.trim().orEmpty()
            selectedLatitude = null
            selectedLongitude = null

            queryJob?.cancel()
            if (query.isEmpty()) {
                (etAddress as? android.widget.AutoCompleteTextView)?.setAdapter(null)
                return@addTextChangedListener
            }

            queryJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(250)
                val request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(sessionToken)
                    .setQuery(query)
                    .build()

                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        predictions = response.autocompletePredictions
                        val items = predictions.map { it.getFullText(null).toString() }
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            items
                        )
                        (etAddress as android.widget.AutoCompleteTextView).setAdapter(adapter)
                        (etAddress as android.widget.AutoCompleteTextView).showDropDown()
                    }
            }
        }

        (etAddress as android.widget.AutoCompleteTextView).setOnItemClickListener { _, _, position, _ ->
            val prediction = predictions.getOrNull(position) ?: return@setOnItemClickListener
            val placeId = prediction.placeId
            val req = FetchPlaceRequest.builder(
                placeId,
                listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)
            ).setSessionToken(sessionToken).build()

            placesClient.fetchPlace(req)
                .addOnSuccessListener { resp ->
                    val place = resp.place
                    etAddress.setText(place.address ?: prediction.getFullText(null).toString())
                    selectedLatitude = place.latLng?.latitude
                    selectedLongitude = place.latLng?.longitude
                }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim().ifEmpty { null }
            val rating = ratingBar.rating
            val task = etTask.text.toString().trim().ifEmpty { null } // new field

            val selectedTags = mutableListOf<String>()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.isChecked) selectedTags += chip.text.toString()
            }
            val tagsCsv = selectedTags.joinToString(", ")

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vm.addPoi(
                name = name,
                rating = rating,
                address = address,
                tagCsv = tagsCsv,
                task = task // add description
            )

            Snackbar.make(v, "POI saved successfully", Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        return v
    }
}
