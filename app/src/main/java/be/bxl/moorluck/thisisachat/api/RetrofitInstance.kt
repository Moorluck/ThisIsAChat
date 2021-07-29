package be.bxl.moorluck.thisisachat.api

import com.google.gson.GsonBuilder
import com.google.gson.internal.GsonBuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: NominatimApi by lazy {
        retrofit.create(NominatimApi::class.java)
    }
}