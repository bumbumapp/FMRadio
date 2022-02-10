package io.bumbumapps.radio.internetradioplayer

import android.app.Application
import com.bumbumapps.radiofm.FactoryRegistry
import com.bumbumapps.radiofm.MemberInjectorRegistry
import com.facebook.stetho.Stetho
import io.bumbumapps.radio.internetradioplayer.data.utils.FileLoggingTree
import io.bumbumapps.radio.internetradioplayer.di.Scopes
import io.bumbumapps.radio.internetradioplayer.di.module.AppModule
import io.bumbumapps.radio.internetradioplayer.extensions.globalErrorHandler
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import toothpick.Toothpick
import toothpick.configuration.Configuration
import toothpick.registries.FactoryRegistryLocator.setRootRegistry
import toothpick.registries.MemberInjectorRegistryLocator.setRootRegistry



@Suppress("unused")
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes())
        } else {
            Toothpick.setConfiguration(Configuration.forProduction().disableReflection())
            setRootRegistry(FactoryRegistry())
            setRootRegistry(MemberInjectorRegistry())
        }

        Scopes.app.installModules(AppModule(this))

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
            Timber.plant(FileLoggingTree.Builder(applicationContext)
                    .log(FileLoggingTree.Logs.ERROR)
                    .build()
            )
        }

        RxJavaPlugins.setErrorHandler(globalErrorHandler)
    }
}
