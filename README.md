# Firebase Realtime Database To-Do App with Priority Management

---

## 📌 Objective & Scope

The objective of this assignment is to master the integration of **Firebase Realtime Database** with modern **Android Jetpack Compose** to build a reactive, priority-driven To-Do list application:
1. **Firebase Realtime Database Integration:** Store, observe, update, and delete tasks in the cloud with sub-second synchronization.
2. **Priority Management:** Dynamically categorize tasks into **High**, **Medium**, and **Low** priorities, automatically sorted and rendered via reactive Compose state observation.
3. **Interactive Jetpack Compose UI:** Leverage modern Compose components including `LazyColumn`, `SwipeToDismissBox`, Material 3 theming, state hoisting, and unidirectional data flow.
4. **Resilient Offline Architecture:** Offline disk persistence and automatic delta synchronization upon reconnection.

---

## Key Features

- **⚡ Full Cloud CRUD Operations:**
  - **Create:** Instantly add new tasks with descriptions and priority flags via `DatabaseReference.push()`.
  - **Read:** Observe database changes in real-time using Kotlin Coroutines `callbackFlow` and `ValueEventListener`.
  - **Update:** In-place editing dialog allowing users to revise task description, priority, or completion status.
  - **Delete:** Interactive bidirectional swipe gestures (`SwipeToDismissBox`) with instantaneous cloud removal and undo capabilities.
- **🎯 Dynamic Priority Sorting & Visual Badges:**
  - Tasks are dynamically prioritized: **High** (🔴) $\rightarrow$ **Medium** (🟠) $\rightarrow$ **Low** (🟢).
  - High-priority tasks automatically rise to the top of the `LazyColumn`.
  - Color-coded badges and task cards for enhanced visual hierarchy.
- **🔍 Quick Search & Priority Filters:**
  - Instant live filtering by priority chips (**All**, **High**, **Medium**, **Low**).
  - Search query support with real-time text matching.
- **📊 Real-time Dashboard Statistics:**
  - Dynamic counters displaying Total Tasks, High Priority Tasks, and Completed Tasks.
- **📶 Offline Persistence & Conflict Resilience:**
  - Local caching via `FirebaseDatabase.getInstance().setPersistenceEnabled(true)`.
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
            (StateFlow<TodoUiState>) (Add/Edit/Delete/Filter)
                              │
+-------------------------------------------------------------+
|                     ViewModel Layer                         |
|  - TodoViewModel.kt                                         |
|    * Priority sorting algorithm (High > Medium > Low)       |
|    * Filtering, search logic, error handling                |
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
|  - Cloud JSON Tree (https://...firebasedatabase.app/tasks)  |
+-------------------------------------------------------------+
```

### Data Model (`Task.kt`)
```kotlin
data class Task(
    val id: String = "",
    val description: String = "",
    val priority: String = "Medium",
    val completed: Boolean = false,
    val timestamp: Long = 0L
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
   - Launch Android Studio Ladybug / Meerkat or newer.
   - Select **Open** and select the cloned project folder.
3. **Configure Firebase:**
   - Place your `google-services.json` inside the `app/` folder (a valid template is already pre-configured for NMIMS MAD lab).
   - Verify that your Firebase Realtime Database rules allow read and write access:
     ```json
     {
       "rules": {
         "tasks": {
           ".read": true,
           ".write": true
         }
       }
     }
     ```
4. **Build & Run:**
   - Connect an Android device or start an Android Virtual Device (AVD with API 26+).
   - Press **Run 'app'** (`Shift + F10`).
