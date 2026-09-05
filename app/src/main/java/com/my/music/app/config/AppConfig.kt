package com.my.music.app.config

object AppConfig {
    const val SEARCH_DEBOUNCE_MILLIS = 600L
    const val PROGRESS_UPDATE_INTERVAL_MILLIS = 250L

    const val DEFAULT_SEARCH_TERM = ""

    const val INITIAL_PAGE_SIZE = 20
    const val LOAD_MORE_SIZE = 20
    const val MAX_RESULTS = 200
    const val BASE_URL = "https://itunes.apple.com/"
}