# Android and Compose Basics

This document explains the main Android and Jetpack Compose concepts required to understand the application code.

## Activity and lifecycle

An `Activity` is an Android component that provides a window for application UI. It is created and managed by the operating system.

An activity moves through lifecycle states as it becomes visible, enters the foreground, loses focus, or is destroyed. Android may recreate an activity after a configuration change or process termination. Code should therefore avoid treating an activity instance as permanent storage for application state.

## Modern Android storage

### `Uri`

A `Uri` identifies a resource without requiring the application to know its physical location:

```text
content://media/external/file/123
```

A URI may refer to content from local storage, a document provider, another application, or a cloud-backed service.

### `ContentResolver`

`ContentResolver` provides access to data identified by a content URI. It can:

- Query metadata
- Open input and output streams
- Open file descriptors
- Insert, update, or delete content

### `MediaStore`

`MediaStore` is an Android database that indexes shared media and document files. Applications can query it to retrieve content URIs and metadata such as name, size, and timestamps.

### Storage Access Framework

The Storage Access Framework provides Android's system interface for selecting documents and folders. Instead of requesting access to an entire storage location, an application receives permission for the URI selected by the user.

### `FileProvider`

`FileProvider` securely exposes a private application file as a temporary `content://` URI.

Only directories declared in the provider configuration can be shared. Access is granted through URI permissions, so another component can read the file without receiving its private filesystem path.

## Jetpack Compose

Jetpack Compose is a declarative UI toolkit. UI is described as a set of functions based on current state instead of being updated manually through view references.

```kotlin
@Composable
fun Example(isLoading: Boolean) {
    if (isLoading) {
        CircularProgressIndicator()
    } else {
        Text("Ready")
    }
}
```

A function marked with `@Composable` can emit UI and call other composable functions. These calls form a UI tree managed by Compose.

### State and recomposition

Recomposition is the process of reevaluating composable functions after observable state changes.

```kotlin
var expanded by remember { mutableStateOf(false) }
```

When `expanded` changes, Compose schedules recomposition for functions that read it. Compose then updates only the affected parts of the UI tree.

remember stores a value across recompositions:

```kotlin
val state = remember { mutableStateOf(false) }
```

## Compose effects

Effect APIs allow Compose to run work that cannot be expressed as UI.

### `LaunchedEffect`

`LaunchedEffect` starts a coroutine tied to the composition:

```kotlin
LaunchedEffect(key) {
    loadData()
}
```

It starts when it enters composition. If its key changes, the existing coroutine is cancelled and a new one starts.

### `DisposableEffect`

`DisposableEffect` manages resources that require cleanup:

```kotlin
DisposableEffect(key) {
    registerListener()

    onDispose {
        unregisterListener()
    }
}
```

The cleanup block runs when the effect leaves composition or its key changes.

### `SideEffect`

`SideEffect` runs after every successful recomposition. It is intended for synchronizing Compose state with objects outside Compose.

## ViewModel

A `ViewModel` stores UI-related state and logic outside composable functions.

It survives activity recreation caused by configuration changes, allowing state to remain available while the UI is rebuilt. A ViewModel is cleared when its owner is permanently destroyed.

Compose can observe state exposed by a ViewModel and call its methods in response to user input:

```kotlin
@Composable
fun Example(viewModel: ExampleViewModel) {
    Text(text = viewModel.message)
    Button(onClick = viewModel::updateMessage) {
        Text("Update")
    }
}
```

## Lazy lists

`LazyColumn` and `LazyRow` compose only visible items and a small amount of nearby content. This makes them suitable for large or changing collections.

```kotlin
LazyColumn {
    items(
        items = documents,
        key = { it.id }
    ) { document ->
        DocumentItem(document)
    }
}
```
