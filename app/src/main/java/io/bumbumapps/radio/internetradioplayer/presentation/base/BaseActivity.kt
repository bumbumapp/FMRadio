package io.bumbumapps.radio.internetradioplayer.presentation.base

import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Vladimir Mikhalev 11.11.2018.
 */

abstract class BaseActivity<P : BasePresenter<V>, V : BaseView>
    : AppCompatActivity(), BaseView {

    lateinit var presenter: P
    protected val activityView: View by lazy {
        findViewById<View>(android.R.id.content)
    }

    protected abstract val layout: Int

    protected abstract fun providePresenter(): P

    protected abstract fun setupView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        presenter = providePresenter()
        setupView()
    }

    override fun onStart() {
        super.onStart()
        @Suppress("UNCHECKED_CAST")
        presenter.attachView(this as V)
    }

    override fun onStop() {
        presenter.detachView()
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) {
            presenter.destroy()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (handleBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun handleBackPressed(): Boolean {
        return supportFragmentManager.fragments.any { it is BackPressListener && it.handleBackPressed() }
    }

    override fun showToast(resId: Int) {

    }

    override fun showSnackbar(resId: Int) {

    }

    protected val isPresenterInitialized get() = this::presenter.isInitialized
}
