package com.weternityreadymedia.eternityready.eternityreadytv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.weternityreadymedia.eternityready.eternityreadytv.viewmodel.PresenterViewModel
import com.weternityreadymedia.eternityready.eternityreadytv.views.home.CategoriesFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.schedule.TvSchedulingFragment
import com.weternityreadymedia.eternityready.eternityreadytv.views.search.SearchActivity

class MainFragment : Fragment() {
    companion object {
        val TAG: String = MainFragment::class.java.name
    }

    private lateinit var viewModel: PresenterViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory())[PresenterViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, TvSchedulingFragment(), TvSchedulingFragment.TAG)
            .commitNow()
        initializeButtons(view)
    }

    private fun initializeButtons(view: View) {
        view.findViewById<Button>(R.id.search).setOnClickListener {
            val intent = Intent(requireActivity(), SearchActivity::class.java).apply {
                putExtra(SearchActivity.SEARCHABLE_LIST, viewModel.liveData.value?.channels?.toTypedArray())
            }
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.on_demand).setOnClickListener {  }

        view.findViewById<Button>(R.id.categories_live).apply {
            setOnClickListener {
                text = if (text.contentEquals("Categories", ignoreCase = true)) {
                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, CategoriesFragment(), CategoriesFragment.TAG)
                        .commit()
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.live_tv_24, 0, 0, 0)
                    getText(R.string.live_tv)
                } else {
                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, TvSchedulingFragment(), TvSchedulingFragment.TAG)
                        .commit()
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.space_dashboard, 0, 0, 0)
                    getText(R.string.categories)
                }
            }
            requestFocus()
        }
    }
}