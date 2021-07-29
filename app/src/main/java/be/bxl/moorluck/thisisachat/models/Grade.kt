package be.bxl.moorluck.thisisachat.models

data class Grade (
    val name : String = "Users",
    val users : List<String> = listOf(), //TODO change to Map<String, String>
    val rule : Rule = Rule()
)
