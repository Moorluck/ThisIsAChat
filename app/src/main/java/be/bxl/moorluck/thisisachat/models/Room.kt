package be.bxl.moorluck.thisisachat.models

import java.util.*

data class Room (
    val type : String? = null,
    val id : String? = null,
    val name : String? = null,
    var users : Map<String, String> = mapOf(), // UserID, CustomName
    val photoRef : String = "",
    val messages : Map<String, Message> = mapOf(), // UserID, Content | Attention ! utilisé push pour en faire une liste
    val maxUsers : Int = 20,
    val shortCuts : Map<String, String> = mapOf(), // Message, Imgurl
    var grades : Map<String, Grade> = mapOf()
)