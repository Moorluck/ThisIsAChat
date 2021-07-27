package be.bxl.moorluck.thisisachat.models

data class User(
    val email : String? = null,
    val pseudo : String? = null,
    val rooms : List<Room> = listOf(),
    val imgUrl : String? = null,
    val position : Position = Position()
)