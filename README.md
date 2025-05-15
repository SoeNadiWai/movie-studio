# MovieStudio 🎬

MovieStudio is an Android application designed for movie enthusiasts to discover, track, and explore films. Built with modern Android development practices, it leverages the TMDB API to provide a rich movie browsing experience.

## 🌟 Features

* **Home Screen:**
    * Displays curated lists of movies such as "Popular," "Now Playing," "Top Rated," and "Upcoming" in horizontally scrolling carousels.
    * Includes a prominent search bar to navigate to the dedicated search feature.
* **Movie Detail Screen:**
    * Shows comprehensive movie details including:
        * Backdrop image with a parallax scrolling effect.
        * Title, overview, genres, user rating (5-star display), release year, and runtime.
        * "Add to Favorites" toggle button.
        * "Share" button to share movie details via other apps.
        * "Where to Watch" section displaying streaming/rental/buy provider logos (links to TMDB for options).
* **Search Screen:**
    * **Keyword Search:** Allows users to search for movies by typing keywords, with results displayed dynamically. Includes debouncing for efficient API calls.
    * **Genre Filtering:** A filter button opens a bottom sheet allowing users to select one or more genres.
    * **Dynamic Results:** Search results (from keyword or genre filter) are displayed on the same screen with pagination.
* **Movie List Screen:**
    * Displays a full, scrollable list of movies for a specific category (e.g., "Popular Movies" when "See all" is clicked on the Home screen, or movies for a selected genre).
    * Supports pagination to load more movies as the user scrolls.
* **Favorites Screen:**
    * Displays all movies that the user has marked as favorite.
    * Allows users to remove movies from their favorites.
* **Navigation:**
    * Uses Jetpack Navigation Compose for a single-activity architecture.
    * Custom-styled floating bottom navigation bar for main sections (Home, Search, Favorites, Settings).
* **User Experience:**
    * Modern, clean UI built entirely with Jetpack Compose.
    * Native Android 12+ splash screen with a custom app icon.
    * Edge-to-edge display.

## 🛠️ Technologies & Libraries Used

* **Language:** [Kotlin](https://kotlinlang.org/) (100%)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
* **Architecture:** MVVM (Model-View-ViewModel) with a Repository pattern.
* **Navigation:** [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
* **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
* **Networking:**
    * [Retrofit 2](https://square.github.io/retrofit/) - For type-safe HTTP calls to the TMDB API.
    * [OkHttp 3](https://square.github.io/okhttp/) (usually included with Retrofit) - HTTP client.
    * [Gson](https://github.com/google/gson) (or Moshi/kotlinx.serialization) - For JSON parsing.
* **Image Loading:** [Coil](https://coil-kt.github.io/coil/) - For asynchronously loading and displaying images.
* **Local Data Persistence:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) - For storing favorite movies.
* **API:** [The Movie Database (TMDB) API](https://www.themoviedb.org/documentation/api)
* **Splash Screen:** [AndroidX Core Splashscreen API](https://developer.android.com/develop/ui/views/launch/splash-screen) (for Android 12+)
* **Material Design 3:** Components and theming.

## ⚙️ Setup & Configuration

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/YOUR_USERNAME/MovieStudio.git](https://github.com/YOUR_USERNAME/MovieStudio.git)
    ```
2.  **Open in Android Studio:** Open the cloned project in the latest stable version of Android Studio.
3.  **TMDB API Key:**
    * You will need a TMDB API key to run this application.
    * Visit [TMDB API](https://www.themoviedb.org/settings/api) to register and get your key.
    * Once you have your key, add it to the project. Typically, this is done by creating a `local.properties` file in the root of your project (if it doesn't exist) and adding the line:
        ```properties
        TMDB_API_KEY="YOUR_ACTUAL_API_KEY"
        ```
    * Then, update the `build.gradle` (app level) to read this key and make it available via `BuildConfig`:
        ```gradle
        // In android block -> defaultConfig
        buildConfigField("String", "TMDB_API_KEY", "\"${project.properties["TMDB_API_KEY"] ?: ""}\"")
        ```
    * Access it in your code (e.g., Repository or Network Module) using `BuildConfig.TMDB_API_KEY`.
4.  **Build and Run:** Build the project and run it on an Android emulator or physical device.

## 🖼️ Screenshots

<div style="display: flex;">
<img src="https://github.com/user-attachments/assets/d196596b-c7b7-461a-8582-f72937bd7da4" alt="Home Screen" style="width: 20%;"/>
&nbsp;
<img src="https://github.com/user-attachments/assets/f663a52d-83bd-421b-b31f-4f70e05a21f2" alt="Success Dialog" style="width: 20%;"/>
&nbsp;
<img src="https://github.com/user-attachments/assets/5687ea5e-8cc5-48ad-bfa6-40d961fc80f3" alt="Success Dialog" style="width: 20%;"/>
&nbsp;
<img src="https://github.com/user-attachments/assets/b437e7c9-b3ab-4ad7-91bf-a30ea7d161e1" alt="Success Dialog" style="width: 20%;"/>
</div>

## 🚀 Future Enhancements (Potential Ideas)

* User accounts and cloud sync for favorites/watchlist.
* More detailed cast and crew information pages.
* TV Show browsing and tracking.
* Personalized recommendations based on user activity.
* Advanced filtering options (year, rating range, etc.) on list screens.
* Trailers and video playback integration.

## 🤝 Contributing

Contributions are welcome! If you'd like to contribute, please fork the repository and submit a pull request. For major changes, please open an issue first to discuss what you would like to change.
