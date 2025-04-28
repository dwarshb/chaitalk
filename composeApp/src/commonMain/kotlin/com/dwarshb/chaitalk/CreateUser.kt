package com.dwarshb.chaitalk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.onCompletion
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun CreateUserScreen(personaId: String,onPersonaCreated: (Persona?) -> Unit) {
    var id by remember { mutableStateOf(TextFieldValue(personaId)) }
    var personaName by remember { mutableStateOf(TextFieldValue("")) }
    var personaDescription by remember { mutableStateOf(TextFieldValue("")) }
    var personalityTrait by remember { mutableStateOf(TextFieldValue("")) }
    val avatars = remember { (1..12).map { "https://avatar.iran.liara.run/public/$it" } }
    val selectedAvatar = remember { mutableStateOf<String?>(null) }

    var expanded by remember { mutableStateOf(false) }
    var selectedPersonality by remember { mutableStateOf("Friendly") }
    val personalities = listOf("Friendly", "Professional", "Humorous", "Empathetic", "Creative")

    val database = FirebaseDatabase()
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Your Persona", fontSize = 24.sp,
            color = MaterialTheme.colors.primary)

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(8.dp).height(200.dp)
        ) {
            items(avatars.size) { index ->
                val avatarUrl = avatars[index]
                println(avatarUrl)
                KamelImage(
                    resource = {asyncPainterResource(avatarUrl)},
                    contentDescription = "" ,
                    onFailure = {
                        it.printStackTrace()
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp)
                        .clickable { selectedAvatar.value = avatarUrl }
                        .then(
                            if (selectedAvatar.value == avatarUrl)
                                Modifier.border(2.dp, Color.Green, CircleShape)
                            else Modifier
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Persona Avatar Placeholder (can be replaced with Image Picker)
//        Image(
//            painter = painterResource(id = R.drawable.ic_person_placeholder), // Replace with actual image
//            contentDescription = "Persona Avatar",
//            modifier = Modifier.size(100.dp)
//        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = id,
            label = { Text("Persona Id") },
            onValueChange = { id = it },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { /* Move focus to next field */ })
        )

        // Persona Name
        OutlinedTextField(
            value = personaName,
            label = { Text("Name") },
            onValueChange = { personaName = it },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { /* Move focus to next field */ })
        )

        // Personality Trait
        Box {
            OutlinedTextField(
                value = selectedPersonality,
                onValueChange = {},
                label = { Text("Personality") },
                readOnly = true,
                modifier = Modifier
                    .clickable { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                personalities.forEach { personality ->
                    DropdownMenuItem(onClick = {
                        selectedPersonality = personality
                        expanded = false
                    }) {
                        Text(text = personality)
                    }
                }
            }
        }

        // Persona Description
        OutlinedTextField(
            value = personaDescription,
            label = { Text("Description") },
            onValueChange = { personaDescription = it },
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