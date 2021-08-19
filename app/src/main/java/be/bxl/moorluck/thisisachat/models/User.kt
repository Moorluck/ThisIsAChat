package be.bxl.moorluck.thisisachat.models

data class User (
    val email : String? = null,
    val pseudo : String? = null,
    val rooms : Map<String, Boolean> = mapOf(), // the boolean is to know if the user read the last message
    val imgUrl : String? = null,
    val position : Position = Position()
)