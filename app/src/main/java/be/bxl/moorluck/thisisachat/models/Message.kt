package be.bxl.moorluck.thisisachat.models

data class Message (
    val content : String = "",
    val date : Long,
    val senderId : String
)