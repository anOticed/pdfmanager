# Architecture

## Overview

`PDF Manager` is a single-activity Android application built with Kotlin and Jetpack Compose. It follows a feature-based MVVM architecture that separates feature state, PDF processing, and Android-specific operations.

The project is divided into four main areas:

- `app` assembles the application and coordinates features
- `feature` contains UI and state management for each tool
- `core.pdf` provides shared document models and PDF operations
- `core.system` handles Android APIs and storage access

## Application layer

The `app` package serves as the application entry and coordination layer.

`MainActivity` starts Compose and handles PDF files received through `Open with` or sharing intents.

`PdfManagerApp` acts as the composition root. It creates feature `ViewModel` instances and provides shared services for file selection, permissions, preview, export, page editing, and toast notifications.

## Feature layer

The `feature` package is organized by application tool. Each feature owns its Compose UI, `ViewModel`, and feature-specific models.

Compose functions display state and forward user actions. Document loading, modification, and saving are delegated to shared layers.

Each feature `ViewModel` manages:

- Selected files or images
- Operation settings
- Input validation
- Loading and processing state
- Reordering and selection
- Preview and export requests

## PDF processing layer

`core.pdf` contains document-related logic shared across features and is separated by responsibility:

- `model` defines document and metadata models
- `catalog` loads documents and manages their properties
- `edit` contains validated editing data and page editor sessions
- `render` prepares previews and page thumbnails
- `write` performs document transformations
- `util` provides filename, formatting, initialization, and page-layout utilities

## Android integration layer

`core.system` isolates operations that depend on Android framework APIs and connects application logic with device storage.

Its main responsibilities include:

- File and folder selection through Activity Result APIs
- Storage and camera permissions
- Document access through `ContentResolver`
- Temporary file management
- Cache file sharing through `FileProvider`
- Export destination handling
- Toast notifications

## Preview and export

Preview and export use shared infrastructure across features. Each feature passes prepared input and operation settings to the corresponding PDF writer.

For preview, the writer generates a temporary document in the application cache, which is opened with AndroidX PDF Viewer. Preview and export rely on the same PDF generation logic, so both operations produce an identical document.

Export is handled through a shared process that:

1. Normalizes the output name
2. Requests a file or folder destination
3. Generates the document in the background
4. Writes it to the selected destination
5. Reports success or failure
6. Removes temporary or incomplete files