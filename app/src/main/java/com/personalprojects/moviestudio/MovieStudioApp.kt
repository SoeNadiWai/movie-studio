package com.personalprojects.moviestudio

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.personalprojects.favorites.FavoritesScreen
import com.personalprojects.home.HomeScreen
import com.personalprojects.moviedetail.MovieDetailScreen
import com.personalprojects.movielist.MovieListScreen
import com.personalprojects.moviestudio.ui.CustomBottomNavigationItem
import com.personalprojects.search.SearchScreen

@Composable
fun MovieStudioApp(
    appState: MovieStudioAppState = rememberMovieStudioAppState()
) {
    if (appState.isOnline) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // Show Bottom Bar only on relevant screens
                if (appState.shouldShowBottomBar) {

                    // The actual styled bar container
                    Surface( // Using Surface for elevation, shape, color
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .height(64.dp), // Adjust height as needed
                        // .shadow(elevation = 4.dp, shape = RoundedCornerShape(32.dp)), // Optional shadow
                        shape = RoundedCornerShape(32.dp), // Fully rounded corners for capsule
                        color = Color(0xFF222222), // Dark background color example
                        tonalElevation = 4.dp // Or use elevation if preferred over shadow modifier
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp), // Internal padding for items
                            horizontalArrangement = Arrangement.SpaceAround, // Distribute items
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Iterate over your nav items
                            bottomNavItems.forEach { item ->
                                val isSelected = appState.currentTopLevelRoute == item.route
                                CustomBottomNavigationItem(
                                    item = item,
                                    isSelected = isSelected,
                                    selectedColor = Color(0xFFE50914), // Example Red indicator color
                                    unselectedColor = Color.Gray, // Example Gray unselected color
                                    onClick = { appState.navigateToTopLevelRoute(item.route) }
                                )
                            }
                        }
                    }
//                    AppBottomNavigationBar(
//                        items = bottomNavItems,
//                        currentRoute = appState.currentTopLevelRoute,
//                        onItemClick = { route -> appState.navigateToTopLevelRoute(route) })
                }
            }

        ) { innerPadding ->
            NavHost(
                navController = appState.navController,
                startDestination = AppDestinations.HOME,
                popExitTransition = { scaleOut(targetScale = 0.9f) },
                popEnterTransition = { EnterTransition.None },
                modifier = Modifier
                    .background(Color.Black)
                    .padding( // Ignore bottom padding from Scaffold
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                    )
                    .fillMaxSize()
            ) {
                composable(route = AppDestinations.HOME) {
                    HomeScreen(
                        onMovieClick = { movieId ->
                            val route = "${AppDestinations.MOVIE_DETAILS}/$movieId"
                            appState.navController.navigate(route)
                        }, onSeeAllClick = {
                            val route = "${AppDestinations.MOVIE_LIST}/${it.name}"
                            appState.navController.navigate(route)
                        },
                        onSearchFilterClick = {
                            appState.navController.navigate(AppDestinations.MOVIE_SEARCH)
                        },
                        innerPadding = innerPadding
                    )
                }

                composable(route = AppDestinations.FAVORITE) {
                    FavoritesScreen(
                        onNavigateToDetail = {
                            val route = "${AppDestinations.MOVIE_DETAILS}/$it"
                            appState.navController.navigate(route)
                        },
                        onNavigateUp = { appState.navController.popBackStack() }
                    )
                }

                composable(route = AppDestinations.WATCHLIST) {
                    // HomeScreen()
                }

                composable(route = AppDestinations.SETTINGS) {
                    // HomeScreen()
                }

                composable(
                    route = AppDestinations.MOVIE_DETAILS_ROUTE,
                    arguments = listOf(navArgument(AppDestinations.MOVIE_DETAILS_ARG_ID) {
                        type = NavType.IntType
                    })
                ) { _ ->
                    MovieDetailScreen(onNavigateUp = { appState.navController.popBackStack() })
                }

                composable(
                    route = AppDestinations.MOVIE_LIST_ROUTE,
                    arguments = listOf(navArgument(AppDestinations.MOVIE_LIST_CATEGORY_ARG) {
                        type = NavType.StringType
                    })
                ) { _ ->
                    MovieListScreen(
                        onNavigateUp = { appState.navigateBack() },
                        onNavigateToDetail = {
                            val route = "${AppDestinations.MOVIE_DETAILS}/$it"
                            appState.navController.navigate(route)
                        })
                }

                // Inside MovieStudioNavHost
                composable(route = AppDestinations.MOVIE_SEARCH) { // Assuming you have a SEARCH route
                    SearchScreen(
                        onNavigateToDetail = { movieId ->
                            val route = "${AppDestinations.MOVIE_DETAILS}/$movieId"
                            appState.navController.navigate(route)
                        },
                        onNavigateUp = { appState.navigateBack() },
                    )
                }
            }
        }

    } else {
        OfflineDialog { appState.refreshOnline() }
    }
}


@Composable
fun OfflineDialog(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(R.string.connection_error_title)) },
        text = { Text(text = stringResource(R.string.connection_error_message)) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry_label))
            }
        }
    )
}