package io.bumbumapps.radio.internetradioplayer.presentation.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Created by Vladimir Mikhalev 27.03.2019.
 */

abstract class BaseCustomView<P : BasePresenter<V>, V : BaseView>
@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), LifecycleObserver, BaseView {

    protected lateinit var presenter: P

    protected abstract val layout: Int

    protected abstract fun providePresenter(): P

    protected abstract fun setupView()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        View.inflate(context, layout, this)
        presenter = providePresenter()
        setupView()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        @Suppress("UNCHECKED_CAST")
        presenter.attachView(this as V)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        presenter.detachView()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        presenter.destroy()
    }

    //region =============== BaseView =============s=

    override fun handleBackPressed(): Boolean {
        return false
    }

    override fun showToast(resId: Int) {

    }

    override fun showSnackbar(resId: Int) {

    }

    //endregion

    protected val isPresenterInit: Boolean get() = ::presenter.isInitialized
}