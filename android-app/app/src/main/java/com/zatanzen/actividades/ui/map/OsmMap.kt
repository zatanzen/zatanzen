package com.zatanzen.actividades.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zatanzen.actividades.model.Activity
import com.zatanzen.actividades.model.LatLng
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMap(
    activities: List<Activity>,
    userLocation: LatLng?,
    onActivityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onClick by rememberUpdatedState(onActivityClick)

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(15.0)
        }
    }
    val icons = remember { mutableMapOf<String, Drawable>() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { mapView.controller.setCenter(GeoPoint(it.lat, it.lon)) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { mv ->
            mv.overlays.removeAll { it is Marker }
            userLocation?.let { loc ->
                mv.overlays.add(Marker(mv).apply {
                    position = GeoPoint(loc.lat, loc.lon)
                    icon = icons.getOrPut("me") { circleIcon(context, "🧍", 0xFF1E88E5.toInt()) }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ -> true }
                })
            }
            activities.forEach { activity ->
                mv.overlays.add(Marker(mv).apply {
                    position = GeoPoint(activity.location.lat, activity.location.lon)
                    title = activity.title
                    icon = icons.getOrPut(activity.category.name) {
                        circleIcon(context, activity.category.emoji, activity.category.color.toInt())
                    }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ ->
                        onClick(activity.id)
                        true
                    }
                })
            }
            mv.invalidate()
        },
    )
}

/** A round pin with a colored ring and an emoji in the middle. */
private fun circleIcon(context: Context, emoji: String, color: Int): Drawable {
    val density = context.resources.displayMetrics.density
    val size = (44 * density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val half = size / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    canvas.drawCircle(half, half, half, paint)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(half, half, half - 4 * density, paint)
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size * 0.5f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(emoji, half, half - (text.descent() + text.ascent()) / 2, text)
    return BitmapDrawable(context.resources, bitmap)
}
