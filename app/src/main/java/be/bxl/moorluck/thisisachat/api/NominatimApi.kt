package be.bxl.moorluck.thisisachat.api

import be.bxl.moorluck.thisisachat.api.models.Place
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {

    @GET("reverse")
    suspend fun getPlace(@Query("lat") lat : Double, @Query("lon") lon : Double,
                         @Query("format") format : String = "jsonv2") : Place

}