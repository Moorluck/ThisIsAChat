package be.bxl.moorluck.thisisachat.models

data class Rule (
    val messageByHour : Int = 0,
    val renameMember : Boolean = true,
    val manageGrade : Boolean = true,
    val manageMaxUsers : Boolean = true
)
