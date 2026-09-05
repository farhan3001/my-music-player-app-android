package com.my.music.app.di

import com.my.music.app.config.AppConfig.BASE_URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DataModuleTest {

    @Test
    fun provideRetrofitUsesTheConfiguredBaseUrl() {
        val retrofit = DataModule.provideRetrofit()

        assertEquals(BASE_URL, retrofit.baseUrl().toString())
    }

    @Test
    fun provideItunesApiReturnsANonNullImplementation() {
        val retrofit = DataModule.provideRetrofit()
        val api = DataModule.provideItunesApi(retrofit)

        assertNotNull(api)
    }
}