package be.bxl.moorluck.thisisachat.api.models

data class Address(
    val country: String? = null,
    val country_code: String? = null,

    val county: String? = null,
    val state: String? = null,
    val region: String? = null,

    val city: String? = null,
    val city_district: String? = null,
    val town: String? = null,
    val village: String? = null,

    val road: String? = null,
    val house_number: String? = null,
    val neighbourhood: String? = null,
    val postcode: String? = null,
)