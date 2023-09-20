package com.example.famsafe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.famsafe.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var batteryPercentageListener: BatteryPercentageListener
    private var listMembers = mutableListOf<MemberModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mContext = requireContext()

        listMembers = mutableListOf(
            MemberModel(
                "Lokesh",
                "9th building, 2nd floor, Maldiv Road, Manali",
                "", // Initialize the battery percentage field as empty
                "220"
            ),

            // Add other members here...
        )

        val adapter = MemberAdapter(listMembers)

        binding.recyclerMember.layoutManager = LinearLayoutManager(mContext)
        binding.recyclerMember.adapter = adapter

        // Create and observe the battery percentage
        batteryPercentageListener = BatteryPercentageListener(mContext)

        batteryPercentageListener.observe(viewLifecycleOwner, Observer { batteryPercentage ->
            // Create a new list with updated battery percentage
            val updatedListMembers = listMembers.map { member ->
                MemberModel(
                    member.name,
                    member.address,
                    "$batteryPercentage%",
                    member.distance // Replace with the actual field name
                )
            }

            // Update the reference to the new list
            listMembers.clear()
            listMembers.addAll(updatedListMembers)

            // Notify the adapter that the data has changed
            adapter.notifyDataSetChanged()
        })
    }
}
