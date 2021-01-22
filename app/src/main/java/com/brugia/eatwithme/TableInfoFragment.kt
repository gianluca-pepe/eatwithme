package com.brugia.eatwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory

class TableInfoFragment : Fragment() {
    private val tableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }
    private lateinit var nameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var tableImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextView = view.findViewById(R.id.table_name)
        descriptionTextView = view.findViewById(R.id.table_description)
        tableImageView = view.findViewById(R.id.table_image)

        tableViewModel.getSelectedTable().observe(viewLifecycleOwner, {
            it?.let {
                nameTextView.text = it.name
                descriptionTextView.text = it.description
                // set image
                val hours = it.tableHourText()
                //Check the hour and set the image according it
                if( hours >= "05:00" && hours < "11:30" ){
                    tableImageView.setImageResource(R.drawable.colazione)
                }else if( hours >= "11:30" && hours < "15:00" ){
                    tableImageView.setImageResource(R.drawable.pranzo)
                }else if( hours >= "19:00" && hours < "22:30" ){
                    tableImageView.setImageResource(R.drawable.cena)
                }else{
                    tableImageView.setImageResource(R.drawable.cocktail)//in every other hours, just a cocktail..
                }
            }
        })
    }
}