package com.dhun.app.ui.screens.splash

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.DhunApp
import com.dhun.app.ui.theme.LocalDhunColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(onReady: () -> Unit, onNeedPermission: () -> Unit) {
    val c = LocalDhunColors.current
    val app = DhunApp.instance
    val scan by app.library.scanProgress.collectAsState()
    var cursorVisible by remember { mutableStateOf(true) }

    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        rememberPermissionState(Manifest.permission.READ_MEDIA_AUDIO)
    else
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    LaunchedEffect(Unit) {
        while (true) { cursorVisible = !cursorVisible; delay(500) }
    }

    LaunchedEffect(perm.status.isGranted) {
        if (perm.status.isGranted) {
            app.library.scan()
        } else {
            onNeedPermission()
        }
    }

    LaunchedEffect(scan.done) {
        if (scan.done && scan.totalFound > 0) {
            delay(200)
            onReady()
        }
    }

    Box(Modifier.fillMaxSize().background(c.bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ASCII header
            androidx.compose.material.Text(
                "dhun${if (cursorVisible) "_" else " "}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = c.accent,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(8.dp))
            androidx.compose.material.Text(
                "offline music player",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = c.textSecondary,
            )
            Spacer(Modifier.height(36.dp))
            androidx.compose.material.Text(
                if (scan.totalFound == 0 && !scan.done) "scanning device..."
                else if (scan.done && scan.totalFound == 0) "[ no tracks found ]"
                else "scanning device... ${scan.totalFound} tracks",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = if (scan.done && scan.totalFound == 0) c.error else c.textMuted,
            )
            if (scan.totalFound > 0) {
                Spacer(Modifier.height(6.dp))
                androidx.compose.material.Text(
                    "indexed ${scan.scanned}/${scan.totalFound}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = c.textMuted,
                )
            }
        }
        androidx.compose.material.Text(
            "v0.1.0-offline",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = c.textMuted,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
        )
    }
}
