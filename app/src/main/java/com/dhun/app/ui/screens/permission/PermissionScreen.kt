package com.dhun.app.ui.screens.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.app.components.TermButton
import com.dhun.app.ui.theme.LocalDhunColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onGranted: () -> Unit) {
    val c = LocalDhunColors.current
    val ctx = LocalContext.current
    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        rememberPermissionState(Manifest.permission.READ_MEDIA_AUDIO) {
            if (it) onGranted()
        }
    else
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (it) onGranted()
        }

    Box(Modifier.fillMaxSize().background(c.bg).padding(24.dp), contentAlignment = Alignment.Center) {
        Column {
            androidx.compose.material.Text(
                "> dhun requires storage access",
                fontFamily = FontFamily.Monospace,
                color = c.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(18.dp))
            androidx.compose.material.Text(
                "to scan local audio files on your device\n" +
                        "and build your music library.\n\n" +
                        "no network permission is requested.\n" +
                        "your files stay on your device.",
                fontFamily = FontFamily.Monospace,
                color = c.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(28.dp))
            if (perm.status.isGranted) {
                TermButton(label = "continue", accent = true, onClick = onGranted)
            } else if (perm.status.shouldShowRationale) {
                TermButton(label = "allow", accent = true, onClick = { perm.launchPermissionRequest() })
                Spacer(Modifier.height(8.dp))
                androidx.compose.material.Text(
                    "(tap allow when prompted)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = c.textMuted,
                )
            } else {
                TermButton(label = "allow", accent = true, onClick = { perm.launchPermissionRequest() })
                Spacer(Modifier.height(10.dp))
                TermButton(label = "open settings", small = true, onClick = {
                    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}"))
                    ctx.startActivity(i)
                })
            }
        }
    }
}
