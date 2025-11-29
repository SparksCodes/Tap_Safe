package com.example.sos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sos.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewContactScreen(
    initialContact: Contact? = null,   // null = nuevo, no null = editar
    onSave: (name: String, phone: String, icon: String) -> Unit,
    onCancel: () -> Unit               // 👉 nuevo callback
) {
    val isEditing = initialContact != null

    var icon by remember { mutableStateOf(initialContact?.icon ?: "") }
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var phone by remember { mutableStateOf(initialContact?.phone ?: "") }

    val isValid = name.isNotBlank() && phone.isNotBlank() // 👉 Validación

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isEditing) "Editar contacto SOS" else "Nuevo contacto SOS",
            style = MaterialTheme.typography.titleLarge
        )

        TextField(
            value = icon,
            onValueChange = { icon = it },
            label = { Text("Icono (emoji o texto)") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre *") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono *") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Botones alineados uno al lado del otro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedButton(
                onClick = { onCancel() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    if (isValid) {
                        onSave(name.trim(), phone.trim(), icon.trim())
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = isValid // 👉 Solo habilitado si hay datos válidos
            ) {
                Text(if (isEditing) "Guardar" else "Crear")
            }
        }
    }
}





