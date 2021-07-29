package be.bxl.moorluck.thisisachat.api.models

data class Place(
    val address: Address,
    val addresstype: String,
    val boundingbox: List<String>,
    val category: String,
    val display_name: String,
    val importance: Int,
    val lat: String,
    val licence: String,
    val lon: String,
    val name: Any,
    val osm_id: Int,
    val osm_type: String,
    val place_id: Int,
    val place_rank: Int,
    val type: String
)