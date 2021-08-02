package be.bxl.moorluck.thisisachat.api

import be.bxl.moorluck.thisisachat.api.models.PlaceDetail
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApi {
    @GET("json")
    suspend fun getPlacesDetail(@Query("query") query : String, @Query("key") key : String) : PlaceDetail
}