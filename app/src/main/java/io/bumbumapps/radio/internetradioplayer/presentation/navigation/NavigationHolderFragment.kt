package io.bumbumapps.radio.internetradioplayer.presentation.navigation

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Slide
import androidx.transition.TransitionManager
import io.bumbumapps.radio.internetradioplayer.R
import io.bumbumapps.radio.internetradioplayer.extensions.dp
import io.bumbumapps.radio.internetradioplayer.extensions.visible
import io.bumbumapps.radio.internetradioplayer.extensions.waitForLayout
import io.bumbumapps.radio.internetradioplayer.presentation.base.BackPressListener

// Former kotlin-android-extensions synthetic view accessors.
private val android.app.Activity.childrenContainer: android.widget.FrameLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.childrenContainer)
private val androidx.fragment.app.Fragment.childrenContainer: android.widget.FrameLayout get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.childrenContainer)
private val android.app.Dialog.childrenContainer: android.widget.FrameLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.childrenContainer)
private val android.app.Activity.childrenScroll: android.widget.ScrollView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.childrenScroll)
private val androidx.fragment.app.Fragment.childrenScroll: android.widget.ScrollView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.childrenScroll)
private val android.app.Dialog.childrenScroll: android.widget.ScrollView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.childrenScroll)
private val android.app.Activity.dataContainer: android.widget.FrameLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.dataContainer)
private val androidx.fragment.app.Fragment.dataContainer: android.widget.FrameLayout get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.dataContainer)
private val android.app.Dialog.dataContainer: android.widget.FrameLayout get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.dataContainer)
private val android.app.Activity.dataScroll: android.widget.ScrollView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.dataScroll)
private val androidx.fragment.app.Fragment.dataScroll: android.widget.ScrollView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.dataScroll)
private val android.app.Dialog.dataScroll: android.widget.ScrollView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.dataScroll)
private val android.app.Activity.parentBt: com.google.android.material.button.MaterialButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.parentBt)
private val androidx.fragment.app.Fragment.parentBt: com.google.android.material.button.MaterialButton get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.parentBt)
private val android.app.Dialog.parentBt: com.google.android.material.button.MaterialButton get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.parentBt)
private val android.app.Activity.parentTitleTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.parentTitleTv)
private val androidx.fragment.app.Fragment.parentTitleTv: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.parentTitleTv)
private val android.app.Dialog.parentTitleTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.parentTitleTv)
private val android.app.Activity.screenTitleTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.screenTitleTv)
private val androidx.fragment.app.Fragment.screenTitleTv: android.widget.TextView get() = requireView().findViewById(io.bumbumapps.radio.internetradioplayer.R.id.screenTitleTv)
private val android.app.Dialog.screenTitleTv: android.widget.TextView get() = findViewById(io.bumbumapps.radio.internetradioplayer.R.id.screenTitleTv)

/**
 * Created by Vladimir Mikhalev 21.03.2019.
 */

abstract class NavigationHolderFragment : Fragment(), BackPressListener {

    private lateinit var currentScreenContext: ScreenContext

    abstract fun getFirstScreen(): ScreenContext

    abstract fun onScreenChange(screen: ScreenContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentScreenContext = getFirstScreen()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_navigation_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navigateTo(currentScreenContext, animate = false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdded) childFragmentManager.fragments.forEach { it.userVisibleHint = isVisibleToUser }
    }

    override fun handleBackPressed(): Boolean {
        return childFragmentManager.fragments.any { it is BackPressListener && it.handleBackPressed() }
                || backTo(currentScreenContext.parent)
    }

    private fun navigateTo(screenContext: ScreenContext, animate: Boolean) {
        setupNavigation(screenContext, animate, isForward = true)
        val fragment = screenContext.createFragment() ?: return
        fragment.userVisibleHint = userVisibleHint
        childFragmentManager.beginTransaction()
                .replace(R.id.dataContainer, fragment)
                .addToBackStack(null)
                .commit()

    }

    private fun backTo(screenContext: ScreenContext?): Boolean {
        if (screenContext == null) return false
        setupNavigation(screenContext, animate = true, isForward = false)
        childFragmentManager.popBackStack()
        return true
    }

