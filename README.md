# Firebase Realtime Database To-Do App with Priority Management

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.2-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20Material3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Realtime%20Database-orange.svg)](https://firebase.google.com/docs/database)

---

## 🎓 Academic Submission Details

| Field | Details |
| :--- | :--- |
| **Institution** | SVKM's NMIMS Deemed-to-be UNIVERSITY |
| **Campus** | School of Technology Management & Engineering, Mumbai / Navi Mumbai |
| **Course** | Mobile Application Development (Subject Code: `702AI0E002`) |
| **Academic Year** | 2024 – 2025 |
| **Student Name** | Devraj Murari |
| **Roll Number** | N066 |
| **Assignment** | Firebase Realtime Database & CRUD Operations with Jetpack Compose |

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

## 📝 Answers to Assignment Questions (B.3)

### **Q1 (i). What is Firebase Realtime Database, and why is it suitable for real-time apps?**

**Answer:**
**Firebase Realtime Database** is a cloud-hosted, NoSQL database provided by Google where data is stored as a single large JSON tree and synchronized in real-time to every connected client.

**Why it is suitable for real-time apps:**
1. **Persistent WebSocket Connection:** Unlike traditional REST architectures where mobile clients poll the server at fixed intervals (introducing network overhead and latency), Firebase maintains a bi-directional persistent WebSocket connection. When data changes at any node, the server pushes the updated delta to all subscribed clients within milliseconds (sub-100ms latency).
2. **Reactive Event-Driven Architecture:** Clients register listeners (e.g., `ValueEventListener`, `ChildEventListener`). Data synchronization happens reactively without manual refresh logic, making it ideal for collaborative apps, chat messengers, live dashboards, and reactive To-Do lists.
3. **Cross-Platform Real-time Sync:** Synchronization occurs seamlessly across Android, iOS, Web, and backend environments simultaneously, ensuring data consistency across multiple user devices.
4. **Built-in Offline Engine:** The client SDK transparently maintains an on-disk cache of data and locally queues write operations, allowing apps to remain fully responsive without active internet connectivity.

---

### **Q2 (ii). Explain how the Firebase Realtime Database manages synchronization between multiple clients. How does it handle offline scenarios?**

**Answer:**

#### 1. Multi-Client Synchronization Mechanism:
- **Single Source of Truth (Cloud JSON Tree):** All data is stored in the cloud as a nested JSON structure.
- **Observer Pattern via WebSockets:** When client A initiates an update (e.g., adding or modifying a task), the Firebase SDK pushes the payload over its open WebSocket connection to the cloud database.
- **Server Dispatch & Fan-out:** The Firebase server commits the change, evaluates security rules, and immediately broadcasts the updated snapshot/delta to all other connected clients (client B, client C) who hold active listeners on that node.

#### 2. Offline Handling & Edge Synchronization:
Firebase Realtime Database is built with an **Offline-First** philosophy:
1. **Local Disk Persistence (`setPersistenceEnabled(true)`):** When enabled, Firebase caches every synchronized node into a local on-device SQLite database. App restarts immediately read from this local cache.
2. **Optimistic Local UI Updates:** When an offline user creates, edits, or deletes a task, Firebase fires local event listeners *immediately*. The UI updates instantaneously with zero network latency, providing a seamless user experience.
3. **Persistent Write Queue:** Offline mutation events are recorded in a local write queue.
4. **Reconnection & Automatic Conflict Resolution:**
   - As soon as the device reconnects to Wi-Fi/Cellular, the SDK flushes the write queue to the Firebase server.
   - The server applies transactions. If timestamps are assigned using `ServerValue.TIMESTAMP`, the server resolves chronological ordering deterministically.
   - The authoritative server state is then reflected back to the client, reconciling any discrepancies.

---

### **Q3 (iii). Compare Firebase Realtime Database with other database options available for Android. What are the benefits and drawbacks of using Firebase for a To-Do app?**

**Answer:**

#### Comprehensive Database Comparison Table:

| Feature / Database | Firebase Realtime Database | Cloud Firestore | Room (SQLite) | Supabase / PostgreSQL | Realm (Mongo Device Sync) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Type** | NoSQL (JSON Tree) | NoSQL (Document/Collection)| Relational (SQL) | Relational (PostgreSQL) | Object-oriented |
| **Location** | Cloud + Local Cache | Cloud + Local Cache | Local-only (Device) | Cloud + Realtime | Local + Cloud Sync |
| **Realtime Sync** | Native (WebSockets) | Native (gRPC Streams) | Via Kotlin Flow / InvalidationTracker | WebSockets (Postgres CDC) | Native Object Sync |
| **Offline Support** | Transparent (Disk Cache) | Advanced (Indexed Cache) | Native (100% Offline) | Manual caching required | Native |
| **Query Complexity** | Basic (Filter/Sort on 1 key)| Complex (Compound Queries) | High (Full SQL queries, Joins) | Very High (Full SQL, Views) | Medium (Object queries) |
| **Pricing Model** | Bandwidth (GB Downloaded) | Document Reads/Writes | Free / Open Source | Compute / Bandwidth | Tiered pricing |

#### Benefits of Firebase Realtime Database for a To-Do App:
- **Instant Setup & Zero Backend:** No custom server, ORM tables, or API boilerplate needed; plug-and-play SDK.
- **Effortless Live Sync:** Two users sharing a task list see changes instantly without pulling to refresh.
- **Built-in Offline First:** The To-Do app works on airplanes or poor network zones without extra code.
- **Lightweight Structure:** Simple key-value / JSON nodes match the flat structure of tasks perfectly.

#### Drawbacks of Firebase Realtime Database for a To-Do App:
- **Limited Querying & Filtering:** Realtime Database can only sort or filter on a single child key per query (e.g., cannot filter by priority *and* sort by completion date simultaneously at the server level; sorting must be performed client-side in Kotlin/Compose).
- **Entire Subtree Download:** Querying a node downloads all children beneath it, which can cause excessive bandwidth usage if tasks include large payloads (e.g., image attachments).
- **Scale Limits for Deep Trees:** Deeply nested JSON structures degrade performance, necessitating flat denormalized schemas.

---

## 💡 Conclusion & Reflection on Learnings (B.2)

Building this application provided deep practical experience in:
1. **Firebase Cloud Architecture:** Setting up Firebase Realtime Database, handling real-time database references, executing asynchronous write operations (`setValue`, `removeValue`, `push`), and securing database rules.
2. **Coroutines & `callbackFlow` Bridging:** Elegantly converting legacy callback-based APIs (`ValueEventListener`) into modern, lifecycle-aware Kotlin `Flow` streams that automatically manage listener lifecycles via `awaitClose`.
3. **Jetpack Compose State Management:** Constructing reactive UIs with Unidirectional Data Flow, hoisting state to `TodoViewModel`, and observing changes via `collectAsStateWithLifecycle()`.
4. **Interactive Material 3 UI/UX:** Implementing modern mobile interaction paradigms including `SwipeToDismissBox` for fluid swipe-to-delete, animated list updates with stable keys in `LazyColumn`, and dynamic Material 3 color schemes.
5. **Priority-Driven Scheduling:** Implementing dynamic multi-tier sorting algorithms that prioritize urgent actions while maintaining clean, readable user interfaces.

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
