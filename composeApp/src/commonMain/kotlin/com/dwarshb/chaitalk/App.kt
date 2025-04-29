package com.dwarshb.chaitalk


import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.cash.sqldelight.db.SqlDriver
import com.dwarshb.chaitalk.authentication.AuthenticationView
import com.dwarshb.chaitalk.authentication.AuthenticationViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseUser
import com.dwarshb.firebase.onCompletion

enum class Screen {
    Initial,CreateUser,Chat,Auth
}

@Composable
@Preview
fun App(sqlDriver: SqlDriver? = null,navController: NavHostController = rememberNavController()) {
    MaterialTheme {

        val firebase = Firebase()
        firebase.initialize(apiKey = API_KEY, databaseUrl = DATABASE_URL,storageUrl = STORAGE_URL)
        firebase.setGemini("gemini-2.0-flash",GEMINI_API_KEY)

        var personaId by remember { mutableStateOf("") }
        var persona by remember { mutableStateOf<Persona?>(null) }

        var firebaseUser by remember { mutableStateOf(FirebaseUser("", "", "")) }
        val authenticationViewModel = viewModel { AuthenticationViewModel(firebase, sqlDriver) }
        authenticationViewModel.checkSession(object : onCompletion<User> {
            override fun onSuccess(T: User) {
                firebaseUser = FirebaseUser(
                    T.email, T.idToken, T.localId.toString()
                )
                authenticationViewModel.firebase?.setCurrentUser(firebaseUser)
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
            }
        })

        NavHost(
            navController = navController,
            startDestination = Screen.Initial.name
        ) {
            composable(route = Screen.Initial.name) {
                MainScreen(
                    onChatWithPersona = {
                        persona = it
                        navController.navigate(Screen.Chat.name)
                    },
                    personaCreateRequest = {
                    personaId = it
                    if(firebaseUser.idToken.isEmpty()) {
                        navController.navigate(Screen.Auth.name)
                    } else {
                        navController.navigate(Screen.CreateUser.name)
                    }
                })
            }
            composable(route = Screen.CreateUser.name) {
               CreateUserScreen(personaId) {
                   persona = it
                   navController.navigate(Screen.Chat.name)
               }

            }
            composable(route = Screen.Chat.name) {
                persona?.let { ChatScreen(it) }
            }
            composable(route = Screen.Auth.name) {
                AuthenticationView(authenticationViewModel,
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        firebaseUser = it
                        navController.navigate(Screen.CreateUser.name)
                    })
            }
        }
    }
}