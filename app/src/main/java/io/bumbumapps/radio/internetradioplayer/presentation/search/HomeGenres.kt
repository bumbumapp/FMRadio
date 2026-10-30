package io.bumbumapps.radio.internetradioplayer.presentation.search

/** Genre shortcuts shown on the Bento search home (title in the navigation tree -> tile colour). */
object HomeGenres {

    val FEATURED = listOf("Hit Music", "Rock", "Jazz", "80's", "Electronic", "Hip Hop")

    /** Returns the featured titles that exist in [available] (case-insensitive), in featured order, max [limit]. */
    fun pick(available: List<String>, limit: Int = 6): List<String> =
        FEATURED.mapNotNull { f -> available.firstOrNull { it.equals(f, ignoreCase = true) } }.take(limit)
}
