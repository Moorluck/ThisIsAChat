package be.bxl.moorluck.thisisachat.models

data class Grade (
    val name : String = "Users",
    val users : List<String> = listOf(),
    val rule : Rule = Rule()
)
