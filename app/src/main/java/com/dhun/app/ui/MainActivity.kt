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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
    ) { /* regardless of result, app still runs */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // request notification permission on T+
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
    const val NowPlaying = "nowplaying"
    const val Queue = "queue"
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

@Composable
fun AppRoot() {
    val c = LocalDhunColors.current
    val nav = rememberNavController()
    var showQueue by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(Tab.Library) }
    val app = DhunApp.instance
    val playerState by app.player.state.collectAsState()
    val hasTrack = playerState.queue.isNotEmpty()

    LaunchedEffect(Unit) {
        nav.navigate(Routes.Splash) {
            popUpTo(0) { inclusive = true }
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
                            onReady = {
                                nav.navigate(Routes.Home) { popUpTo(Routes.Splash) { inclusive = true } }
                            },
                            onNeedPermission = {
                                nav.navigate(Routes.Permission) { popUpTo(Routes.Splash) { inclusive = true } }
                            },
                        )
                    }
                    composable(Routes.Permission) {
                        PermissionScreen(
                            onGranted = {
                                nav.navigate(Routes.Splash) { popUpTo(0) { inclusive = true } }
                            },
                        )
                    }
                    composable(Routes.Home) {
                        tab = Tab.Library
                        HomeScaffold(tab = tab, onTabChange = { tab = it }, nav = nav)
                    }
                    composable("search") {
                        tab = Tab.Search
                        Box(Modifier.fillMaxSize()) {
                            SearchScreen(
                                onBack = { nav.popBackStack(); tab = Tab.Library },
                                onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                                onOpenAlbum = { id -> nav.navigate(Routes.album(id)) },
                                onOpenArtist = { name -> nav.navigate(Routes.artist(name)) },
                            )
                            Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                                MiniPlayerHost(
                                    onExpand = { nav.navigate(Routes.NowPlaying) },
                                    onDismiss = { app.player.stopAndHide() },
                                    onSwipePrev = { app.player.prev() },
                                    onSwipeNext = { app.player.next() },
                                )
                                BottomTabBar(tab, onTab = { t ->
                                    tab = t
                                    when (t) {
                                        Tab.Library -> nav.navigate(Routes.Home) { popUpTo(Routes.Home) { inclusive = true } }
                                        Tab.Playlists -> nav.navigate("playlists") { popUpTo(Routes.Home) }
                                        Tab.Search -> { }
                                        Tab.Settings -> nav.navigate("settings")
                                    }
                                })
                            }
                        }
                    }
                    composable("playlists") {
                        tab = Tab.Playlists
                        Box(Modifier.fillMaxSize()) {
                            PlaylistsListScreen(onOpenPlaylist = { id -> nav.navigate(Routes.playlist(id)) })
                            Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                                MiniPlayerHost(
                                    onExpand = { nav.navigate(Routes.NowPlaying) },
                                    onDismiss = { app.player.stopAndHide() },
                                    onSwipePrev = { app.player.prev() },
                                    onSwipeNext = { app.player.next() },
                                )
                                BottomTabBar(tab, onTab = { t ->
                                    tab = t
                                    when (t) {
                                        Tab.Library -> nav.navigate(Routes.Home) { popUpTo(Routes.Home) { inclusive = true } }
                                        Tab.Playlists -> { }
                                        Tab.Search -> nav.navigate("search")
                                        Tab.Settings -> nav.navigate("settings")
                                    }
                                })
                            }
                        }
                    }
                    composable("settings") {
                        tab = Tab.Settings
                        Box(Modifier.fillMaxSize()) {
                            SettingsScreen(onBack = { nav.popBackStack() })
                            Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                                MiniPlayerHost(
                                    onExpand = { nav.navigate(Routes.NowPlaying) },
                                    onDismiss = { app.player.stopAndHide() },
                                    onSwipePrev = { app.player.prev() },
                                    onSwipeNext = { app.player.next() },
                                )
                                BottomTabBar(tab, onTab = { t ->
                                    tab = t
                                    when (t) {
                                        Tab.Library -> nav.navigate(Routes.Home) { popUpTo(Routes.Home) { inclusive = true } }
                                        Tab.Playlists -> nav.navigate("playlists")
                                        Tab.Search -> nav.navigate("search")
                                        Tab.Settings -> {}
                                    }
                                })
                            }
                        }
                    }
                    composable(Routes.NowPlaying) {
                        Box(Modifier.fillMaxSize().navigationBarsPadding()) {
                            NowPlayingScreen(
                                onCollapse = { nav.popBackStack() },
                                onOpenQueue = { showQueue = true },
                                onAddToPlaylist = { /* TODO */ },
                            )
                        }
                    }
                    composable(Routes.Album, arguments = listOf(androidx.navigation.navArgument("id") { type = NavType.LongType })) { entry ->
                        val id = entry.arguments?.getLong("id") ?: -1L
                        Box(Modifier.fillMaxSize()) {
                            AlbumDetailScreen(
                                albumId = id,
                                onBack = { nav.popBackStack() },
                                onGoToArtist = { name -> nav.navigate(Routes.artist(name)) },
                                onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                            )
                            Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                                MiniPlayerHost(
                                    onExpand = { nav.navigate(Routes.NowPlaying) },
                                    onDismiss = { app.player.stopAndHide() },
                                    onSwipePrev = { app.player.prev() },
                                    onSwipeNext = { app.player.next() },
                                )
                            }
                        }
                    }
                    composable(Routes.Artist) { entry ->
                        val name = java.net.URLDecoder.decode(entry.arguments?.getString("name") ?: "", "UTF-8")
                        Box(Modifier.fillMaxSize()) {
                            ArtistDetailScreen(
                                artistName = name,
                                onBack = { nav.popBackStack() },
                                onOpenAlbum = { id -> nav.navigate(Routes.album(id)) },
                                onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                            )
                            Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                                MiniPlayerHost(
                                    onExpand = { nav.navigate(Routes.NowPlaying) },
                                    onDismiss = { app.player.stopAndHide() },
                                    onSwipePrev = { app.player.prev() },
                                    onSwipeNext = { app.player.next() },
                                )
                            }
                        }
                    }
                    composable(Routes.Folder) { entry ->
                        val path = java.net.URLDecoder.decode(entry.arguments?.getString("path") ?: "", "UTF-8")
                        Box(Modifier.fillMaxSize()) {
                            FolderDetailScreen(
                                path = path,
                                onBack = { nav.popBackStack() },
                                onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                            )
                            Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                                MiniPlayerHost(
                                    onExpand = { nav.navigate(Routes.NowPlaying) },
                                    onDismiss = { app.player.stopAndHide() },
                                    onSwipePrev = { app.player.prev() },
                                    onSwipeNext = { app.player.next() },
                                )
                            }
                        }
                    }
                    composable(Routes.Playlist, arguments = listOf(androidx.navigation.navArgument("id") { type = NavType.LongType })) { entry ->
                        val id = entry.arguments?.getLong("id") ?: -1L
                        Box(Modifier.fillMaxSize()) {
                            PlaylistDetailScreen(
                                playlistId = id,
                                onBack = { nav.popBackStack() },
                                onNowPlaying = { nav.navigate(Routes.NowPlaying) },
                            )
                            Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                                MiniPlayerHost(
                                    onExpand = { nav.navigate(Routes.NowPlaying) },
                                    onDismiss = { app.player.stopAndHide() },
                                    onSwipePrev = { app.player.prev() },
                                    onSwipeNext = { app.player.next() },
                                )
                            }
                        }
                    }
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
                        onAddTracks = { /* TODO */ },
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerHost(onExpand: () -> Unit, onDismiss: () -> Unit, onSwipePrev: () -> Unit, onSwipeNext: () -> Unit) {
    MiniPlayer(
        onExpand = onExpand,
        onDismiss = onDismiss,
        onSwipePrev = onSwipePrev,
        onSwipeNext = onSwipeNext,
    )
}

