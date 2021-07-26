package be.bxl.moorluck.thisisachat.models

data class User(
    val email : String,
    val password : String,
    val pseudo : String,
    val rooms : List<Room> = listOf(),
    val imgId : String = "",
    val lat : Double = 55.5833,
    val long : Double = -4.3833
)