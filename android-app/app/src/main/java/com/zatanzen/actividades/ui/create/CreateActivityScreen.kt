package com.zatanzen.actividades.ui.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zatanzen.actividades.data.DemoData
import com.zatanzen.actividades.model.Category
import com.zatanzen.actividades.ui.AppViewModel
import com.zatanzen.actividades.ui.common.SectionTitle
import com.zatanzen.actividades.ui.common.formatRating
import kotlin.math.roundToInt

private val durations = listOf(60 to "1 h", 120 to "2 h", 180 to "3 h", 240 to "4 h")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActivityScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.CONVERSACION) }
    var topic by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(120) }
    var requirements by remember { mutableStateOf("") }
    var minRating by remember { mutableFloatStateOf(0f) }
    var excludeConflicts by remember { mutableStateOf(true) }
    var maxParticipants by remember { mutableFloatStateOf(6f) }

    val canPublish = title.isNotBlank() && placeName.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva actividad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(title, { title = it }, label = { Text("Título *") }, modifier = Modifier.fillMaxWidth())

            SectionTitle("Tipo de actividad")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(0.dp)) {
                items(Category.entries) { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = c },
                        label = { Text("${c.emoji} ${c.label}") },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(topic, { topic = it }, label = { Text("Tema (ej. economía, la final, Catan)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(placeName, { placeName = it }, label = { Text("Lugar (ej. Bar La Esquina) *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                description, { description = it },
                label = { Text("Descripción") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionTitle("Duración en el mapa")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                durations.forEach { (minutes, label) ->
                    FilterChip(selected = duration == minutes, onClick = { duration = minutes }, label = { Text(label) })
                }
            }

            SectionTitle("Requisitos")
            OutlinedTextField(
                requirements, { requirements = it },
                label = { Text("Uno por línea (ej. Trae cerveza)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            SectionTitle("Filtro de entrada")
            Text(
                if (minRating > 0f) "Calificación mínima: ★ ${formatRating(minRating.toDouble())}"
                else "Calificación mínima: ninguna"
            )
            Slider(value = minRating, onValueChange = { minRating = (it * 2).roundToInt() / 2f }, valueRange = 0f..5f, steps = 9)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { excludeConflicts = !excludeConflicts },
            ) {
                Text("Excluir personas con reportes de conflicto", modifier = Modifier.weight(1f))
                Switch(checked = excludeConflicts, onCheckedChange = { excludeConflicts = it })
            }

            SectionTitle("Cupo máximo: ${maxParticipants.roundToInt()} personas")
            Slider(value = maxParticipants, onValueChange = { maxParticipants = it }, valueRange = 2f..20f, steps = 17)

            Spacer(Modifier.height(8.dp))
            Text(
                "La actividad aparecerá en el mapa en tu ubicación actual.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = canPublish,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val activity = vm.repo.create(
                        title = title.trim(),
                        category = category,
                        topic = topic.trim().ifEmpty { category.label },
                        description = description.trim(),
                        placeName = placeName.trim(),
                        location = vm.userLocation.value ?: DemoData.DEFAULT_LOCATION,
                        durationMinutes = duration,
                        requirements = requirements.lines().map { it.trim() }.filter { it.isNotEmpty() },
                        minRating = minRating.toDouble(),
                        excludeConflicts = excludeConflicts,
                        maxParticipants = maxParticipants.roundToInt(),
                        now = vm.currentTime(),
                    )
                    onCreated(activity.id)
                },
            ) { Text("Publicar actividad") }
        }
    }
}
