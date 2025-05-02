package com.personalprojects.moviestudio

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * List of screens for [MovieStudioApp]
 */
object AppDestinations {
    const val HOME = "home"
    const val FAVORITE = "favorite"
    const val WATCHLIST = "watchlist"
    const val SETTINGS = "settings"

    const val MOVIE_DETAILS = "movie_details"
    const val MOVIE_DETAILS_ARG_ID = "movieId"
    const val MOVIE_DETAILS_ROUTE = "$MOVIE_DETAILS/{$MOVIE_DETAILS_ARG_ID}"

    const val MOVIE_LIST = "movie_list"
    const val MOVIE_LIST_CATEGORY_ARG = "categoryType"
    const val MOVIE_LIST_ROUTE = "$MOVIE_LIST/{$MOVIE_LIST_CATEGORY_ARG}"

    const val MOVIE_SEARCH = "movie_search"

}

val topLevelDestinations = listOf(
    AppDestinations.HOME,
    AppDestinations.FAVORITE,
    AppDestinations.WATCHLIST,
    AppDestinations.SETTINGS
)

class MovieStudioAppState(
    val navController: NavHostController,
    private val context: Context
) {

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val currentTopLevelRoute: String?
        @Composable get() = topLevelDestinations.firstOrNull { isTopLevelRoute(it) }

    val shouldShowBottomBar: Boolean
        @Composable get() = currentDestination?.route in topLevelDestinations

    @Composable
    private fun isTopLevelRoute(route: String): Boolean {
        return currentDestination?.hierarchy?.any { it.route == route } == true
    }

    fun navigateToTopLevelRoute(topLevelRoute: String){
        navController.navigate(topLevelRoute){
            popUpTo(navController.graph.findStartDestination().id){
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }


    fun navigateBack() {
        navController.popBackStack()
    }

    var isOnline by mutableStateOf(checkIfOnline())
        private set

    fun refreshOnline() {
        isOnline = checkIfOnline()
    }
    private fun checkIfOnline(): Boolean {
        val cm = getSystemService(context, ConnectivityManager::class.java)

        val capabilities = cm?.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    }
}

@Composable
fun rememberMovieStudioAppState(
    navController: NavHostController = rememberNavController(),
    context: Context = LocalContext.current
): MovieStudioAppState {
    return remember(navController, context) {
        MovieStudioAppState(navController, context)
    }
}
