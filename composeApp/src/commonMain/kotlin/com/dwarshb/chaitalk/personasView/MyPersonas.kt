package com.dwarshb.chaitalk.personasView

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwarshb.chaitalk.Persona
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.onCompletion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPersonas(onBackPressed: ()->Unit) {
    val firebaseUser = Firebase.getCurrentUser()
    val myPersonaViewModel = MyPersonaViewModel()
    val personaList = myPersonaViewModel.personas.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        myPersonaViewModel.fetchMyPersonas(firebaseUser?.uid.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Personas") },
                navigationIcon = {
                    IconButton(onClick = {onBackPressed()}){
                        Icon(Icons.AutoMirrored.Default.ArrowBack,"Back")
                    }
                })
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(personaList.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        Text(it.name, style = MaterialTheme.typography.titleMedium)
                        Text(it.id, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(it.id))
                        }) {
                        Icon(Icons.Default.ContentCopy, "Copy Persona Id")
                    }
                }
                HorizontalDivider()
            }
        }

    }
}