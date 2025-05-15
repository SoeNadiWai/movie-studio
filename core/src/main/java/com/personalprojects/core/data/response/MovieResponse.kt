package com.personalprojects.core.data.response

import com.squareup.moshi.Json

data class MovieResponse(
    val adult: Boolean,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "genre_ids") val genreIds: List<Int>,
    val id: Int,
    @Json(name = "original_language") val originalLanguage: String,
    @Json(name = "original_title") val originalTitle: String,
    val overview: String,
    val popularity: Double,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "release_date") val releaseDate: String,
    val title: String,
    val video: Boolean,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "vote_count") val voteCount: Int
)

// Main response object
data class MovieDetailsResponse(
    @Json(name = "adult") val adult: Boolean?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "belongs_to_collection") val belongsToCollection: CollectionInfo?,
    @Json(name = "budget") val budget: Long?, // Use Long for potentially large values
    @Json(name = "genres") val genres: List<Genre>?,
    @Json(name = "homepage") val homepage: String?,
    @Json(name = "id") val id: Int?, // Usually non-null, but safer as nullable
    @Json(name = "imdb_id") val imdbId: String?,
    @Json(name = "origin_country") val originCountry: List<String>?,
    @Json(name = "original_language") val originalLanguage: String?,
    @Json(name = "original_title") val originalTitle: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "popularity") val popularity: Double?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "production_companies") val productionCompanies: List<ProductionCompany>?,
    @Json(name = "production_countries") val productionCountries: List<ProductionCountry>?,
    @Json(name = "release_date") val releaseDate: String?, // Keep as String, parse later if needed
    @Json(name = "revenue") val revenue: Long?, // Use Long for potentially large values
    @Json(name = "runtime") val runtime: Int?,
    @Json(name = "spoken_languages") val spokenLanguages: List<SpokenLanguage>?,
    @Json(name = "status") val status: String?,
    @Json(name = "tagline") val tagline: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "video") val video: Boolean?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "watch/providers") val watchProviders: WatchProvidersApiResponse?
)

data class WatchProvidersApiResponse(
    @Json(name = "results") val results: Map<String, WatchProviderCountryResult>?
)

data class WatchProviderCountryResult(
    @Json(name = "link") val link: String?,
    @Json(name = "flatrate") val flatrate: List<WatchProviderApiResponseItem>?,
    @Json(name = "rent") val rent: List<WatchProviderApiResponseItem>?,
    @Json(name = "buy") val buy: List<WatchProviderApiResponseItem>?
)

data class WatchProviderApiResponseItem(
    @Json(name = "logo_path") val logoPath: String?,
    @Json(name = "provider_id") val providerId: Int?,
    @Json(name = "provider_name") val providerName: String?,
    @Json(name = "display_priority") val displayPriority: Int?
)

// Nested data class for "belongs_to_collection"
data class CollectionInfo(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?
)

// Nested data class for items in the "genres" list
data class Genre(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

// Nested data class for items in the "production_companies" list
data class ProductionCompany(
    @Json(name = "id") val id: Int?,
    @Json(name = "logo_path") val logoPath: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "origin_country") val originCountry: String?
)

// Nested data class for items in the "production_countries" list
data class ProductionCountry(
    @Json(name = "iso_3166_1") val iso31661: String?,
    @Json(name = "name") val name: String?
)

// Nested data class for items in the "spoken_languages" list
data class SpokenLanguage(
    @Json(name = "english_name") val englishName: String?,
    @Json(name = "iso_639_1") val iso6391: String?,
    @Json(name = "name") val name: String?
)
