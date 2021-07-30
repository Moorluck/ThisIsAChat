package be.bxl.moorluck.thisisachat.models

data class Grade (
    val name : String = "Users",
    val users : Map<String, String> = mapOf(),
    val rule : Rule = Rule()
)
