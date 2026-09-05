package com.my.music.app.predeterminedClassesObject

import com.my.music.app.data.MusicRepository
import com.my.music.app.model.Music

class FakeMusicRepository : MusicRepository {

    var songsToReturn: List<Music> = emptyList()
    var shouldThrow: Boolean = false
    val requestedLimits = mutableListOf<Int>()

    override suspend fun searchSongs(query: String, limit: Int): List<Music> {
        requestedLimits.add(limit)
        if (shouldThrow) throw RuntimeException("network error")
        return songsToReturn.take(limit)
    }
}