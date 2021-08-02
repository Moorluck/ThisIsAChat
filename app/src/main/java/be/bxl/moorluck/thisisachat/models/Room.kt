package be.bxl.moorluck.thisisachat.models

data class Room (
    val name : String? = null,
    var users : Map<String, String> = mapOf(), // UserID, CustomName
    val photoRef : String = "",
    val message : Map<String, Message> = mapOf(), // UserID, Content | Attention ! utilisé push pour en faire une liste
    val maxUsers : Int = 20,
    val shortCuts : Map<String, String> = mapOf(), // Message, Imgurl
    var grades : Map<String, Grade> = mapOf()
)