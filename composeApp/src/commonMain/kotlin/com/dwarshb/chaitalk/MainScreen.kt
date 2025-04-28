package com.dwarshb.chaitalk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainScreen(personaCreateRequest: (String)->Unit) {
    var showContent by remember { mutableStateOf(false) }
    var personaId by remember { mutableStateOf("") }

    var firebaseData = FirebaseDatabase()
    var coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally)
    {
        Image(
            painterResource(Res.drawable.home_screen_logo), ""
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
                firebaseData.readFirebaseDatabase(listOf("personasId"), "",
                    object : onCompletion<String> {
                        override fun onSuccess(t: String) {
                            println(t)
                            if (t == "null") {
                                showContent = !showContent
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
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("This personaId doesn't exist")
                Button(onClick = {
                    personaCreateRequest(personaId)
                }) {
                    Text("Create Persona")
                }
            }
        }
    }
}