package be.bxl.moorluck.thisisachat.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Message (
    val userId : String? = null,
    val pseudo : String? = null,
    val imgProfileRef : String? = null,
    val date : String? = null,
    val content : String? = null,
    val imgContent : String? = null
) : Parcelable