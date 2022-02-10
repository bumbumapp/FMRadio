package io.bumbumapps.radio.internetradioplayer.di.module

import android.content.Context
import android.media.MediaMetadataRetriever
import io.bumbumapps.radio.internetradioplayer.data.db.EqualizerDatabase
import io.bumbumapps.radio.internetradioplayer.data.db.HistoryDatabase
import io.bumbumapps.radio.internetradioplayer.data.db.StationsDatabase
import io.bumbumapps.radio.internetradioplayer.data.db.SuggestionsDatabase
import io.bumbumapps.radio.internetradioplayer.data.net.CoverArtService
import io.bumbumapps.radio.internetradioplayer.data.net.RestServiceProvider
import io.bumbumapps.radio.internetradioplayer.data.net.UberStationsService
import io.bumbumapps.radio.internetradioplayer.data.repository.*
import io.bumbumapps.radio.internetradioplayer.data.service.player.LoadControl
import io.bumbumapps.radio.internetradioplayer.data.utils.DiskCacheManager
import io.bumbumapps.radio.internetradioplayer.data.utils.ShortcutHelper
import io.bumbumapps.radio.internetradioplayer.data.utils.StationParser
import io.bumbumapps.radio.internetradioplayer.domain.interactor.*
import okhttp3.OkHttpClient
import toothpick.config.Module

/**
 * Created by Vladimir Mikhalev 30.09.2017.
 */

class AppModule(context: Context) : Module() {

    init {
        bind(Context::class.java).toInstance(context)

        val cacheManager = DiskCacheManager(context)
        val cachedOkHttpClient = RestServiceProvider.cachedOkHttpClient(cacheManager)

        bind(OkHttpClient::class.java).toInstance(RestServiceProvider.okHttpClient)
        bind(UberStationsService::class.java)
                .toInstance(RestServiceProvider.getUberStationsService(cachedOkHttpClient))
        bind(CoverArtService::class.java)
                .toInstance(RestServiceProvider.getCoverArtService(cachedOkHttpClient))

        bind(StationsDatabase::class.java).toInstance(StationsDatabase.newInstance(context))
        bind(SuggestionsDatabase::class.java).toInstance(SuggestionsDatabase.newInstance(context))
        bind(HistoryDatabase::class.java).toInstance(HistoryDatabase.newInstance(context))
        bind(EqualizerDatabase::class.java).toInstance(EqualizerDatabase.newInstance(context))

        bind(StationParser::class.java).singletonInScope()
        bind(ShortcutHelper::class.java).singletonInScope()
        bind(MediaMetadataRetriever::class.java).toInstance(MediaMetadataRetriever())

        bind(SearchRepository::class.java).singletonInScope()
        bind(FavoritesRepository::class.java).singletonInScope()
        bind(StationRepository::class.java).singletonInScope()
        bind(PlayerRepository::class.java).singletonInScope()
        bind(HistoryRepository::class.java).singletonInScope()
        bind(EqualizerRepository::class.java).singletonInScope()
        bind(MediaRepository::class.java).singletonInScope()
        bind(RecordsRepository::class.java).singletonInScope()
        bind(SuggestionRepository::class.java).singletonInScope()

        bind(MainInteractor::class.java).singletonInScope()
        bind(SearchInteractor::class.java).singletonInScope()
        bind(FavoriteListInteractor::class.java).singletonInScope()
        bind(StationInteractor::class.java).singletonInScope()
        bind(PlayerInteractor::class.java).singletonInScope()
        bind(HistoryInteractor::class.java).singletonInScope()
        bind(EqualizerInteractor::class.java).singletonInScope()
        bind(MediaInteractor::class.java).singletonInScope()
        bind(RecordsInteractor::class.java).singletonInScope()
        bind(SuggestionInteractor::class.java).singletonInScope()

        bind(LoadControl::class.java).singletonInScope()
    }
}
