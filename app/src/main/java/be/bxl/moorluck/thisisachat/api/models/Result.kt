package be.bxl.moorluck.thisisachat.api.models

data class Result(
    val formatted_address: String,
    val geometry: Geometry,
    val icon: String,
    val icon_background_color: String,
    val icon_mask_base_uri: String,
    val name: String,
    val photos: List<Photo>,
    val place_id: String,
    val reference: String,
    val types: List<String>
)