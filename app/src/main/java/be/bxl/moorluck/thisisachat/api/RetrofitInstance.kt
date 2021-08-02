package be.bxl.moorluck.thisisachat.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofitGeocoding by lazy {
        Retrofit.Builder()
            .baseUrl(Url.BASE_URL_GEOCODING)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val retrofitPlace by lazy {
        Retrofit.Builder()
            .baseUrl(Url.BASE_URL_PLACE)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiGeocoding: NominatimApi by lazy {
        retrofitGeocoding.create(NominatimApi::class.java)
    }

    val apiPlace: PlacesApi by lazy {
        retrofitPlace.create(PlacesApi::class.java)
    }
}