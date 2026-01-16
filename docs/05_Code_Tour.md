# Code tour

This document gives a practical overview of where key logic lives in the repository.

## 1) App entry point and global UI

### MainActivity

`MainActivity` is the single Android entry point. It sets the Compose content and hosts the entire UI.

### App shell (scaffold)

Global app layout is built around a single `Scaffold`:

- `AppTopBar` selects which top bar to show depending on the current tab/state
- `AppBottomBar` renders the bottom tab navigation

### AppNav (tab routing)

`AppNav` is the high-level router for the main tabs.  
It controls which feature screen is visible and keeps the tab structure consistent.

### Storage permission flow

The app includes a small permission flow for storage access.
If access is missing, a dialog is shown and the user is guided to system settings.

## 2) Core layer (shared infrastructure)

Shared code used by multiple features is placed in `core/`.

### PDF model

`core/pdf/model/PdfFile.kt` defines `PdfFile`, the main model for a PDF entry.

It contains:
- identity (`uri`, `name`)
- basic metadata (size, page count, storage info, timestamps, locked flag)
- small formatting helpers used by UI (for example meta line and date formatting)

### PDF repository

`core/pdf/PdfRepository` is responsible for:
- listing PDFs via MediaStore
- reading metadata and building `PdfFile` instances

It does not render pages and does not perform PDF manipulation yet.

### Pickers (SAF)

The project uses the Android system pickers (Storage Access Framework) for selecting PDFs/images.
Picker entry points are exposed through a CompositionLocal (`LocalPickers`) so screens can call them without passing objects through many layers.

### Toast system

A small toast system is used for user messages:
- a toast manager is provided via CompositionLocal
- ViewModels can trigger toasts through a binding helper

## 3) Feature modules

Feature code lives under `feature/`.  
Each feature typically contains:

- composable screens
- a ViewModel holding state and actions
- UI components specific to that feature (top bars, cards, etc.)

### PDF list

The PDF list feature is the main entry for selecting files.

Key ideas in this module:
- normal browsing mode vs selection mode
- one-time actions (open merge / split / preview / details) are triggered through a `pendingEvent` and handled in a dedicated handler

### Merge

The merge feature is split into:
- an empty state screen (no selection yet)
- an active screen (selected PDFs visible as a reorderable list)

The ViewModel stores:
- selected PDFs
- reorder logic
- actions to open preview

### Split

The split feature is similar in structure:
- empty screen (no PDF selected)
- active screen (split options)

Split method selection is stored in the ViewModel.
Preview for split is present as an entry point, but detailed rendering logic is currently stubbed.

### Preview

Preview is implemented as an overlay layer above the main UI.

Key parts:
- a preview controller provided via `ProvidePreview`
- screens open preview via `LocalPreviewNav.open...()`
- preview UI renders pages into bitmaps and shows them in a vertical list

The preview goal is “smooth scrolling without reloading”, which can use more memory on large documents.

### Images to PDF

Images-to-PDF follows the same pattern (empty vs active).
Currently, gallery selection and rendering are stubbed with placeholder items.

### Settings

The settings feature contains UI-only settings screens (no business logic).

## 4) UI theme

The project theme lives under `ui/theme/`.

This includes:
- app color palette and design tokens
- common styling constants used across features

The goal is consistent styling across all screens.
