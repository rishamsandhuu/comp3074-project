package com.example.huntquest.ui.home

import android.annotation.SuppressLint
import android.media.Rating
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.huntquest.R
import java.util.Locale

class PoiAdapter(
    private val items: MutableList<HomePoi>,
    private val onEdit: (HomePoi, Int) -> Unit,
    private val onRemove: (HomePoi, Int) -> Unit,
    private val onItemClick: (HomePoi) -> Unit
) : RecyclerView.Adapter<PoiAdapter.VH>() {

    private val visible = items.toMutableList()

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvDistance: TextView = v.findViewById(R.id.tvDistance)
        val tvHours: TextView = v.findViewById(R.id.tvHours)

        val tvRating: TextView = v.findViewById(R.id.tvRating)
        val ivStar: View = v.findViewById(R.id.ivStar)


        val btnEdit: View = v.findViewById(R.id.btnEdit)
        val btnRemove: View = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_poi, parent, false)
        return VH(v)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(h: VH, position: Int) {
        val poi = visible[position]
        h.tvName.text = poi.name
        h.tvDistance.text = if (poi.distanceKm > 0) String.format("%.1f km", poi.distanceKm) else ""
        h.tvHours.text = poi.hours

        h.tvRating.text = String.format("%.1f", poi.rating)

        h.btnEdit.setOnClickListener { onEdit(poi, position) }
        h.btnRemove.setOnClickListener { onRemove(poi, position) }
        h.itemView.setOnClickListener { onItemClick(poi) }
    }

    override fun getItemCount(): Int = visible.size

    fun submitList(newItems: List<HomePoi>) {
        items.clear()
        items.addAll(newItems)
        filter("") // reset
    }

    fun removeAt(adapterPos: Int) {
        val poi = visible.getOrNull(adapterPos) ?: return
        items.remove(poi)
        visible.removeAt(adapterPos)
        notifyItemRemoved(adapterPos)
    }

    fun filter(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        visible.clear()
        if (q.isEmpty()) {
            visible.addAll(items)
        } else {
            visible.addAll(
                items.filter { poi ->
                    poi.name.lowercase(Locale.getDefault()).contains(q) ||
                            poi.hours.lowercase(Locale.getDefault()).contains(q)
                }
            )
        }
        notifyDataSetChanged()
    }
}
