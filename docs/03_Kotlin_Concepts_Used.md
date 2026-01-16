# Kotlin concepts used in this codebase

This file explains Kotlin language features that may look unusual for those who mostly know Java.  
Only Kotlin-specific ideas are covered here (Android/Compose concepts are explained separately).

## Null-safety

Kotlin requires explicit handling of `null`.

Common operators used in the code:

- `?` in a type: `PdfFile?` means the value can be null
- Safe call: `value?.something` runs only if `value` is not null
- Elvis operator: `a ?: b` uses `b` if `a` is null
- `let { ... }`: commonly used after a safe call to work with a non-null value
- `!!`: asserts non-null and crashes if the value is null (used rarely and should be avoided)

## Top-level declarations

Kotlin allows functions and constants outside classes (top-level).
This project uses this style for:

- screen-level composables and helpers in the same file
- small private helper functions next to the code that uses them

## Properties instead of getters/setters

Kotlin uses properties:

```kotlin
var selected: Int = 0
    private set
```

- reads/writes look like field access
- writes can be restricted with `private set`
- computed properties are common:

```kotlin
val isActive: Boolean get() = selectedItems.isNotEmpty()
```

## `when` as an expression

`when` is similar to `switch`, but more powerful and returns a value:

```kotlin
val text = when (count) {
    1 -> "1 page"
    else -> "$count pages"
}
```

## String templates

Kotlin supports **string templates** (string interpolation), which avoids manual concatenation.

Two common forms:

- Insert a variable with `$name`
- Insert an expression with `${...}`

Examples used in this project:

```kotlin
"$pagesCount pages • $sizeText"
```

```kotlin
val label = "${pdf.name}#$pageIndex"
```

## Error handling with `runCatching`

`runCatching { ... }` wraps exceptions and avoids verbose try/catch in code:

```kotlin
runCatching { renderer.close() }
```

## Delegated properties (`by`)

Kotlin supports delegation syntax, which is used for state-like properties:

```kotlin
var selected by mutableStateOf(0)
```

The key Kotlin part here is `by`, which delegates getter/setter behavior to another object.

## `suspend` function

`suspend` marks a function that can **wait for something without blocking the thread** (for example I/O).
Instead of freezing the UI thread, the coroutine can pause and resume later.

A `suspend` function can be called only:
- from another `suspend` function, or
- from a coroutine (for example inside `LaunchedEffect { ... }`).

Example:

```kotlin
suspend fun loadTextFromFile(uri: Uri): String {
    return withContext(Dispatchers.IO) {
        // read file here
        "result"
    }
}

@Composable
fun Example() {
    LaunchedEffect(Unit) {
        val text = loadTextFromFile(uri)
    }
}
```

Note: `suspend` does not automatically run code on a background thread.
The thread is defined by the coroutine context (for example Dispatchers.IO).