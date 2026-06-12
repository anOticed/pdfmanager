# Kotlin Concepts Used

This document explains the main Kotlin concepts used throughout `PDF Manager`. It is intended for readers familiar with Java, so only language features that may require additional explanation are covered.

## Properties and type inference

Kotlin uses properties instead of separate fields, getters, and setters:

```kotlin
val name: String
var isLoading: Boolean
```

- `val` cannot be reassigned after initialization
- `var` can be reassigned

A property can remain publicly readable while restricting its setter:

```kotlin
var selectedPreset by mutableStateOf(CompressionPreset.MEDIUM)
    private set
```

Computed properties return a value without storing it:

```kotlin
val isActive: Boolean
    get() = selectedImages.isNotEmpty()
```

Kotlin usually infers local variable types from their assigned values:

```kotlin
val pdf = selectedSplitPdf
val files = mutableListOf<PdfFile>()
```

## Null safety

Kotlin distinguishes between nullable and non-null types:

```kotlin
val pdf: PdfFile
val selectedPdf: PdfFile?
```

`PdfFile` must contain a value, while `PdfFile?` may contain `null`.

Common null-safety operators used in the project:

- `value?.property` accesses a property only when `value` is not null
- `value ?: fallback` returns `fallback` when `value` is null
- `value as? Type` performs a safe cast and returns null if the type does not match
- `value?.let { ... }` runs a block only for a non-null value

The non-null assertion operator `!!` is avoided because it throws an exception when the value is null.

## `object` and `companion object`

`object` declares a singleton instance:

```kotlin
object TempFileStore {
    fun createTempPdfFile(...) {
        // ...
    }
}
```

The project uses singleton objects for shared operations that do not require separate instances, including PDF writers, catalog services, temporary-file management, and destination writing.

A `companion object` belongs to a class and stores members that can be accessed without creating an instance:

```kotlin
enum class AppDestination(val route: String) {
    PdfList("pdfs"),
    Merge("merge"),
    Split("split"),
    Images("images"),
    Settings("settings");

    companion object {
        fun fromIndex(index: Int): AppDestination {
            return entries.getOrElse(index) { PdfList }
        }
    }
}
```

This is similar to a Java static method, although the members belong to a companion object generated for the class.

## `when`

`when` is Kotlin's alternative to Java `switch`. It supports values, types, nullable branches, and arbitrary conditions.

```kotlin
when (request) {
    is PreviewRequest.Single -> showPdf(request.pdf)
    is PreviewRequest.Split -> buildSplitPreview(request)
}
```

`when` can also return a value:

```kotlin
val plan = when (val result = splitPlanResult) {
    is SplitPlanResult.Ready -> result.plan
    is SplitPlanResult.Error -> return
    null -> return
}
```

## Delegated Compose state

Kotlin's `by` keyword delegates property access to another object. In Compose, it is used to expose observable state as a regular property:

```kotlin
var isLoading by mutableStateOf(false)
    private set
```

Without delegation, the value would be accessed through `.value`:

```kotlin
val state = mutableStateOf(false)
state.value = true
```

With `by`, the same state is read and updated directly:

```kotlin
isLoading = true
```

Changing a `mutableStateOf` value schedules recomposition for composable functions that read it.

## Coroutines

Kotlin coroutines are used for file access and PDF processing without blocking the Android main thread.

A `suspend` function can pause and resume while waiting for another operation:

```kotlin
suspend fun loadPdfMetadata(
    context: Context,
    uri: Uri
): PdfFile
```

It can be called only from another `suspend` function or from a coroutine.

File and PDF operations run on `Dispatchers.IO`:

```kotlin
val preview = withContext(Dispatchers.IO) {
    MergePdfWriter.buildPreviewPdf(
        context = context,
        pdfs = files,
        pagesPerSheet = pagesPerSheet
    )
}
```

`suspend` does not automatically move code to a background thread. 

`withContext(Dispatchers.IO)` explicitly selects a dispatcher intended for blocking I/O work.
