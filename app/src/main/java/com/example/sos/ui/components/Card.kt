package com.example.sos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.sos.Contact

/******************************
 *   CARD NORMAL (SIN SWIPE)  *
 ******************************/
@Composable
fun ContactCard(
    name: String,
    phone: String,
    iconText: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ICONO GRANDE EN CÍRCULO
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontSize = 42.sp,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = name.toUpperCase(Locale.current),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = phone,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}


/********************************************
 * CARD CON SWIPE + LONG PRESS + DIÁLOGO    *
 ********************************************/
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableContactCard(
    contact: Contact,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Estado del Swipe
    val dismissState = rememberDismissState(
        confirmStateChange = { value ->
            when (value) {
                DismissValue.DismissedToEnd -> {     // 👉 izq → dcha (Editar)
                    onEdit()
                    false
                }
                DismissValue.DismissedToStart -> {   // 👉 dcha → izq (Llamar)
                    onCall()
                    false
                }
                else -> false
            }
        }
    )

    // Diálogo de confirmación al mantener pulsado
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar contacto") },
            text = { Text("¿Quieres borrar este contacto?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("No") }
            }
        )
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(
            DismissDirection.StartToEnd,   // Editar
            DismissDirection.EndToStart    // Llamar
        ),
        background = { /* Opcional: fondo según swipe */ },
        dismissContent = {
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier.combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { /* Click normal si necesitas algo luego */ },
                    onLongClick = { showDeleteDialog = true } // Mantener pulsado → Borrar
                )
            ) {
                ContactCard(
                    name = contact.name,
                    phone = contact.phone,
                    iconText = contact.icon
                )
            }
        }
    )
}
