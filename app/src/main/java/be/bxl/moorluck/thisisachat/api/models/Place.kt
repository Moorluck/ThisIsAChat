package be.bxl.moorluck.thisisachat.api.models

data class Place(
    val address: Address,
    val addresstype: String,
    val boundingbox: List<String>,
    val category: String,
    val display_name: String,
    val importance: Double,
    val lat: String,
    val licence: String,
    val lon: String,
    val name: Any,
    val osm_id: Double,
    val osm_type: String,
    val place_id: Double,
    val place_rank: Double,
    val type: String
)