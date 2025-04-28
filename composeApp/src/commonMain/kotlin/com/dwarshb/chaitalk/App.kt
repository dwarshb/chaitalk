package com.dwarshb.chaitalk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import chaitalk.composeapp.generated.resources.Res
import chaitalk.composeapp.generated.resources.compose_multiplatform
import chaitalk.composeapp.generated.resources.home_screen_logo
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.launch

enum class Screen {
    Initial,CreateUser,Chat
}

@Composable
@Preview
fun App(navController: NavHostController = rememberNavController()) {
    MaterialTheme {

        val firebase = Firebase()
        firebase.initialize(apiKey = API_KEY, databaseUrl = DATABASE_URL,storageUrl = STORAGE_URL)
        firebase.setGemini("gemini-2.0-flash",GEMINI_API_KEY)

        var personaId by remember { mutableStateOf("") }
        var persona by remember { mutableStateOf<Persona?>(null) }

        NavHost(
            navController = navController,
            startDestination = Screen.Initial.name
        ) {
            composable(route = Screen.Initial.name) {
                MainScreen{
                    personaId = it
                    navController.navigate(Screen.CreateUser.name)
                }
            }
            composable(route = Screen.CreateUser.name) {
                CreateUserScreen(personaId) {
                    persona = it
                    navController.navigate(Screen.Chat.name)
                }
            }
            composable(route = Screen.Chat.name) {
                persona?.let { ChatScreen(personaId, it) }
            }
        }
    }
}