package com.example.famsafe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MemberAdapter(
    private val listMembers: List<MemberModel>,
    private val onDistanceClick: (position: Int) -> Unit,
    private val onSmsClick: (position: Int) -> Unit,
    private val onStopsosclick: (position: Int) -> Unit

) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val item = inflater.inflate(R.layout.item_member, parent, false)
        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listMembers[position]
        holder.name.text = item.name
        holder.address.text = item.address
        holder.battery.text = item.battery
        holder.distance.text = item.distance
        holder.emailval.text=item.emailval
        holder.sms
        holder.Stopsos
        // Set click listener for the entire item
        holder.distance.setOnClickListener {
            onDistanceClick(position)
        }
        holder.sms.setOnClickListener {
            onSmsClick(position)
        }

        holder.Stopsos.setOnClickListener{
            onStopsosclick(position)
        }

    }



    override fun getItemCount(): Int {
        return listMembers.size
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val imageUser = item.findViewById<ImageView>(R.id.img_user)
        val name = item.findViewById<TextView>(R.id.name)
        val address = item.findViewById<TextView>(R.id.address)
        val battery = item.findViewById<TextView>(R.id.battery_percent)
        val distance = item.findViewById<TextView>(R.id.distance_value)
        val emailval=item.findViewById<TextView>(R.id.emailval)
        val sms=item.findViewById<ImageView>(R.id.sms)
        val Stopsos=item.findViewById<ImageView>(R.id.Stopsos)
    }
}
