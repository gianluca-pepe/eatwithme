package com.brugia.eatwithme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    lateinit var seek: SeekBar
    lateinit var txtkm: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* SeekBar management*/
        seek = view.findViewById<SeekBar>(R.id.seekBar)
        txtkm = view.findViewById<TextView>(R.id.txtchilometri)


        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                //Toast.makeText(getActivity(), "Progress is: " +  seek.progress+"/"+seek.max, Toast.LENGTH_SHORT).show()
                txtkm.text = seek.progress.toString() + " Km"
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
              //Toast.makeText(getActivity(),  "Progress is: " + seek.progress + "%", Toast.LENGTH_SHORT).show()
            }
        })
        /* End SeekBar management*/

    }

}