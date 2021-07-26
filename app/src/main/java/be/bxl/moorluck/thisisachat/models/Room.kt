package be.bxl.moorluck.thisisachat.models

data class Room (
    val name : String? = null,
    val users : Map<String, String> = mapOf(), // UserID, CustomName
    val message : Map<String, String>, // UserID, Content
    val maxUsers : Int = 20,
    val shortCuts : List<ShortCut> = listOf(),
    val grades : Map<Grade, List<String>> = mapOf(Grade() to users.keys.toList())
)