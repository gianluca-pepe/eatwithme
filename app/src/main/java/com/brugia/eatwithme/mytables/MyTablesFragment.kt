/*
* https://developer.android.com/guide/navigation/navigation-swipe-view-2
*/
package com.brugia.eatwithme.mytables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.brugia.eatwithme.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MyTablesFragment : Fragment() {

    // tab titles
    private val titles = arrayOf("Next tables", "Past tables")

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var tabTablesCollectionAdapter: TabTablesCollectionAdapter
    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_tables, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabTablesCollectionAdapter = TabTablesCollectionAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = tabTablesCollectionAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }


}

class TabTablesCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    lateinit var fragment: Fragment

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        //println("position: $position")
        // Return a NEW fragment instance in createFragment(int) according to the position
        fragment = if(position == 1) {
            PastTables()
        }else{
            NextTables()
        }
        return fragment
    }
}
