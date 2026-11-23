package com.zatanzen.actividades.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RateDialog(
    personName: String,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, comment: String, conflict: Boolean) -> Unit,
) {
    var stars by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var conflict by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar a $personName") },
        text = {
            Column {
                Text("¿Cómo se comportó en la actividad?")
                Row(Modifier.padding(vertical = 8.dp)) {
                    for (i in 1..5) {
                        Text(
                            "★",
                            fontSize = 36.sp,
                            color = if (i <= stars) Color(0xFFFFB300) else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .clickable { stars = i }
                                .padding(horizontal = 2.dp),
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario (opcional)") },
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { conflict = !conflict }) {
                    Checkbox(checked = conflict, onCheckedChange = { conflict = it })
                    Text("Reportar un conflicto con esta persona")
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(stars, comment.trim(), conflict) }) { Text("Enviar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
