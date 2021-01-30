package com.brugia.eatwithme.createtable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.brugia.eatwithme.R
import com.brugia.eatwithme.createtable.pages.CreateTableDateFragment
import com.brugia.eatwithme.createtable.pages.CreateTableMapFragment
import com.brugia.eatwithme.createtable.pages.CreateTableNameFragment
import com.brugia.eatwithme.createtable.pages.CreateTableConfirmationFragment
import com.brugia.eatwithme.tablelist.SelectedTableViewModel
import com.brugia.eatwithme.tablelist.SelectedTableViewModelFactory
import kotlinx.coroutines.selects.select


class CreateTablePagerFragment : Fragment() {
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var viewPager: ViewPager2
    private lateinit var callback:OnBackPressedCallback
    private val newTableViewModel by activityViewModels<CreateTableViewModel>()
    private val selectedTableViewModel by activityViewModels<SelectedTableViewModel> {
        SelectedTableViewModelFactory(this.requireContext())
    }
    private lateinit var stepper: Stepper
    private lateinit var stepperView: View
    private lateinit var nextBackButtonsView: View
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            goPreviousPage()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.create_table_pager, container, false)

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = view.findViewById(R.id.create_table_pager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.isUserInputEnabled = false

        stepper = Stepper(
                view,
                listOf(
                        mapOf(
                                "text" to R.string.create_table_step1,
                                "button" to R.id.radioButton1,
                        ),
                        mapOf(
                                "text" to R.string.create_table_step2,
                                "button" to R.id.radioButton2,
                        ),
                        mapOf(
                                "text" to R.string.create_table_step3,
                                "button" to R.id.radioButton3,
                        ),
                ),
                view.findViewById(R.id.stepBar)
        )

        nextButton = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener { goNextPage() }
        previousButton = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener { goPreviousPage() }

        nextBackButtonsView = view.findViewById(R.id.nextBackButtonsContainer)
        stepperView = view.findViewById(R.id.stepper)
        return view
    }

    private inner class ScreenSlidePagerAdapter(f: Fragment) : FragmentStateAdapter(f) {
        private val pages = listOf(
                CreateTableNameFragment(),
                CreateTableDateFragment(),
                CreateTableMapFragment(),
                CreateTableConfirmationFragment(),
        )

        override fun getItemCount(): Int = pages.size

        override fun createFragment(position: Int): Fragment {
            val page = pages[position]

            page.onNextClicked = ::goNextPage
            page.onPreviousClicked = ::goPreviousPage

            return page
        }
    }

    fun goNextPage() {
        val formPage = childFragmentManager.findFragmentByTag("f" + viewPager.currentItem) as FormPage
        if (formPage.isValid()) {
            if (viewPager.currentItem +2 == viewPager.adapter?.itemCount) {
                newTableViewModel.createTable()
                selectedTableViewModel.setSelectedTable(newTableViewModel.table.value)
            }

            if (viewPager.currentItem + 1 == viewPager.adapter?.itemCount) {
                //findNavController().navigate(R.id.tableLobbyFragment)
                findNavController().navigate(R.id.tableInfoFragment)
                return
            }

            viewPager.currentItem = viewPager.currentItem + 1
            stepper.completeStep()
            buildSurroundingUI()
        }
    }

    fun goPreviousPage() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            callback.remove()
            requireActivity().onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
            stepper.stepBack()
            buildSurroundingUI()
        }
    }

    private fun buildSurroundingUI() {
        /*
            If the page we moved on is the page for restaurants, hide next/back buttons
         */
        val actualPage = childFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
        when (actualPage) {
            is CreateTableMapFragment -> {
                nextBackButtonsView.visibility = View.GONE
            }
            is CreateTableConfirmationFragment -> {
                stepperView.visibility = View.GONE
                nextBackButtonsView.visibility = View.GONE
            }
            else -> {
                stepperView.visibility = View.VISIBLE
                nextBackButtonsView.visibility = View.VISIBLE
            }
        }
    }
}
