package com.dwarshb.chaitalk.authentication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chaitalk.composeapp.generated.resources.Res
import chaitalk.composeapp.generated.resources.confirm_password
import chaitalk.composeapp.generated.resources.create_new_account
import chaitalk.composeapp.generated.resources.email
import chaitalk.composeapp.generated.resources.home_screen_logo
import chaitalk.composeapp.generated.resources.login_account_title
import chaitalk.composeapp.generated.resources.next
import chaitalk.composeapp.generated.resources.password
import chaitalk.composeapp.generated.resources.sign_up_title
import com.dwarshb.firebase.FirebaseUser
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthenticationView(viewModel : AuthenticationViewModel,
                       onBackPressed: () -> Unit,
                       onSuccess: (FirebaseUser) -> Unit) {
    val signUpTitleStr = stringResource(Res.string.sign_up_title)
    val createAccountStr = stringResource(Res.string.create_new_account)
    val loginAccountStr = stringResource(Res.string.login_account_title)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var title by remember { mutableStateOf(signUpTitleStr) }
    var signIn by remember { mutableStateOf(true) }
    var emailValid by remember { mutableStateOf(true) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var nameValid by remember { mutableStateOf(true) }
    var phoneValid by remember { mutableStateOf(true) }
    var signInText by remember { mutableStateOf(createAccountStr) }
    var openDialog by remember { mutableStateOf(false) }
    var openDialogTitle by remember { mutableStateOf("") }
    var openDialogText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var firebaseToken by remember { mutableStateOf("") }

    Column {
        Card(modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { onBackPressed() },
                    modifier = Modifier.align(Alignment.Start)) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, "")
                }
                Image(painterResource(Res.drawable.home_screen_logo),"",
                    modifier = Modifier.size(150.dp))
                Text(
                    text = title,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                )

                OutlinedTextField(
                    value = email,
                    isError = !emailValid,
                    onValueChange = { email = it },
                    label = { Text(stringResource(Res.string.email)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    isError = passwordError,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(
                    visible = !signIn,
                    enter = fadeIn(initialAlpha = 0.4f),
                    exit = fadeOut(animationSpec = tween(250))
                ) {
                    OutlinedTextField(
                        value = confirmPassword,
                        isError = confirmPasswordError,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(Res.string.confirm_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
//                if(signIn) {
//                    Spacer(modifier = Modifier.height(24.dp))
//                    Text(
//                        stringResource(Res.string.forgot_password),
//                        modifier = Modifier.align(Alignment.End)
//                            .clickable {
//                                //TODO(ForgotPassword
//                            })
//                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (email.isEmpty())
                            emailValid = false
                        else if (password.isEmpty())
                            passwordError = true
                        when (signIn) {
                            true -> {
                                emailValid = viewModel.validateEmail(email)
                                if (emailValid && !passwordError) {
                                    coroutineScope.launch {
                                        viewModel.login(
                                            email,
                                            password,
                                            object : onCompletion<String> {
                                                override fun onSuccess(token: String) {
                                                    openDialogTitle = "Login Success"
                                                    openDialogText = "Token: ${token}"
                                                    firebaseToken = token
                                                    openDialog = true
                                                }

                                                override fun onError(e: Exception) {
                                                    openDialogTitle = "Error"
                                                    openDialogText = e.message.toString()
                                                    openDialog = true
                                                }
                                            })
                                    }
                                }
                            }

                            false -> {
                                emailValid = viewModel.validateEmail(email)
                                confirmPasswordError = confirmPassword.isEmpty() && confirmPassword==password
                                if (emailValid && !passwordError && !confirmPasswordError && nameValid && phoneValid) {
                                    coroutineScope.launch {
                                        viewModel.signUp(
                                            email,
                                            password,
                                            confirmPassword,
                                            name,
                                            phoneNumber,
                                            object : onCompletion<String> {
                                                override fun onSuccess(token: String) {
                                                    openDialogTitle = "Account Created Successfully"
                                                    openDialogText = "Token: ${token}"
                                                    firebaseToken = token
                                                    openDialog = true
                                                }

                                                override fun onError(e: Exception) {
                                                    openDialogTitle = "Error"
                                                    openDialogText = e.message.toString()
                                                    openDialog = true
                                                }
                                            })
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.next))
                }
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = AnnotatedString(signInText),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .clickable {
                            if (signIn) {
                                signIn = false
                                title = createAccountStr
                                signInText = loginAccountStr
                            } else {
                                signIn = true
                                title = signUpTitleStr
                                signInText = createAccountStr
                            }
                        },
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (openDialog) {

                    AlertDialog(
                        onDismissRequest = {
                            openDialog = false
                        },
                        title = {
                            Text(text = openDialogTitle)
                        },
                        text = {
                            Text(openDialogText)
                        },
                        confirmButton = {
                            Button(

                                onClick = {
                                    openDialog = false
                                    if (openDialogTitle != "Error") {
                                        var localUser = viewModel.getLocalUserByEmail(email)
                                        onSuccess(
                                            FirebaseUser(
                                                emailID = email,
                                                uid = localUser?.localId.toString(),
                                                idToken = firebaseToken
                                            )
                                        )
                                    }

                                }) {
                                Text("Ok")
                            }
                        }
                    )
                }
            }
        }
    }
}