package com.zatanzen.actividades.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zatanzen.actividades.model.UserStats
import com.zatanzen.actividades.ui.AppViewModel
import com.zatanzen.actividades.ui.common.Avatar
import com.zatanzen.actividades.ui.common.SectionTitle
import com.zatanzen.actividades.ui.common.formatDate
import com.zatanzen.actividades.ui.common.formatRating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: AppViewModel,
    userId: String,
    onBack: () -> Unit,
    onActivityClick: (String) -> Unit,
) {
    val users by vm.repo.users.collectAsStateWithLifecycle()
    val activities by vm.repo.activities.collectAsStateWithLifecycle()
    val reviews by vm.repo.reviews.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val user = users[userId]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userId == vm.repo.currentUserId) "Mi perfil" else "Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (user == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Usuario no encontrado.")
            }
            return@Scaffold
        }
        val stats = UserStats.from(reviews, userId)
        val history = activities.filter { userId in it.participantIds }.sortedByDescending { it.startsAt }
        val received = reviews.filter { it.toUserId == userId }.sortedByDescending { it.createdAt }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Avatar(user.name, Modifier.size(64.dp))
                    Column {
                        Text(user.name, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            stats.rating?.let { "★ ${formatRating(it)} (${stats.reviewCount} reseñas)" } ?: "Sin calificaciones todavía",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            if (stats.conflictReports == 0) "Sin reportes de conflicto"
                            else "⚠ ${stats.conflictReports} reporte(s) de conflicto",
                            color = if (stats.conflictReports == 0) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.error,
                        )
                    }
                }
                if (user.bio.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(user.bio)
                }
            }

            item { SectionTitle("Actividades (${history.size})") }
            if (history.isEmpty()) item { Text("Todavía no ha participado en actividades.") }
            items(history, key = { "a-" + it.id }) { activity ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onActivityClick(activity.id) },
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${activity.category.emoji} ${activity.title}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            (if (activity.isActive(now)) "En curso · " else "") +
                                (if (activity.hostId == userId) "Anfitrión · " else "") +
                                formatDate(activity.startsAt),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            item { SectionTitle("Opiniones recibidas") }
            if (received.isEmpty()) item { Text("Sin opiniones todavía.") }
            items(received, key = { "r-" + it.id }) { review ->
                val author = users[review.fromUserId]?.name ?: "Alguien"
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text("★".repeat(review.stars) + "☆".repeat(5 - review.stars) + "  $author", style = MaterialTheme.typography.titleSmall)
                    Text("${review.activityTitle} · ${formatDate(review.createdAt)}", style = MaterialTheme.typography.bodySmall)
                    if (review.comment.isNotBlank()) Text(review.comment)
                    if (review.conflict) Text("⚠ Reportó un conflicto", color = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider()
            }
        }
    }
}
