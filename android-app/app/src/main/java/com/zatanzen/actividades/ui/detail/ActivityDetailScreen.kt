package com.zatanzen.actividades.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zatanzen.actividades.model.Activity
import com.zatanzen.actividades.model.JoinCheck
import com.zatanzen.actividades.model.User
import com.zatanzen.actividades.model.UserStats
import com.zatanzen.actividades.model.checkJoin
import com.zatanzen.actividades.ui.AppViewModel
import com.zatanzen.actividades.ui.common.RateDialog
import com.zatanzen.actividades.ui.common.SectionTitle
import com.zatanzen.actividades.ui.common.UserRow
import com.zatanzen.actividades.ui.common.formatDate
import com.zatanzen.actividades.ui.common.formatRating
import com.zatanzen.actividades.ui.common.formatRemaining
import com.zatanzen.actividades.ui.common.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    vm: AppViewModel,
    activityId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
) {
    val activities by vm.repo.activities.collectAsStateWithLifecycle()
    val users by vm.repo.users.collectAsStateWithLifecycle()
    val reviews by vm.repo.reviews.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()
    val activity = activities.find { it.id == activityId }
    var rateTarget by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(activity?.title ?: "Actividad", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (activity == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Esta actividad ya no existe.")
            }
            return@Scaffold
        }

        val me = vm.repo.currentUserId
        val myStats = UserStats.from(reviews, me)
        val check = checkJoin(activity, me, myStats, now)
        val active = activity.isActive(now)
        val participants = activity.participantIds.mapNotNull { users[it] }
        val iParticipated = me in activity.participantIds

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
        ) {
            item { Header(activity, active, now) }

            item {
                SectionTitle("Requisitos")
                if (activity.requirements.isEmpty()) {
                    Text("Sin requisitos especiales.")
                } else {
                    activity.requirements.forEach { Text("• $it") }
                }
            }

            item {
                SectionTitle("Filtro de entrada")
                Text(
                    if (activity.minRating > 0) "Calificación mínima: ★ ${formatRating(activity.minRating)}"
                    else "Abierta a cualquier calificación"
                )
                if (activity.excludeConflicts) {
                    Text("No se admiten personas con reportes de conflicto")
                }
            }

            item { SectionTitle("Participantes (${participants.size}/${activity.maxParticipants})") }

            items(participants, key = { it.id }) { user ->
                val canRate = !active && iParticipated && user.id != me &&
                    reviews.none { it.fromUserId == me && it.toUserId == user.id && it.activityId == activity.id }
                UserRow(
                    user = user,
                    stats = UserStats.from(reviews, user.id),
                    onClick = { onUserClick(user.id) },
                    subtitle = if (user.id == activity.hostId) "Anfitrión" else null,
                    trailing = if (canRate) {
                        { TextButton(onClick = { rateTarget = user }) { Text("Calificar") } }
                    } else null,
                )
            }

            item {
                Spacer(Modifier.height(20.dp))
                JoinSection(
                    check = check,
                    isHost = activity.hostId == me,
                    active = active,
                    onJoin = { vm.repo.join(activity.id, vm.currentTime()) },
                    onLeave = { vm.repo.leave(activity.id) },
                )
            }
        }

        rateTarget?.let { target ->
            RateDialog(
                personName = target.name,
                onDismiss = { rateTarget = null },
                onSubmit = { stars, comment, conflict ->
                    vm.repo.addReview(target.id, activity, stars, comment, conflict, vm.currentTime())
                    rateTarget = null
                },
            )
        }
    }
}

@Composable
private fun Header(activity: Activity, active: Boolean, now: Long) {
    Text(
        "${activity.category.emoji} ${activity.category.label}",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(activity.title, style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(4.dp))
    Text("Tema: ${activity.topic}", style = MaterialTheme.typography.bodyLarge)
    Text("📍 ${activity.placeName}")
    Text(
        if (active) "🕒 ${formatTime(activity.startsAt)} – ${formatTime(activity.endsAt)} · termina en ${formatRemaining(activity.endsAt - now)}"
        else "🕒 Terminó el ${formatDate(activity.endsAt)}"
    )
    if (activity.description.isNotBlank()) {
        Spacer(Modifier.height(12.dp))
        Text(activity.description)
    }
}

@Composable
private fun JoinSection(
    check: JoinCheck,
    isHost: Boolean,
    active: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
) {
    when (check) {
        JoinCheck.Ok -> Button(onClick = onJoin, modifier = Modifier.fillMaxWidth()) { Text("Unirme") }
        JoinCheck.AlreadyJoined -> when {
            isHost -> Notice("Eres el anfitrión de esta actividad.")
            !active -> Notice("Participaste en esta actividad. Puedes calificar a los demás.")
            else -> {
                Notice("Ya estás dentro. ¡Disfruta!")
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) { Text("Salir de la actividad") }
            }
        }
        JoinCheck.Ended -> Notice("Esta actividad ya terminó.")
        JoinCheck.Full -> Notice("El cupo está completo.")
        is JoinCheck.RatingTooLow -> Notice(
            check.actual?.let {
                "No puedes unirte: tu calificación (★ ${formatRating(it)}) es menor a la mínima requerida (★ ${formatRating(check.required)})."
            } ?: "No puedes unirte: necesitas calificaciones de otras actividades (mínimo ★ ${formatRating(check.required)})."
        )
        is JoinCheck.HasConflictReports -> Notice(
            "No puedes unirte: esta actividad no admite personas con reportes de conflicto (tienes ${check.count})."
        )
    }
}

@Composable
private fun Notice(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(text, modifier = Modifier.padding(16.dp))
    }
}
