package com.example.weatherbyagendaandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.weatherbyagendaandroid.presentation.composable.MapViewComposableWrapper
import dagger.hilt.android.AndroidEntryPoint
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class MapActivity : ComponentActivity() {

    private fun copyPmTilesFromAssets(): File {
        val context = this
        val outFile = File(context.filesDir, "us-only.pmtiles")

        if (!outFile.exists()) {
            context.assets.open( "us-only.pmtiles").use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        return outFile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapLibre.getInstance(this)
            copyPmTilesFromAssets()

            MapViewComposableWrapper(Modifier.fillMaxSize()){ map ->
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(44.8113, -91.4985))
                    .zoom(7.0)
                    .build()
            }
        }
    }
}