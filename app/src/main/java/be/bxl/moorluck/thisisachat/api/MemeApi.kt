package be.bxl.moorluck.thisisachat.api

import be.bxl.moorluck.thisisachat.api.models.Meme
import retrofit2.http.GET

interface MemeApi {

    @GET("gimme")
    suspend fun getMeme() : Meme

}