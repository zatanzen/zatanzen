package com.zatanzen.actividades.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zatanzen.actividades.model.User
import com.zatanzen.actividades.model.UserStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val spanish: Locale = Locale.forLanguageTag("es")

fun formatRating(value: Double): String = String.format(Locale.US, "%.1f", value)

fun formatRemaining(ms: Long): String {
    val totalMin = (ms / 60_000).coerceAtLeast(0)
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "$h h $m min" else "$m min"
}

fun formatDate(ms: Long): String = SimpleDateFormat("d MMM yyyy, HH:mm", spanish).format(Date(ms))

fun formatTime(ms: Long): String = SimpleDateFormat("HH:mm", spanish).format(Date(ms))

fun statsLine(stats: UserStats): String {
    val rating = stats.rating?.let { "★ ${formatRating(it)} · ${stats.reviewCount} reseñas" } ?: "Sin calificaciones"
    return if (stats.conflictReports > 0) "$rating · ⚠ ${stats.conflictReports} reporte(s)" else rating
}

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = modifier.size(40.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun UserRow(
    user: User,
    stats: UserStats,
    onClick: () -> Unit,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(user.name)
        Column(Modifier.weight(1f)) {
            Text(user.name + (subtitle?.let { " · $it" } ?: ""), style = MaterialTheme.typography.bodyLarge)
            Text(statsLine(stats), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 20.dp, bottom = 6.dp),
    )
}
