package com.dhun.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dhun.app.DhunApp
import com.dhun.app.components.BottomTab
import com.dhun.app.components.MiniPlayer
import com.dhun.app.ui.screens.album.AlbumDetailScreen
import com.dhun.app.ui.screens.album.ArtistDetailScreen
import com.dhun.app.ui.screens.album.FolderDetailScreen
import com.dhun.app.ui.screens.library.LibraryScreen
import com.dhun.app.ui.screens.nowplaying.NowPlayingScreen
import com.dhun.app.ui.screens.permission.PermissionScreen
import com.dhun.app.ui.screens.playlists.PlaylistDetailScreen
import com.dhun.app.ui.screens.playlists.PlaylistsListScreen
import com.dhun.app.ui.screens.queue.QueueSheet
import com.dhun.app.ui.screens.search.SearchScreen
import com.dhun.app.ui.screens.settings.SettingsScreen
import com.dhun.app.ui.screens.splash.SplashScreen
import com.dhun.app.ui.theme.DhunTheme
import com.dhun.app.ui.theme.LocalDhunColors

class MainActivity : ComponentActivity() {

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* app continues regardless */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Do NOT manually startForegroundService the media session service:
        // Media3 starts/stops the service automatically when playback begins and
        // calls startForeground() via DefaultMediaNotificationProvider. Pre-starting
        // caused ForegroundServiceDidNotStartInTimeException ANRs on MIUI.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            DhunTheme {
                AppRoot()
            }
        }
    }
}

private object Routes {
    const val Splash = "splash"
    const val Permission = "perm"
    const val Home = "home"
    const val Search = "search"
    const val Playlists = "playlists"
    const val Settings = "settings"
    const val NowPlaying = "nowplaying"
    const val Album = "album/{id}"
    const val Artist = "artist/{name}"
    const val Folder = "folder/{path}"
    const val Playlist = "playlist/{id}"
    fun album(id: Long) = "album/$id"
    fun artist(name: String) = "artist/${java.net.URLEncoder.encode(name, "UTF-8")}"
    fun folder(path: String) = "folder/${java.net.URLEncoder.encode(path, "UTF-8")}"
    fun playlist(id: Long) = "playlist/$id"
}

private enum class Tab { Library, Playlists, Search, Settings }

// Tabs that show bottom bar + mini player
private val bottomBarRoutes = setOf(Routes.Home, Routes.Playlists, Routes.Search, Routes.Settings)

