package com.personalprojects.core.data

import com.personalprojects.core.data.response.CollectionInfo
import com.personalprojects.core.data.response.Genre
import com.personalprojects.core.data.response.MovieDetailsResponse
import com.personalprojects.core.data.response.MovieResponse
import com.personalprojects.core.data.response.ProductionCompany
import com.personalprojects.core.data.response.ProductionCountry
import com.personalprojects.core.data.response.SpokenLanguage
import com.personalprojects.core.data.response.WatchProviderApiResponseItem
import com.personalprojects.core.data.response.WatchProviderCountryResult
import com.personalprojects.core.data.response.WatchProvidersApiResponse
import com.squareup.moshi.Json

data class Movie(
    val adult: Boolean,
    val backdropPath: String?,
    val genreIds: List<Int>,
    val id: Int,
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    val posterPath: String?,
    val releaseDate: String,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int,
) {
    companion object {
        fun toMovie(movieResponse: MovieResponse): Movie {
            return Movie(
                adult = movieResponse.adult,
                backdropPath = movieResponse.backdropPath,
                genreIds = movieResponse.genreIds,
                id = movieResponse.id,
                originalLanguage = movieResponse.originalLanguage,
                originalTitle = movieResponse.originalTitle,
                overview = movieResponse.overview,
                popularity = movieResponse.popularity,
                posterPath = movieResponse.posterPath,
                releaseDate = movieResponse.releaseDate,
                title = movieResponse.title,
                video = movieResponse.video,
                voteAverage = movieResponse.voteAverage,
                voteCount = movieResponse.voteCount,
            )
        }
    }
}

data class MovieDetails(
    val adult: Boolean?,
    val backdropPath: String?,
    val belongsToCollection: CollectionInfo?,
    val budget: Long?,
    val genres: List<Genre>?,
    val homepage: String?,
    val id: Int?,
    val imdbId: String?,
    val originCountry: List<String>?,
    val originalLanguage: String?,
    val originalTitle: String?,
    val overview: String?,
    val popularity: Double?,
    val posterPath: String?,
    val productionCompanies: List<ProductionCompany>?,
    val productionCountries: List<ProductionCountry>?,
    val releaseDate: String?,
    val revenue: Long?,
    val runtime: Int?,
    val spokenLanguages: List<SpokenLanguage>?,
    val status: String?,
    val tagline: String?,
    val title: String?,
    val video: Boolean?,
    val voteAverage: Double?,
    val voteCount: Int?,
    val watchProvidersLink: String? = null,
    val flatrateProviders: List<WatchProvider>? = null,
    val rentProviders: List<WatchProvider>? = null,
    val buyProviders: List<WatchProvider>? = null,
    val watchProviders: WatchProvidersApiResponse? = null
) {
    companion object {
        fun toMovieDetails(response: MovieDetailsResponse): MovieDetails {
            return MovieDetails(
                adult = response.adult,
                backdropPath = response.backdropPath,
                budget = response.budget,
                belongsToCollection = response.belongsToCollection,
                genres = response.genres,
                homepage = response.homepage,
                id = response.id,
                imdbId = response.imdbId,
                originCountry = response.originCountry,
                originalLanguage = response.originalLanguage,
                originalTitle = response.originalTitle,
                overview = response.overview,
                popularity = response.popularity,
                posterPath = response.posterPath,
                productionCompanies = response.productionCompanies,
                productionCountries = response.productionCountries,
                releaseDate = response.releaseDate,
                revenue = response.revenue,
                runtime = response.runtime,
                spokenLanguages = response.spokenLanguages,
                status = response.status,
                tagline = response.tagline,
                title = response.title,
                video = response.video,
                voteAverage = response.voteAverage,
                voteCount = response.voteCount,
                watchProviders = response.watchProviders
            )
        }
    }
}

data class WatchProvider(
    val providerId: Int?,
    val providerName: String?,
    val logoPath: String?,
    val displayPriority: Int?
) {
    companion object {
        fun toWatchProvider(response: WatchProvidersApiResponse): WatchProvider {
            return WatchProvider(
                providerId = response.results?.get("US")?.flatrate?.get(0)?.providerId,
                providerName = response.results?.get("US")?.flatrate?.get(0)?.providerName,
                logoPath = response.results?.get("US")?.flatrate?.get(0)?.logoPath,
                displayPriority = response.results?.get("US")?.flatrate?.get(0)?.displayPriority
            )
        }
    }
}

fun WatchProviderApiResponseItem.toWatchProvider(): WatchProvider? {
    if (providerId == null || providerName == null || logoPath == null) return null
    return WatchProvider(
        providerId = providerId,
        providerName = providerName,
        logoPath = logoPath,
        displayPriority = displayPriority
    )
}