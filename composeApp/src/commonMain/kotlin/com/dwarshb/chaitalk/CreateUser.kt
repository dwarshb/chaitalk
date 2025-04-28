package com.dwarshb.chaitalk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun CreateUserScreen(personaId: String,onPersonaCreated: (Persona?) -> Unit) {
    var personaName by remember { mutableStateOf(TextFieldValue("")) }
    var personaDescription by remember { mutableStateOf(TextFieldValue("")) }
    var personalityTrait by remember { mutableStateOf(TextFieldValue("")) }
    val database = FirebaseDatabase()
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Your own AI", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        // Persona Avatar Placeholder (can be replaced with Image Picker)
//        Image(
//            painter = painterResource(id = R.drawable.ic_person_placeholder), // Replace with actual image
//            contentDescription = "Persona Avatar",
//            modifier = Modifier.size(100.dp)
//        )

        Spacer(modifier = Modifier.height(16.dp))

        // Persona Name
        OutlinedTextField(
            value = personaName,
            onValueChange = { personaName = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
                .padding(16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { /* Move focus to next field */ })
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Personality Trait
        OutlinedTextField(
            value = personalityTrait,
            onValueChange = { personalityTrait = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
                .padding(16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { /* Move focus to next field */ })
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Persona Description
        OutlinedTextField(
            value = personaDescription,
            onValueChange = { personaDescription = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
                .padding(16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { /* Save persona */ })
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Create Persona Button
        Button(
            onClick = {
                val persona = Persona(
                    id =  personaId,
                    name = personaName.text,
                    description = personaDescription.text,
                    personality = personalityTrait.text,
                    personalityPrompt = "",
                    createdBy = Firebase.getCurrentUser()?.uid?:"",
                    createdAt = Clock.System.now().toEpochMilliseconds()
                )
                createPersona(database,coroutineScope,persona) {
                    if (it!=null) {
                        onPersonaCreated(it)
                    }
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Create Persona")
        }
    }
}

// Function to handle persona creation (e.g., save to the database, Firebase)
fun createPersona(database: FirebaseDatabase,coroutineScope: CoroutineScope,
                  persona: Persona, onPersonaCreated: (Persona?)->Unit) {
    // Here you can save the persona data to Firebase, database, etc.
    // For example, send a POST request to save the persona data
    // Note: You could later update this with real-time data handling or session management
    coroutineScope.launch {
        val params = HashMap<String,Any>()
        params.put(persona.id,persona)
        database.patchFirebaseDatabase(listOf("persona"),params,
            object : onCompletion<String>{
                override fun onError(e: Exception) {
                    e.printStackTrace()
                }

                override fun onSuccess(t: String) {
                    println(t)
                    onPersonaCreated(persona)
                }
            })
    }
}