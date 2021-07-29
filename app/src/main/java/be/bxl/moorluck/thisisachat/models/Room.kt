package be.bxl.moorluck.thisisachat.models

data class Room (
    val name : String? = null,
    var users : Map<String, String> = mapOf(), // UserID, CustomName
    val message : Map<String, String> = mapOf(), // UserID, Content
    val maxUsers : Int = 20,
    val shortCuts : List<ShortCut> = listOf(),
    var grades : List<Grade> = listOf()
)