@Composable
private fun BottomTabBar(current: Tab, onTab: (Tab) -> Unit) {
    val c = LocalDhunColors.current
    Row(Modifier.fillMaxWidth().background(c.bg).border(1.dp, c.border).height(58.dp)) {
        BottomTab(
            icon = Icons.Default.Home, label = "library", active = current == Tab.Library,
            onClick = { onTab(Tab.Library) },
        )
        BottomTab(
            icon = Icons.Default.PlaylistPlay, label = "playlists", active = current == Tab.Playlists,
            onClick = { onTab(Tab.Playlists) },
        )
        BottomTab(
            icon = Icons.Default.Search, label = "search", active = current == Tab.Search,
            onClick = { onTab(Tab.Search) },
        )
        BottomTab(
            icon = Icons.Default.Settings, label = "settings", active = current == Tab.Settings,
            onClick = { onTab(Tab.Settings) },
        )
    }
}

@Composable
private fun HomeScaffold(tab: Tab, onTabChange: (Tab) -> Unit, nav: androidx.navigation.NavHostController) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    Box(Modifier.fillMaxSize()) {
        LibraryScreen(
            onOpenNowPlaying = { nav.navigate(Routes.NowPlaying) },
            onOpenSearch = { nav.navigate("search"); onTabChange(Tab.Search) },
            onOpenAlbum = { id -> nav.navigate(Routes.album(id)) },
            onOpenArtist = { name -> nav.navigate(Routes.artist(name)) },
            onOpenFolder = { p -> nav.navigate(Routes.folder(p)) },
            onOpenSettings = { nav.navigate("settings"); onTabChange(Tab.Settings) },
        )
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
            MiniPlayerHost(
                onExpand = { nav.navigate(Routes.NowPlaying) },
                onDismiss = { app.player.stopAndHide() },
                onSwipePrev = { app.player.prev() },
                onSwipeNext = { app.player.next() },
            )
            BottomTabBar(tab, onTab = onTabChange)
        }
    }
}
