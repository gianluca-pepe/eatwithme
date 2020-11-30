package com.brugia.eatwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val NAME = "Nome tavolo"
private const val DESCRIPTION = "Descrizione tavolo"

/**
 * A simple [Fragment] subclass.
 * Use the [TableInfosFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TableInfoFragment : Fragment() {
    private var name: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(NAME)
            description = it.getString(DESCRIPTION)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_info, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param name The name of a table.
         * @param description Short description describing a table.
         * @return A new instance of fragment tableInfos.
         */
        // TODO: Add image param
        @JvmStatic
        fun newInstance(name: String, description: String) =
                TableInfoFragment().apply {
                    arguments = Bundle().apply {
                        putString(NAME, name)
                        putString(DESCRIPTION, description)
                    }
                }
    }
}