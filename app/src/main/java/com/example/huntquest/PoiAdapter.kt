package com.example.huntquest

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PoiAdapter(
    private val items: MutableList<Poi>,
    private val onEdit: (Poi, Int) -> Unit,
    private val onRemove: (Poi, Int) -> Unit,
    private val onItemClick: (Poi) -> Unit   // Added click listener for the whole item
) : RecyclerView.Adapter<PoiAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvDistance: TextView = v.findViewById(R.id.tvDistance)
        val tvHours: TextView = v.findViewById(R.id.tvHours)
        val btnEdit: View = v.findViewById(R.id.btnEdit)
        val btnRemove: View = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poi, parent, false)
        return VH(v)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(h: VH, position: Int) {
        val poi = items[position]
        h.tvName.text = poi.name
        h.tvDistance.text = String.format("%.1f km", poi.distanceKm)
        h.tvHours.text = poi.hours

        // Edit / Remove buttons
        h.btnEdit.setOnClickListener { onEdit(poi, position) }
        h.btnRemove.setOnClickListener { onRemove(poi, position) }

        // Click anywhere on the item opens details
        h.itemView.setOnClickListener { onItemClick(poi) }
    }

    override fun getItemCount(): Int = items.size
}
