package be.bxl.moorluck.thisisachat.models

data class Room (
    val name : String,
    val users : Map<User, String>, // String is the custom name of the room
    val maxUsers : Int = 20,
    val shortCuts : List<ShortCut>,
    val grades : Map<Grade, List<User>> = mapOf(Grade() to users.keys.toList())
)