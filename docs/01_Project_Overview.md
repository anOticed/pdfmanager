# Project Overview

## What this project is

**PDF Manager** is an **offline Android application** focused on local PDF operations and previews.
The app uses Android’s system file access (Storage Access Framework / MediaStore) rather than relying on direct file paths.

The main user flow is:

1. Select a PDF from the device
2. Preview pages
3. Choose an operation (merge, split, preview, images → PDF)
4. Save the result back to storage (some operations are still under development)

## Goals

1. Provide a clean and fast UI for common PDF tasks
2. Keep all operations local
3. Use modern Android stack (Kotlin + Jetpack Compose + ViewModels)
4. Keep the code modular by feature (PDF list, merge, split, preview, images, settings)

## Current scope (implemented in code)

- **PDF list**
    - lists PDFs available on the device (via MediaStore)
    - selection mode and basic per-file options
- **Merge UI**
    - selecting multiple PDFs
    - reorder selected items
    - entering preview
- **Split UI**
    - selecting a single PDF
    - choosing a split method
    - entering preview (rendering is currently stubbed)
- **Images to PDF**
    - "active screen" exists with a reorderable list
    - gallery selection is currently stubbed with placeholder items
- **Preview overlay**
    - opens a PDF preview UI
    - renders pages into bitmaps and displays them in a scrollable list


## Features planned for future iterations

The project is under active development. Some features are implemented only as UI stubs or partially implemented:

- actual PDF manipulation and saving results for merge/split/images workflows
- split-mode specific preview rendering
- advanced page operations (rotate, delete, reorder within a PDF, etc.)
- metadata editing, password operations, compression, N-up layouts

## Repository structure

- `app/`  
  App entry point, global navigation/state, shared scaffolding (top/bottom bars), permission flow
- `core/`  
  Shared infrastructure: PDF repository access, system pickers integration, toast messaging, shared models
- `feature/`  
  Feature modules (PDF list, merge, split, preview, images, settings)
- `ui/theme/`  
  Theme, colors, and UI design tokens