    private fun setupNavigation(screenContext: ScreenContext, animate: Boolean, isForward: Boolean) {
        dataScroll.visible(screenContext is DataScreen || screenContext is FragmentScreen)
        setupHome(screenContext)
        setupParent(screenContext, animate, isForward)
        setupChildren(screenContext, animate, isForward)
        currentScreenContext = screenContext
        onScreenChange(screenContext)
    }

    /** Bento home: on the root screen show the hero + tiles instead of the plain child list. */
    private fun setupHome(screenContext: ScreenContext) {
        val root = requireView()
        val isHome = screenContext.parent == null && screenContext.children.size >= 2
        root.findViewById<View>(R.id.searchHome).visible(isHome)
        root.findViewById<View>(R.id.navigationContainer).visible(!isHome)
        childrenScroll.visible(!isHome)
        if (!isHome) return
        val manual = screenContext.children[0]
        val genres = screenContext.children[1]
        root.findViewById<View>(R.id.heroGenres).setOnClickListener { navigateTo(genres, animate = true) }
        root.findViewById<View>(R.id.tileGenres).setOnClickListener { navigateTo(genres, animate = true) }
        root.findViewById<View>(R.id.tileManual).setOnClickListener { navigateTo(manual, animate = true) }
        val featured = io.bumbumapps.radio.internetradioplayer.presentation.search.HomeGenres
                .pick(genres.children.map { it.title })
        val tiles = listOf(R.id.genreTile0, R.id.genreTile1, R.id.genreTile2, R.id.genreTile3, R.id.genreTile4, R.id.genreTile5)
        val labels = listOf(R.id.genreTile0Tv, R.id.genreTile1Tv, R.id.genreTile2Tv, R.id.genreTile3Tv, R.id.genreTile4Tv, R.id.genreTile5Tv)
        tiles.forEachIndexed { i, id ->
            val tile = root.findViewById<View>(id)
            val title = featured.getOrNull(i)
            val target = genres.children.firstOrNull { it.title == title }
            tile.visible(target != null, gone = false)
            if (target != null) {
                root.findViewById<android.widget.TextView>(labels[i]).text = title
                tile.setOnClickListener { navigateTo(target, animate = true) }
            }
        }
    }

    private fun setupParent(screenContext: ScreenContext, animate: Boolean, isForward: Boolean) {
        parentBt.setOnClickListener { backTo(screenContext.parent) }
        parentBt.visible(screenContext.parent != null, gone = false)
        animateParent(animate, isForward) {
            parentTitleTv.visible(screenContext.parent != null, gone = false)
            parentTitleTv.text = screenContext.parent?.title
            screenTitleTv.text = screenContext.title
        }
    }

    private fun setupChildren(screenContext: ScreenContext, animate: Boolean, isForward: Boolean) {
        val inflater = LayoutInflater.from(requireContext())
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }
        screenContext.children.forEach { child ->
            val view = child.createSmallView(inflater, childrenContainer)
            view.setOnClickListener { navigateTo(child, animate = true) }
            container.addView(view)
        }
        screenContext.parent?.position = childrenScroll.scrollY
        animateChildren(container, animate, isForward)
        childrenContainer.removeAllViews()
        childrenContainer.addView(container)
        if (isForward) {
            screenContext.position = 0
        } else {
            childrenContainer.waitForLayout {
                childrenScroll.scrollTo(0, screenContext.position); true
            }
        }
    }

    private fun animateChildren(target: View, animate: Boolean, isForward: Boolean) {
        if (!animate) return
        val slideIn = Slide()
                .apply { slideEdge = if (isForward) Gravity.END else Gravity.START }
                .setInterpolator(FastOutSlowInInterpolator())
        slideIn.addTarget(target)

        TransitionManager.beginDelayedTransition(childrenContainer, slideIn)
    }

    private fun animateParent(animate: Boolean, isForward: Boolean, onEnd: () -> Unit) {
        onEnd()
        if (!animate) return
        listOf(parentTitleTv, screenTitleTv).forEach {
            it.alpha = 0f
            it.translationX = (if (isForward) 16f else -16f) * requireContext().dp
            it.animate().alpha(1f).translationX(0f).setInterpolator(FastOutSlowInInterpolator()).start()
        }
    }
}