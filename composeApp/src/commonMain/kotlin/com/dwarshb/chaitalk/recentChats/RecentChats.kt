package com.dwarshb.chaitalk.recentChats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dwarshb.chaitalk.Persona
import com.dwarshb.firebase.Firebase
import com.dwarshb.firebase.onCompletion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentChats(onBackPressed:()->Unit,
                onChatSelected: (Persona)->Unit,
                viewModel: RecentChatsViewModel = viewModel()) {
    val chats by viewModel.recentChats.collectAsState()
    val userId = Firebase.getCurrentUser()?.uid
    val clipboardManager = LocalClipboardManager.current
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val onRefresh = {
        isRefreshing = true
        userId?.let { viewModel.fetchRecentChats(it) }
        coroutineScope.launch {
            delay(2000)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        userId?.let { viewModel.fetchRecentChats(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recent Chats") },
                navigationIcon = {
                    IconButton(onClick = {onBackPressed()}){
                        Icon(Icons.AutoMirrored.Default.ArrowBack,"Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onRefresh() }){
                        Icon(Icons.Default.Refresh,"Refresh Button")
                    }
                })
        }
    ) { paddingValues ->
        Column(modifier =  Modifier.padding(paddingValues)) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(chats.toList()) { (personaId, lastMessage) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.getPersona(personaId, object : onCompletion<Persona>{
                                        override fun onSuccess(t: Persona) {
                                            onChatSelected(t)                                        }

                                        override fun onError(e: Exception) {
                                            //Todo show error dialog
                                        }
                                    })
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                "PersonaId: $personaId",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(lastMessage.content,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(personaId))
                            }) {
                            Icon(Icons.Default.ContentCopy, "Copy Persona Id")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
