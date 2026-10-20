# Firebase Realtime Database To-Do App with Priority Management

A reactive, priority-based To-Do application built with **Jetpack Compose**, **Material 3**, and **Firebase Realtime Database**.

---

## 📌 Objective & Scope

The objective of this assignment is to master the integration of **Firebase Realtime Database** with modern **Android Jetpack Compose** to build a reactive, priority-driven To-Do list application:
1. **Firebase Realtime Database Integration:** Store, observe, update, and delete tasks in the cloud with sub-second synchronization.
2. **Priority Management:** Dynamically categorize tasks into **High**, **Medium**, and **Low** priorities, automatically sorted and rendered via reactive Compose state observation.
3. **Interactive Jetpack Compose UI:** Leverage modern Compose components including `LazyColumn`, `SwipeToDismissBox`, Material 3 theming, state hoisting, and unidirectional data flow.
4. **Resilient Offline Architecture:** Offline disk persistence and automatic delta synchronization upon reconnection.

---

## 🚀 Key Features

- **⚡ Full Cloud CRUD Operations:**
  - **Create:** Instantly add new tasks with descriptions and priority flags via `DatabaseReference.push()`.
  - **Read:** Observe database changes in real-time using Kotlin Coroutines `callbackFlow` and `ValueEventListener`.
  - **Update:** In-place editing dialog allowing users to revise task description and priority levels.
  - **Delete:** Interactive bidirectional swipe gestures (`SwipeToDismissBox`) with instantaneous cloud removal.
- **🎯 Dynamic Priority Sorting & Visual Badges:**
  - Tasks are dynamically prioritized: **High** (🔴) $\rightarrow$ **Medium** (🟠) $\rightarrow$ **Low** (🟢).
  - High-priority tasks automatically rise to the top of the `LazyColumn`.
  - Color-coded badges for clean visual hierarchy.
- **📶 Offline Persistence & Conflict Resilience:**
  - Local caching via Firebase Realtime Database.
  - Tasks can be added, updated, and dismissed even while offline; changes queue locally and seamlessly sync upon network restoration.

---

## 🏛️ Architecture & System Design

The application adheres to official Google Android architecture guidelines utilizing **MVVM (Model-View-ViewModel)** with **Unidirectional Data Flow (UDF)**:

```
+-------------------------------------------------------------+
|                      UI Layer (Compose)                     |
|  - MainActivity.kt (EdgeToEdge, Theme setup)                |
|  - TodoScreen.kt (Scaffold, LazyColumn, SwipeToDismissBox)  |
+-------------------------------------------------------------+
                              ▲
                 UI State     │   User Events
            (StateFlow<TodoUiState>) (Add/Edit/Delete)
                              │
+-------------------------------------------------------------+
|                     ViewModel Layer                         |
|  - TodoViewModel.kt                                         |
|    * Dynamic priority sorting algorithm (High > Medium > Low)|
|    * Error handling via Snackbar                            |
+-------------------------------------------------------------+
                              ▲
                 Flow<List<Task>> (callbackFlow)
                              │
+-------------------------------------------------------------+
|                     Data / Repository Layer                 |
|  - TaskRepository.kt                                        |
|    * Firebase Realtime Database reference                   |
|    * ValueEventListener with awaitClose                     |
+-------------------------------------------------------------+
                              ▲
                 WebSocket / REST sync
                              │
+-------------------------------------------------------------+
|                 Firebase Realtime Database                  |
|  - Cloud JSON Tree (https://devraj-todo-default-rtdb...)    |
+-------------------------------------------------------------+
```

### Data Model (`Task.kt`)
```kotlin
data class Task(
    val id: String = "",
    val description: String = "",
    val priority: String = "Medium"
)
```

### Reactive Observation (`callbackFlow`)
```kotlin
fun getTasks(): Flow<List<Task>> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tasks = snapshot.children.mapNotNull { child ->
                child.getValue(Task::class.java)?.copy(
                    id = child.key ?: ""
                )
            }
            trySend(tasks)
        }

        override fun onCancelled(error: DatabaseError) {
            close(error.toException())
        }
    }
    tasksRef.addValueEventListener(listener)
    awaitClose { tasksRef.removeEventListener(listener) }
}
```

---

## 🛠️ Tech Stack & Dependencies

- **Language:** Kotlin 2.2+
- **UI Framework:** Jetpack Compose (Material 3, Material Icons Extended)
- **Architecture:** Android Architecture Components (ViewModel, StateFlow, Coroutines)
- **Backend:** Firebase Realtime Database (`com.google.firebase:firebase-database`)
- **JSON Serialization:** Firebase automatic object mapping via reflection
- **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalogs (`libs.versions.toml`)

---

## 🏃 Setup & Execution Guide

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/dev-raj-murari/Firebase-Realtime-Database-ToDo-Compose.git
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select **Open** and choose the cloned folder.
3. **Configure Firebase:**
   - Place your `google-services.json` inside the `app/` folder (pre-configured for `devraj-todo`).
   - In your Firebase Realtime Database Rules:
     ```json
     {
       "rules": {
         ".read": true,
         ".write": true
       }
     }
     ```
4. **Build & Run:**
   - Select your device/emulator (e.g. `Pixel_8`).
   - Press **Run 'app'** (`Shift + F10`).
