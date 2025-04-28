package com.dwarshb.chaitalk.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.db.SqlDriver
import com.dwarshb.chaitalk.AppUser
import com.dwarshb.chaitalk.Database
import com.dwarshb.chaitalk.DatabaseQueries
import com.dwarshb.chaitalk.User
import com.dwarshb.firebase.AuthResponse
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.FirebaseAuth
import com.dwarshb.firebase.FirebaseDatabase
import com.dwarshb.firebase.FirebaseUser
import com.dwarshb.firebase.TokenResponse
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.launch

class AuthenticationViewModel(var firebase: Firebase? = null, sqlDriver: SqlDriver?) : ViewModel() {
    private var databaseQuery : DatabaseQueries?
    var firebaseAuth = FirebaseAuth()

    init {
        val database : Database? = if(sqlDriver!=null) {Database(sqlDriver)} else {null}
        databaseQuery = database?.databaseQueries
    }

    fun validateEmail(email: String): Boolean {
        if (email == "") return false
        val emailRegex = Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\$")
        return emailRegex.matches(email)
    }

    suspend fun signUp(
        email: String,
        password: String,
        confirmPassword: String,
        name: String,
        phoneNumber: String,
        completion: onCompletion<String>
    ) {
        if (password == confirmPassword) {
                firebaseAuth.signUpWithEmailAndPassword(email,password, object : onCompletion<AuthResponse> {
                    override fun onSuccess(T: AuthResponse) {
                        storeUserDetails(T)
                        updateUserInDatabase(T.localId,T.email,T.refreshToken,name,phoneNumber)
                        completion.onSuccess(T.idToken)
                    }

                    override fun onError(e: Exception) {
                        completion.onError(e)
                    }
                })
        } else {
            completion.onError(Exception("Password doesn't match"))
        }
    }

    suspend fun login(
        email: String,
        password: String,
        completion: onCompletion<String>
    ) {
            firebaseAuth.login(email,password,object : onCompletion<AuthResponse> {
                override fun onSuccess(T: AuthResponse) {
                    storeUserDetails(T)
                    completion.onSuccess(T.idToken)
                }
                override fun onError(e: Exception) {
                    completion.onError(e)
                }
            })
    }

    private fun updateUserInDatabase(uid:String,
                                     email:String,
                                     token:String,
                                     name: String,
                                     phoneNumber: String) {
        firebase?.setCurrentUser(FirebaseUser(email,token,uid))
        val firebaseDatabase = FirebaseDatabase()
        viewModelScope.launch {
            val child = listOf("users")
            val map = HashMap<String,Any>()
            val appUser = AppUser(email,"", emptyList(), uid)
            map.put(uid,appUser)
            firebaseDatabase.patchFirebaseDatabase(child,map,
                object : onCompletion<String> {
                    override fun onSuccess(T: String) {
                        print(T)
                    }

                    override fun onError(e: Exception) {
                        print(e.message)
                    }
                }
            )
        }
    }
    internal fun storeUserDetails(response: AuthResponse) {
        viewModelScope.launch {
            databaseQuery?.insertUser(
                response.idToken, response.email, response.refreshToken,
                response.email, response.localId, ""
            )
        }
    }

    internal fun getLocalUserByEmail(email: String): User? {
        return databaseQuery?.selectUserByEmail(email)?.executeAsOne()
    }

    internal fun checkSession(onCompletion: onCompletion<User>) {
        val userList = databaseQuery?.selectAllUsers()?.executeAsList()
        if (userList!=null) {
            for (user in userList) {
                if (user != null) {
                    viewModelScope.launch {
                        firebaseAuth.getRefreshToken(user.refreshToken,
                            object : onCompletion<TokenResponse> {
                                override fun onSuccess(T: TokenResponse) {
                                    val _user = User(
                                        idToken = T.id_token,
                                        email = user.email,
                                        localId = T.user_id,
                                        refreshToken = T.refresh_token,
                                        name = user.email,
                                        phone = ""
                                    )
                                    println(_user)
                                    onCompletion.onSuccess(_user)
                                }

                                override fun onError(e: Exception) {
                                    onCompletion.onError(e)
                                }
                            })
                    }
                } else {
                    onCompletion.onError(Exception("No session found"))
                }
            }
        }
    }
}
