package com.personalprojects.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.personalprojects.FiveStarRatingIndicator
import com.personalprojects.core.data.Movie
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMovieClick: (movieId: Int) -> Unit,
    onSeeAllClick: (movieCategory: MovieCategory) -> Unit,
    onSearchFilterClick: () -> Unit,
    innerPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()

    val categoriesToShow = remember {
        listOf(
            MovieCategory.UPCOMING,
            MovieCategory.POPULAR,
            MovieCategory.TOP_RATED
        )
    }

    val scaffoldBottomPadding = innerPadding.calculateBottomPadding()
    val lazyListBottomPadding = scaffoldBottomPadding + 16.dp

    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            uiState.isAllLoading -> CircularProgressIndicator()
            uiState.isAllError -> ErrorDialog { viewModel.loadAllHomeScreenData() }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(bottom = lazyListBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Welcome Section
                    item {
                        TopSection()
                    }

                    // Search Bar Section
                    item {
                        SearchBarSection(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            onSearchBarClick = onSearchFilterClick
                        )
                    }

                    // Movie Carousel Section
                    val nowPlayingMovies =
                        uiState.movieLists[MovieCategory.NOW_PLAYING] ?: emptyList()
                    item {
                        if (nowPlayingMovies.isNotEmpty()) {
                            MoviePager(
                                movies = nowPlayingMovies,
                                onMovieClick = { onMovieClick(it) })
                        }
                    }

                    val popularMovies = uiState.movieLists[MovieCategory.POPULAR] ?: emptyList()
                    val topRatedMovies = uiState.movieLists[MovieCategory.TOP_RATED] ?: emptyList()
                    val upcomingMovies = uiState.movieLists[MovieCategory.UPCOMING] ?: emptyList()

                    items(
                        items = categoriesToShow,
                        key = { it.name }
                    ) { category ->
                        MovieCategorySection(
                            title = category.displayTitle,
                            movies = when (category) {
                                MovieCategory.POPULAR -> popularMovies
                                MovieCategory.TOP_RATED -> topRatedMovies
                                MovieCategory.UPCOMING -> upcomingMovies
                                MovieCategory.NOW_PLAYING -> emptyList()
                            },
                            onSeeAllClick = { onSeeAllClick(it) },
                            onMovieClick = { onMovieClick(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCategorySection(
    title: String,
    movies: List<Movie>,
    onSeeAllClick: (MovieCategory) -> Unit,
    onMovieClick: (Int) -> Unit
) {
    Column {
        // Movie Section Header
        MovieSectionHeader(
            title = title,
            onSeeAllClick = { onSeeAllClick(it) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )

        println("movies == $movies")
        // Movie Carousel
        MovieCarouselSection(
            movies = movies,
            onMovieClick = { movieId -> onMovieClick(movieId) },
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun MoviePager(
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 200.dp, // 200
    pagerHeight: Dp = 300.dp // 300
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = (screenWidth - itemWidth) / 2

    val actualMovieCount = movies.size
    val virtualPageCount = Int.MAX_VALUE
    val initialVirtualPage = (virtualPageCount / 2) - ((virtualPageCount / 2) % actualMovieCount)
    val pagerState = rememberPagerState(
        initialPage = initialVirtualPage,
        pageCount = { virtualPageCount }
    )
    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(itemWidth),
            contentPadding = PaddingValues(
                horizontal = horizontalPadding
            ),
            pageSpacing = 12.dp,
            modifier = modifier
                .fillMaxWidth()
                .height(pagerHeight)

        ) { virtualPageIndex ->
            val actualMovieIndex = virtualPageIndex % actualMovieCount
            if (actualMovieIndex in 0..<actualMovieCount) { // Should always be true with %
                val movie = movies[actualMovieIndex]
                val pageOffset =
                    (pagerState.currentPage - virtualPageIndex) + pagerState.currentPageOffsetFraction
                val scale = lerp( // Make center item 1f scale, others smaller
                    start = 0.85f, // Scale of items on the sides
                    stop = 1f,     // Scale of the center item
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )
                val alpha = lerp( // Make center item 1f alpha, others slightly faded
                    start = 0.5f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )
                MoviePagerItem(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                )
            }
        }

        val currentActualIndex = pagerState.currentPage % actualMovieCount
        val currentMovie = movies.getOrNull(currentActualIndex)
        // --- Movie Info & Indicator Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp), // Spacing below pager
            horizontalAlignment = Alignment.CenterHorizontally // Center info and indicator
        ) {
            currentMovie?.let {
                // Movie Title
                Text(
                    text = currentMovie.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                FiveStarRatingIndicator(
                    ratingOutOf10 = currentMovie.voteAverage,
                    starSize = 20.dp,
                    starColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp)) // Space before indicator
            }
        }
    }
}

@Composable
fun MoviePagerItem(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Modifier with graphicsLayer applied
) {
    Card(
        modifier = modifier
            .aspectRatio(2f / 3f) // Maintain poster aspect ratio
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                .crossfade(true)
                // .placeholder(R.drawable.placeholder_poster)
                // .error(R.drawable.error_poster)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize() // Fill the Card
        )
    }
}

@Composable
fun TopSection(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Movie Studio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(
            onClick = {/* TODO: Handle notification click */ }
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CategoryTabsSection(
    categories: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.fillMaxWidth(),
        edgePadding = 16.dp,
        indicator = {},
        divider = {}
    ) {
        categories.forEachIndexed { index, category ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = category,
                        fontWeight = if (selectedTabIndex == index) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MovieCarouselSection(
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Spacing between items
    ) {
        items(movies) { movie ->
            MovieCarouselItem(movie = movie, onClick = { onMovieClick(movie.id) })
        }
    }
}

@Composable
fun MovieCarouselItem(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .aspectRatio(2f / 3f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop, // Crop to fill bounds
                modifier = Modifier.fillMaxSize()

            )
            // Rating overlay
            RatingIndicator(
                rating = movie.popularity,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun RatingIndicator(rating: Double, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Rating Star",
            tint = Color.Yellow,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f", rating),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MovieSectionHeader(
    title: String,
    onSeeAllClick: (MovieCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = { onSeeAllClick(MovieCategory.POPULAR) }) {
            Text(
                text = "See all", // stringResource(R.string.see_all)
                // style = MaterialTheme.typography.labelMedium // Adjust style
            )
        }
    }
}


// TODO: Make Resource Module
@Composable
fun ErrorDialog(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "Error Fetching Movies") },
        text = { Text(text = "We got a problem!") },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    )
}

@Composable
fun SearchBarSection(
    modifier: Modifier = Modifier,
    onSearchBarClick: () -> Unit, // Callback when clicked
    placeholderText: String = "Search Movie"
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface( // Use Surface for shape, background, and click handling
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp) // Ensure minimum height
            // Apply clickable to the whole Surface
            .clickable(
                onClick = onSearchBarClick,
                role = Role.Button,
                interactionSource = interactionSource, // Pass the interaction source
                indication = null // Pass null to disable the ripple indication
            ),

        shape = RoundedCornerShape(30.dp), // Rounded shape
        color = Color.LightGray.copy(alpha = 0.5f), // Background color (match SearchScreen's TextField)
        tonalElevation = 1.dp // Slight elevation if desired
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize() // Fill Surface
                .padding(horizontal = 16.dp), // Internal padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Space icon and text
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null, // Decorative, action described by click
                tint = Color.Gray // Icon color
            )
            Text(
                text = placeholderText,
                color = Color.Gray, // Placeholder color
                style = MaterialTheme.typography.bodyLarge // Match TextField's typical text style
            )
        }
    }
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        onMovieClick = {},
        onSeeAllClick = {},
        innerPadding = PaddingValues(16.dp),
        onSearchFilterClick = {})
}