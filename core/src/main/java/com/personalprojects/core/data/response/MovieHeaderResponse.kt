package com.personalprojects.core.data.response

import com.squareup.moshi.Json

data class MovieHeaderResponse(
    val page: Int,
    val results: List<MovieResponse>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

data class MovieDetailResponse(
    val movie: MovieResponse
)
