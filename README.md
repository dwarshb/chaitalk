
# ☕ chAItalk

**chAItalk** is a personalized AI chat platform where users can create and interact with custom personas powered by generative AI. Each persona has its own name, description, personality, and avatar. The application is designed with Compose Multiplatform, using Firebase Realtime Database for persistence and Gemini-compatible APIs for natural language conversations.

---

## 🌟 Features

- 🧠 **Create Your Persona**  
  Define your AI assistant with a name, description, personality traits, and avatar using a friendly UI.

- 💬 **Chat with Personas**  
  Each chat session is tailored to the persona’s tone and memory of the conversation.

- 🌐 **Compose Multiplatform UI**  
  A modern, declarative UI that works across Android, Desktop, and more.

- 🗂 **Persistent Chat History**  
  Firebase Realtime Database stores conversations per persona for each user.

- ⚙️ **Setting to Modify APIs** (WIP)
  Change the gemini model from the settings option.
<br/>
  <img src="https://github.com/user-attachments/assets/0a972a72-6e2d-4e75-bdd3-e622f98b6db2" width=200>

---

## 🛠 Tech Stack

- **UI:** Compose Multiplatform  
- **Backend:** Firebase Realtime Database (REST API) | Firebase Authentication (REST API)
- **AI Integration:** Gemini APIs  
- **Language:** Kotlin  
- **HTTP Client:** Ktor  
- **Serialization:** kotlinx.serialization

---

## 📸 Demo
[![Demo](https://img.youtube.com/vi/7iFeuRiKPxw/0.jpg)](https://youtu.be/7iFeuRiKPxw)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/dwarshb/chaitalk.git
```

### 2: Open the project and Setup API Keys in App.kt
```bash
val firebaseAPIKey : MutableState<String> = remember {  mutableStateOf(YOUR_FIREBASE_API_KEY")}
val firebaseDatabaseUrl = remember {  mutableStateOf("https://yourprojectdatabaseurl.firebaseio.com/") }
val STORAGE_URL = "YOUR FIREBASE STORAGE URL"
val geminiKey = remember { mutableStateOf("YOUR GEMINI API KEY") }

var geminiModel : MutableState<String> = remember { mutableStateOf("gemini-2.0-flash") }

firebase.initialize(apiKey = firebaseAPIKey.value,
            databaseUrl = firebaseDatabaseUrl.value,storageUrl = STORAGE_URL)
firebase.setGemini(geminiModel.value,geminiKey.value)

```
### 3; Run the project
---

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [GitHub](https://github.com/JetBrains/compose-multiplatform/issues).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.
---
