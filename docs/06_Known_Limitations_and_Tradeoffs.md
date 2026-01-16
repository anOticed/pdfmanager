# Known limitations and trade-offs

This document lists current limitations and intentional trade-offs in the project.
The goal is to clarify what is not implemented yet and what design choices were made.

## 1) Feature scope limitations

The app is still under active development. Some workflows are currently UI-only or partially implemented:

- PDF manipulation (saving results for merge/split/images) is not fully implemented yet
- split-mode specific preview behavior is stubbed
- many planned operations are not implemented yet (metadata editing, password operations, compression, N-up layouts, etc.)

## 2) Storage and permissions

- PDF listing relies on Android storage providers (MediaStore) and URIs
- on Android 11+ the app may request "All files access" (MANAGE_EXTERNAL_STORAGE) to simplify listing/access

This improves usability for file listing but is more permissive than SAF-only access.

## 3) Preview rendering strategy

Preview renders PDF pages into bitmaps and displays them in a vertical list.

Trade-off:
- smoother scrolling (avoid re-rendering during scroll)
- higher memory usage for large PDFs (many pages rendered as bitmaps)

## 4) Offline-first

The app is designed to work completely offline.

Trade-off:
- no cloud sync or online integrations
- all processing depends on device performance