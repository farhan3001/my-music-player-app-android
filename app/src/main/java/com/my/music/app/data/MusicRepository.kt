package com.my.music.app.data

import com.my.music.app.data.remote.ItunesDataSource
import com.my.music.app.model.MusicTrack
import com.my.music.app.model.Music
import javax.inject.Inject

interface MusicRepository {
    suspend fun searchSongs(query: String, limit: Int): List<Music>
}

class MusicRepositoryImpl @Inject constructor(
    private val dataSource: ItunesDataSource
) : MusicRepository {

    override suspend fun searchSongs(query: String, limit: Int): List<Music> =
        dataSource.searchSongsRemote(query, limit).results.mapNotNull { it.toMusic() }

    private fun MusicTrack.toMusic(): Music? {
        val id = trackId ?: return null
        val name = trackName ?: return null
        val artist = artistName ?: return null
        val artwork = artworkUrl100 ?: return null
        val preview = previewUrl ?: return null
        return Music(
            id = id,
            trackName = name,
            artistName = artist,
            artworkUrl = artwork,
            previewUrl = preview,
            trackTimeMillis = trackTimeMillis ?: 0L
        )
    }
}