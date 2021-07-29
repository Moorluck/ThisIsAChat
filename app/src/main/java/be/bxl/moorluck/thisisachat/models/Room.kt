package be.bxl.moorluck.thisisachat.models

data class Room (
    val name : String? = null,
    var users : Map<String, String> = mapOf(), // UserID, CustomName
    val message : List<Message> = listOf(), // UserID, Content
    val maxUsers : Int = 20,
    val shortCuts : Map<String, String> = mapOf(), // Message, Imgurl
    var grades : List<Grade> = listOf()
)