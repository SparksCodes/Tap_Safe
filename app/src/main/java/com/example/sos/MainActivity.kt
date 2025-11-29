package com.example.sos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.sos.ui.components.Header
import com.example.sos.ui.components.SwipeableContactCard
import com.example.sos.ui.screens.NewContactScreen
import com.example.sos.ui.theme.SOSTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File

private const val CONTACTS_FILE_NAME = "contacts.json"

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Cargar contactos iniciales (archivo interno o assets)
        val initialContacts = loadContacts(this)

        setContent {
            SOSTheme {

                val activity = this@MainActivity

                // Estado: mostrar o no el formulario
                val showForm = remember { mutableStateOf(false) }

                // Estado: contacto que se está editando (null = nuevo)
                val editingContact = remember { mutableStateOf<Contact?>(null) }

                // Lista observable
                val contacts = remember {
                    mutableStateListOf<Contact>().apply {
                        addAll(initialContacts)
                    }
                }

                if (showForm.value) {
                    // Pantalla de nuevo/editar contacto
                    NewContactScreen(
                        initialContact = editingContact.value,
                        onSave = { name, phone, icon ->

                            val currentEditing = editingContact.value

                            if (currentEditing == null) {
                                // ➕ NUEVO CONTACTO
                                val nextId = (contacts.maxOfOrNull { it.id } ?: 0) + 1
                                val newContact = Contact(
                                    id = nextId,
                                    name = name,
                                    phone = phone,
                                    icon = icon
                                )
                                contacts.add(newContact)
                            } else {
                                // ✏️ EDITAR CONTACTO EXISTENTE
                                val index = contacts.indexOfFirst { it.id == currentEditing.id }
                                if (index != -1) {
                                    contacts[index] = currentEditing.copy(
                                        name = name,
                                        phone = phone,
                                        icon = icon
                                    )
                                }
                            }

                            // guardar JSON y salir del formulario
                            saveContacts(activity, contacts)
                            editingContact.value = null
                            showForm.value = false
                        },
                        onCancel = { showForm.value = false }
                    )
                } else {
                    // Pantalla principal
                    Scaffold(
                        topBar = {
                            Header(
                                onAddClick = {
                                    // Nuevo contacto
                                    editingContact.value = null
                                    showForm.value = true
                                }
                            )
                        }
                    ) { innerPadding ->

                        LazyColumn(
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            items(
                                items = contacts,
                                key = { it.id }
                            ) { contact ->

                                SwipeableContactCard(
                                    contact = contact,
                                    onCall = {
                                        val numberUri = Uri.parse("tel:${contact.phone}")
                                        val callIntent =
                                            Intent(Intent.ACTION_CALL, numberUri)

                                        if (ActivityCompat.checkSelfPermission(
                                                activity,
                                                Manifest.permission.CALL_PHONE
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            activity.startActivity(callIntent)
                                        } else {
                                            ActivityCompat.requestPermissions(
                                                activity,
                                                arrayOf(Manifest.permission.CALL_PHONE),
                                                1001
                                            )
                                        }
                                    },
                                    onEdit = {
                                        // 👉 Ponemos este contacto en edición y mostramos el formulario
                                        editingContact.value = contact
                                        showForm.value = true
                                    },
                                    onDelete = {
                                        val index =
                                            contacts.indexOfFirst { it.id == contact.id }
                                        if (index != -1) {
                                            contacts.removeAt(index)
                                            saveContacts(activity, contacts)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Funciones para cargar/guardar JSON ---------- */

private fun loadContacts(activity: ComponentActivity): List<Contact> {
    val gson = Gson()
    val type = object : TypeToken<List<Contact>>() {}.type
    val file = File(activity.filesDir, CONTACTS_FILE_NAME)

    // 1) Si el archivo NO existe todavía → lo creamos vacío y devolvemos lista vacía
    if (!file.exists()) {
        file.writeText("[]")      // JSON de lista vacía
        return emptyList()
    }

    // 2) Leemos el contenido
    val json = file.readText()

    // 3) Si por lo que sea está vacío o solo espacios → lista vacía
    if (json.isBlank()) {
        return emptyList()
    }

    // 4) Intentamos parsear; si falla, también devolvemos lista vacía
    return try {
        gson.fromJson<List<Contact>>(json, type) ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}


private fun saveContacts(activity: ComponentActivity, contacts: List<Contact>) {
    try {
        val gson = Gson()
        val json = gson.toJson(contacts)
        val file = File(activity.filesDir, CONTACTS_FILE_NAME)
        file.writeText(json)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

