package be.bxl.moorluck.thisisachat.api.models

data class PlaceDetail(
    val html_attributions: List<Any>,
    val results: List<Result>,
    val status: String
)