@Composable
fun AppRoot() {
    val c = LocalDhunColors.current
    val nav = rememberNavController()
    var showQueue by remember { mutableStateOf(false) }
    val app = DhunApp.instance

    // Observe current route
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Determine which tab is active from current route
    val currentTab = when (currentRoute) {
        Routes.Home -> Tab.Library
        Routes.Playlists -> Tab.Playlists
        Routes.Search -> Tab.Search
        Routes.Settings -> Tab.Settings
        else -> null
    }
    val showBottomBar = currentRoute in bottomBarRoutes

    val playerState by app.player.state.collectAsState()
    val hasTrack = playerState.queue.isNotEmpty() && playerState.durationMs > 0

    LaunchedEffect(Unit) {
        nav.navigate(Routes.Splash) { popUpTo(0) { inclusive = true } }
    }

    fun navigateTab(tab: Tab) {
        val route = when (tab) {
            Tab.Library -> Routes.Home
            Tab.Playlists -> Routes.Playlists
            Tab.Search -> Routes.Search
            Tab.Settings -> Routes.Settings
        }
        // pop up to home so back closes app from any tab, not stack tabs forever
        nav.navigate(route) {
            launchSingleTop = true
            popUpTo(Routes.Home) {
                saveState = true
                inclusive = false
            }
            restoreState = true
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                NavHost(navController = nav, startDestination = Routes.Splash, modifier = Modifier.fillMaxSize()) {
                    composable(Routes.Splash) {
                        SplashScreen(
                            onReady = { nav.navigate(Routes.Home) { popUpTo(Routes.Splash) { inclusive = true } } },
                            onNeedPermission = { nav.navigate(Routes.Permission) { popUpTo(Routes.Splash) { inclusive = true } } },
                        )
                    }
                    composable(Routes.Permission) {
                        PermissionScreen(
                            onGranted = { nav.navigate(Routes.Splash) { popUpTo(0) { inclusive = true } } },
                        )
                    }
                    composable(Routes.Home) {
                        LibraryScreen(
                            onOpenNowPlaying = { nav.navigate(Routes.NowPlaying) },
                            onOpenSearch = { navigateTab(Tab.Search) },
                            onOpenAlbum = { id -> nav.navigate(Routes.album(id)) },
                            onOpenArtist = { name -> nav.navigate(Routes.artist(name)) },
                            onOpenFolder = { p -> nav.navigate(Routes.folder(p)) },
                            onOpenSettings = { navigateTab(Tab.Settings) },
                        )
                    }
                    composable(Routes.Search) {
                        SearchScreen(
                            onBack = { nav.popBackStack() },
                            onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                            onOpenAlbum = { id -> nav.navigate(Routes.album(id)) },
                            onOpenArtist = { name -> nav.navigate(Routes.artist(name)) },
                        )
                    }
                    composable(Routes.Playlists) {
                        PlaylistsListScreen(onOpenPlaylist = { id -> nav.navigate(Routes.playlist(id)) })
                    }
                    composable(Routes.Settings) {
                        SettingsScreen(onBack = { nav.popBackStack() })
                    }
                    composable(Routes.NowPlaying) {
                        Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                            NowPlayingScreen(
                                onCollapse = { nav.popBackStack() },
                                onOpenQueue = { showQueue = true },
                                onAddToPlaylist = { },
                            )
                        }
                    }
                    composable(Routes.Album, arguments = listOf(androidx.navigation.navArgument("id") { type = NavType.LongType })) { entry ->
                        val id = entry.arguments?.getLong("id") ?: -1L
                        AlbumDetailScreen(
                            albumId = id,
                            onBack = { nav.popBackStack() },
                            onGoToArtist = { name -> nav.navigate(Routes.artist(name)) },
                            onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                        )
                    }
                    composable(Routes.Artist) { entry ->
                        val name = java.net.URLDecoder.decode(entry.arguments?.getString("name") ?: "", "UTF-8")
                        ArtistDetailScreen(
                            artistName = name,
                            onBack = { nav.popBackStack() },
                            onOpenAlbum = { id -> nav.navigate(Routes.album(id)) },
                            onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                        )
                    }
                    composable(Routes.Folder) { entry ->
                        val path = java.net.URLDecoder.decode(entry.arguments?.getString("path") ?: "", "UTF-8")
                        FolderDetailScreen(
                            path = path,
                            onBack = { nav.popBackStack() },
                            onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                        )
                    }
                    composable(Routes.Playlist, arguments = listOf(androidx.navigation.navArgument("id") { type = NavType.LongType })) { entry ->
                        val id = entry.arguments?.getLong("id") ?: -1L
                        PlaylistDetailScreen(
                            playlistId = id,
                            onBack = { nav.popBackStack() },
                            onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                        )
                    }
                }
            }

            if (showBottomBar) {
                Column(Modifier.navigationBarsPadding()) {
                    if (hasTrack) {
                        MiniPlayer(
                            onExpand = { nav.navigate(Routes.NowPlaying) },
                            onDismiss = { app.player.stopAndHide() },
                            onSwipePrev = { app.player.prev() },
                            onSwipeNext = { app.player.next() },
                        )
                    }
                    BottomTabBar(currentTab ?: Tab.Library, currentTab != null) { t -> navigateTab(t) }
                }
            }
        }

        if (showQueue) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
                Box(Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp)
                    .navigationBarsPadding()
                ) {
                    QueueSheet(
                        onClose = { showQueue = false },
                        onAddTracks = { },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTabBar(current: Tab, enabled: Boolean, onTab: (Tab) -> Unit) {
    val c = LocalDhunColors.current
    Row(Modifier.fillMaxWidth().background(c.bg).border(1.dp, c.border).height(58.dp)) {
        BottomTab(
            icon = Icons.Default.Home, label = "library", active = enabled && current == Tab.Library,
            onClick = { onTab(Tab.Library) },
        )
        BottomTab(
            icon = Icons.Default.PlaylistPlay, label = "playlists", active = enabled && current == Tab.Playlists,
            onClick = { onTab(Tab.Playlists) },
        )
        BottomTab(
            icon = Icons.Default.Search, label = "search", active = enabled && current == Tab.Search,
            onClick = { onTab(Tab.Search) },
        )
        BottomTab(
            icon = Icons.Default.Settings, label = "settings", active = enabled && current == Tab.Settings,
            onClick = { onTab(Tab.Settings) },
        )
    }
}
