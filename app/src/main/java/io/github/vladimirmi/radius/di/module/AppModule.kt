package io.github.vladimirmi.radius.di.module

import android.content.Context
import io.github.vladimirmi.radius.model.repository.MediaRepository
import io.github.vladimirmi.radius.model.source.MediaSource
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)
        bind(MediaSource::class.java).singletonInScope()
        bind(MediaRepository::class.java).singletonInScope()
    }
}