package be.bxl.moorluck.thisisachat.models

data class User(
    val email : String? = null,
    val pseudo : String? = null,
    val rooms : List<String> = listOf(), //TODO Transform to a Map
    val imgUrl : String? = null,
    val position : Position = Position()
)