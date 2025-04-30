package com.dwarshb.chaitalk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chaitalk.composeapp.generated.resources.Res
import chaitalk.composeapp.generated.resources.home_screen_logo
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.FirebaseUser
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainScreen(firebaseUser: FirebaseUser,
               selectedModel: MutableState<String>,
               openRecentChats: ()->Unit,
               personaCreateRequest: (String)->Unit,
               onChatWithPersona: (Persona)->Unit) {
    var showContent by remember { mutableStateOf(false) }
    var personaId by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val models = listOf("gemini-2.0-flash",
        "gemini-2.0-flash-lite",
        "gemini-1.5-flash",
        "gemini-1.5-pro",
        "gemini-2.5-flash-preview-04-17")

    var firebaseData = FirebaseDatabase()
    var coroutineScope = rememberCoroutineScope()
    Box(modifier =  Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center) // Center it horizontally
            ) {
                Button(onClick = { expanded = true }) {
                    Text(selectedModel.value)
                }
                DropdownMenu(
                    modifier = Modifier.align(Alignment.Center),
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            onClick = {
                                selectedModel.value = model
                                expanded = false
                            }, text = {
                                Text(text = model)
                            })
                    }
                }
            }
            Image(
                painterResource(Res.drawable.home_screen_logo), "",
                modifier = Modifier.width(500.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = personaId,
                onValueChange = { personaId = it },
                label = { Text("Enter personaId") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                coroutineScope.launch {
                    firebaseData.readFirebaseDatabase(listOf("personas", personaId), "",
                        object : onCompletion<String> {
                            override fun onSuccess(t: String) {
                                println(t)
                                if (t == "null") {
                                    showContent = !showContent
                                } else {
                                    val persona =
                                        Json { ignoreUnknownKeys = true }.decodeFromString<Persona>(
                                            t
                                        )
                                    onChatWithPersona(persona)
                                }
                            }

                            override fun onError(e: Exception) {
                                e.printStackTrace()
                            }
                        })
                }
            }) {
                Text("Chat Now!")
            }
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("This personaId doesn't exist")
                    Button(onClick = {
                        personaCreateRequest(personaId)
                    }) {
                        Text("Create Persona")
                    }
                }
            }
        }
        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            if (firebaseUser.idToken.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { openRecentChats() }
                ) {
                    Icon(Icons.AutoMirrored.Default.Message, "Recent Conversation")
                }
            }
        }
    }
}