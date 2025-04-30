package com.dwarshb.chaitalk


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.cash.sqldelight.db.SqlDriver
import com.dwarshb.chaitalk.authentication.AuthenticationView
import com.dwarshb.chaitalk.authentication.AuthenticationViewModel
import com.dwarshb.chaitalk.chat.ChatScreen
import com.dwarshb.chaitalk.personasView.MyPersonas
import com.dwarshb.chaitalk.recentChats.RecentChats
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseUser
import com.dwarshb.firebase.onCompletion

enum class Screen {
    Initial,CreateUser,Chat,Auth,RecentChats,UserPersonas
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(sqlDriver: SqlDriver? = null,navController: NavHostController = rememberNavController()) {
    MaterialTheme {

        val firebase = Firebase()
        var geminiModel : MutableState<String> = remember { mutableStateOf("gemini-2.0-flash") }

        firebase.initialize(apiKey = firebaseAPIKey.value,
            databaseUrl = firebaseDatabaseUrl.value,storageUrl = STORAGE_URL)
        firebase.setGemini(geminiModel.value,geminiKey.value)

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
                MainScreen(firebaseUser,
                    firebaseAPIKey = firebaseAPIKey,
                    firebaseDatabaseUrl = firebaseDatabaseUrl ,
                    geminiKey = geminiKey,
                    geminiModel,
                    openUserPersonas = {
                        navController.navigate(Screen.UserPersonas.name)
                    },
                    openRecentChats = {
                        navController.navigate(Screen.RecentChats.name)
                    },
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
                if (firebaseUser.idToken.isEmpty()) {
                    val openDialog = remember { mutableStateOf(true) }
                    if (openDialog.value) {
                        BasicAlertDialog(onDismissRequest = {}) {
                            Surface(
                                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = AlertDialogDefaults.TonalElevation
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Your Chat History wont be saved, Since you are not loggedIn")
                                    Button(onClick = { openDialog.value = false}) {
                                        Text("Ok")
                                    }
                                }
                            }
                        }
                    }
                }
                persona?.let { ChatScreen(it) {
                    navController.popBackStack()
                } }
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
            composable(route = Screen.RecentChats.name) {
                RecentChats(onBackPressed = { navController.popBackStack() },
                    onChatSelected = {
                        persona = it
                        navController.navigate(Screen.Chat.name)
                    })
            }
            composable(route = Screen.UserPersonas.name) {
                MyPersonas {
                    navController.popBackStack()
                }
            }
        }
    }
}