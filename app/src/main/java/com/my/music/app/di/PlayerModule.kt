package com.my.music.app.di

import com.my.music.app.data.player.ExoPlayerController
import com.my.music.app.data.player.PlayerController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class PlayerModule {

    @Binds
    @ViewModelScoped
    abstract fun bindPlayerController(impl: ExoPlayerController): PlayerController
}