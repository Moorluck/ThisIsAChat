package be.bxl.moorluck.thisisachat.api

import android.content.Context
import be.bxl.moorluck.thisisachat.R

class Url {
    companion object {

        const val BASE_URL_GEOCODING = "https://nominatim.openstreetmap.org/"
        const val BASE_URL_PLACE = "https://maps.googleapis.com/maps/api/place/textsearch/"
        private const val BASE_URL_PLACE_PHOTO = "https://maps.googleapis.com/maps/api/place/photo?photoreference=__photoreference__&maxheight=__maxheight__&maxwidth=__maxwidth__&key=__key__"

        fun getPhotoUrl(photoReference : String, maxHeight : Int, maxWidth : Int, key : String) : String {
            return BASE_URL_PLACE_PHOTO.replace("__photoreference__", photoReference)
                .replace("__maxheight__", maxHeight.toString())
                .replace("__maxwidth__", maxWidth.toString())
                .replace("__key__", key)
        }

        fun getApiKey(context : Context) : String {
            return context.getString(R.string.API_KEY)
        }
    }
}