package be.bxl.moorluck.thisisachat.models

import java.time.LocalDate

data class Message (
    val userId : String? = null,
    val pseudo : String? = null,
    val imgProfileRef : String? = null,
    val date : String? = null,
    val content : String? = null,
    val imgContent : String? = null
)