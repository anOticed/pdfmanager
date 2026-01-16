# Architecture

## High-level design

The project follows a **feature-based UI architecture** built with **Jetpack Compose** and **ViewModels**.

Main layers:

- **UI (Compose)** renders screens based on state
- **State (ViewModels)** stores screen state and handles user actions
- **Data (Repository)** reads PDF metadata from the device storage providers

Navigation approach:

- **Tabs**: bottom bar switches between main screens using a pager
- **Preview**: shown as an overlay above the main UI

## Module/package boundaries

- `app/`  
  App entry, global scaffold, permission flow, top/bottom bars, routing between tabs
- `feature/*`  
  Feature UI + feature ViewModel logic (PDF list, merge, split, images, preview, settings)
- `core/*`  
  Shared infrastructure used by multiple features (repository, pickers, toast system, shared models)
- `ui/theme/*`  
  Theme and design tokens (colors, typography, shapes)

This separation is intentional: features can evolve independently while reusing shared services from `core/`.

## UI layer (Jetpack Compose)

### “Empty” vs “Active” screens

Some features are split into two simple states:

- **Empty state**: no input selected yet
- **Active state**: input selected, user can configure the operation

Examples:
- `feature/merge`: MergeScreen vs MergeActiveScreen
- `feature/split`: SplitScreen vs SplitActiveScreen
- `feature/images`: ImagesScreen vs ImagesActiveScreen (currently stubbed)

### Shared scaffold

The app uses one `Scaffold` and swaps bars depending on the current tab/state:

- `AppTopBar` selects the correct top bar composable
- `AppBottomBar` shows tab navigation

## State layer (ViewModels)

### Why ViewModels are used here

Each feature has a ViewModel that owns:
- current selection (chosen PDFs/images)
- active mode flags
- UI state (selected method, counts, etc.)
- actions that update the state

### Event handling

Some actions should happen only once (open merge/split/preview/details).  
This is handled by a simple event pattern:

- `PdfListViewModel` sets a `pendingEvent`
- `PdfListEventHandler` reacts to it and performs the side effect
- the event is cleared after handling

## Data layer (PDF repository)

### PdfRepository

`core/pdf/PdfRepository` is responsible for:
- scanning PDFs via **MediaStore**
- constructing `PdfFile` models with metadata needed by the UI

It does not render pages and does not perform PDF manipulation yet.

### PdfFile

`PdfFile` is the shared model for a PDF entry and contains:
- identity: `uri`, `name`
- metadata: size, page count, storage path, timestamps, “locked” flag
- formatting helpers used by UI

## Storage access and pickers

### Storage Access Framework (SAF)

The app uses Android’s standard storage APIs:
- **SAF pickers** for selecting PDFs/images
- **MediaStore** for listing PDFs on the device

### Pickers via CompositionLocal

Pickers are provided through `LocalPickers` so screens can access them without passing objects through many composables.

## Toast messaging

The app uses a lightweight toast infrastructure:
- `ToastManager` is provided via CompositionLocal
- ViewModels can trigger messages through a simple binding helper

## Preview architecture

Preview is implemented as an overlay layer:

- `ProvidePreview` sets up preview state in composition
- Screens call `LocalPreviewNav.open...()` to open preview
- The preview UI is displayed above the main content with slide animations

Inside preview, pages are rendered into bitmaps and displayed vertically.
The goal is to avoid reloading while scrolling (trade-off: memory usage on very large documents).

## Permissions / app startup flow

On startup, the app checks storage access.
If access is missing, it shows a permission dialog and guides the user to system settings.
On Android 11+, this can include requesting "All files access" (MANAGE_EXTERNAL_STORAGE) depending on the storage access mode.