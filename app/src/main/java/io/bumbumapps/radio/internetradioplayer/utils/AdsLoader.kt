package io.bumbumapps.radio.internetradioplayer.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.bumbumapps.radio.internetradioplayer.utils.Globals.TIMER_FINISHED

object AdsLoader {
    private var mInterstitialAd: InterstitialAd? = null
    private var TAG = "TAG"
    fun displayInterstitial(context: Context) {
         mInterstitialAd=null
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context,"ca-app-pub-8444865753152507/6654387151", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd?) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

    }
    fun showAds(context: Context, unit: Unit?) {
        if (TIMER_FINISHED){
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(context as Activity)

                mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        TIMER_FINISHED=false
                        Timers.timer().start()
                        displayInterstitial(context)
                        if (unit!=null)
                            return unit
                    }
                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        Log.d(TAG, "Ad failed to show.")
                    }
                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Ad showed fullscreen content.")
                        mInterstitialAd = null
                    }
                }

            } else {
                if (unit!=null)
                return unit
            }
        }else
        {
            if (unit!=null)
                return unit
        }

    }
}