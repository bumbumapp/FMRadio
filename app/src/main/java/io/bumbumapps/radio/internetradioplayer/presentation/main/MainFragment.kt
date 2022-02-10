package io.bumbumapps.radio.internetradioplayer.presentation.main

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.data.preference.Preferences
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_main.*
import toothpick.Toothpick


/**
 * Created by Vladimir Mikhalev 23.10.2017.
 */

class MainFragment : BaseFragment<MainPresenter, MainView>(), MainView {

    override val layout = R.layout.fragment_main

    companion object {
        fun newInstance(page: Int): MainFragment {
            return MainFragment().apply {
                arguments = Bundle().apply { putInt(Preferences.KEY_MAIN_PAGE_ID, page) }
            }
        }
    }

    override fun providePresenter(): MainPresenter {
        return Toothpick.openScopes(Scopes.ROOT_ACTIVITY, this)
                .getInstance(MainPresenter::class.java).also {
                    Toothpick.closeScope(this)
                }
    }

    override fun setupView(view: View) {
        mainPager.adapter = MainPagerAdapter(requireContext(), childFragmentManager)
        mainPager.offscreenPageLimit = 2
        mainTl.setupWithViewPager(mainPager)
        val pageId = arguments?.getInt(Preferences.KEY_MAIN_PAGE_ID) ?: 0
        setPageId(pageId)

        mainPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                presenter.selectPage(position)
            }
        })
    }

    //region =============== MainView ==============

    override fun setPageId(pageId: Int) {
        arguments = Bundle().apply { putInt(Preferences.KEY_MAIN_PAGE_ID, pageId) }
        val page = when (pageId) {
            R.id.nav_search -> PAGE_SEARCH
            R.id.nav_favorites -> PAGE_FAVORITES
            else -> PAGE_HISTORY
        }
        mainPager.setCurrentItem(page, false)
    }

    //endregion
}
