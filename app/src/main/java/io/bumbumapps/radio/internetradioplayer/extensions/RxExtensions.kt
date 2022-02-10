package io.bumbumapps.radio.internetradioplayer.extensions

import io.bumbumapps.radio.internetradioplayer.utils.MessageException
import io.bumbumapps.radio.internetradioplayer.utils.MessageResException
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import timber.log.Timber
import java.io.IOException
import java.net.SocketException

/**
 * Created by Vladimir Mikhalev 14.11.2017.
 */

val errorHandler: (Throwable) -> Unit = {
    runOnUiThread {
        if (it is MessageResException) {

        } else {
            if (it !is MessageException) Timber.e(it)

        }
    }
}

val globalErrorHandler: (Throwable) -> Unit = {
    if (it is UndeliverableException) {
        val e = it.cause

        if ((e is IOException) || (e is SocketException)  // fine, irrelevant network problem or API that throws on cancellation
                || e is InterruptedException) { // fine, some blocking code was interrupted by a dispose call)
        } else Timber.e(e, "Undeliverable exception received, not sure what to do")
    }
}

fun <T : Any> Single<T>.subscribeX(
        onError: (Throwable) -> Unit = errorHandler,
        onSuccess: (T) -> Unit = {}
): Disposable = subscribe(onSuccess, onError)

fun Completable.subscribeX(
        onError: (Throwable) -> Unit = errorHandler,
        onComplete: () -> Unit = {}
): Disposable = subscribe(onComplete, onError)

fun <T : Any> Observable<T>.subscribeX(
        onError: (Throwable) -> Unit = errorHandler,
        onComplete: () -> Unit = {},
        onNext: (T) -> Unit = {}
): Disposable = subscribe(onNext, onError, onComplete)

fun <T : Any> Maybe<T>.subscribeX(
        onError: (Throwable) -> Unit = errorHandler,
        onComplete: () -> Unit = {},
        onNext: (T) -> Unit = {}
): Disposable = subscribe(onNext, onError, onComplete)

