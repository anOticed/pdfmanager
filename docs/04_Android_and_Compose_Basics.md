# Android and Jetpack Compose basics

This document explains the Android and Jetpack Compose concepts needed to understand how this project works.
Only the concepts that are relevant to this codebase are included.

## 1) What an Android app is built from

### Activity

An **Activity** is a screen-level Android component with a lifecycle managed by the system.

In this project, the Android entry point is a single Activity (MainActivity), which hosts the entire Compose UI. 
Some files are named *...Activity*, but they are composable wrappers (not Android Activity classes).

### Lifecycle

An Activity can be created, paused, resumed, and destroyed.
This matters because:

- UI should not hold long-lived resources directly
- heavy work should be cancellable
- resources should be released when a screen is closed

In Compose, lifecycle-sensitive cleanup is usually done with `DisposableEffect`.

## 2) Storage access model used on modern Android

Android apps should not rely on raw file paths. Modern Android uses **URIs** and system-managed storage access.

### Uri and ContentResolver

A `Uri` is an identifier for a document.
The app reads the content through `ContentResolver`, typically by opening an input stream or a file descriptor.

### MediaStore (listing files)

**MediaStore** is a system database that indexes media and documents.
This project uses it to list available PDF files and load metadata (name, size, timestamps, etc.).

Note: on Android 11+ the app may request "All files access" (MANAGE_EXTERNAL_STORAGE) to simplify PDF listing and access.

### Storage Access Framework (SAF) (user picks files)

**SAF** is the system file picker.
The user selects PDFs/images and the app receives a `Uri` that it can read.

## 3) What Jetpack Compose is

Jetpack Compose is a **declarative UI toolkit**.

Instead of manually modifying UI elements, Compose works like this:

- UI is described as functions
- these functions are re-run automatically when relevant state changes
- Compose updates only the parts of the UI that changed

### Composable functions

A `@Composable` function describes UI.

```kotlin
@Composable
fun MyScreen() {
    Text("Hello")
}
```

Composable functions can call other composables and build the UI tree.

## 4) State and recomposition

### Recomposition

**Recomposition** is when Compose re-runs composable functions because some state they read has changed.

Key rule:
- Composables should be side-effect free (they should only describe UI).
- Side effects must be done using Compose effect APIs (see below).

### `remember` and state holders

`remember` stores a value across recompositions.

Common patterns:

- `remember { ... }` for objects that should not be recreated
- `mutableStateOf(...)` for observable state

When a `mutableStateOf` value changes, Compose triggers recomposition for the parts that read it.

### `derivedStateOf`

`derivedStateOf` creates a value derived from other state values and recalculates only when its inputs change.
This is used for computed UI data.

## 5) Side effects in Compose

Composable functions should not perform side effects directly (such as I/O, opening files, or starting animations).
Compose provides effect APIs for this:

### `LaunchedEffect`

Runs a coroutine when a key changes (or once, if the key is stable).

Used for:
- loading data
- running async work tied to a composable lifecycle

### `DisposableEffect`

Used to clean up resources when a composable leaves the composition.

Used for:
- closing file descriptors
- releasing renderers/caches
- removing listeners

## 6) ViewModel in this project

A **ViewModel** is a state holder designed for UI.
It survives configuration changes (for example, screen rotation) and keeps UI state out of composables.

In this project, feature ViewModels typically store:

- current selection (PDFs/images)
- UI mode flags (empty vs active)
- selected options (split method, etc.)
- actions that mutate the state (select, clear, reorder, open preview)

Compose screens read the ViewModel state and call ViewModel methods on user interaction.

## 7) Coroutines (used for background work)

Android UI must stay responsive, so heavier work runs off the main thread.

This project uses Kotlin coroutines for:

- loading metadata
- rendering preview pages (bitmap rendering)

Typical pattern:

- CPU/IO work is executed on `Dispatchers.IO`
- the UI observes the resulting state and updates automatically

## 8) Lists in Compose (`LazyColumn`)

`LazyColumn` renders large vertical lists efficiently.

Important details used in this project:

- items can have stable `key`s to avoid UI glitches during reordering
- only visible items are composed (and more are composed as the user scrolls)

This is used for:
- PDF lists
- selected items lists (merge/images)
- preview page lists

## 9) Navigation approach used here

This project does not rely on the Navigation Component.
Instead it uses:

- tab switching (pager + bottom bar)
- preview opening as a separate layer (overlay-style preview flow)

This keeps navigation simple and matches the app’s structure (features are mostly independent tabs with a shared preview experience).

