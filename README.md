# PDF Manager - Android

The application works completely offline. The user opens a PDF through the system file dialog, previews pages, chooses an operation, and saves the result back to storage.

## Planned features:

- display of page previews, multiple selection, moving, deleting, rotating;
- merging multiple PDFs into one with visible page order;
- splitting a PDF: by ranges, one page per file, or every N pages;
- "pages per sheet": 2/4/6/9 pages per sheet, margins, orientation, fill order, live preview;
- PDF size reduction by lowering image quality/resolution with estimated final size;
- metadata editing (title, author, keywords, subject);
- password management: set a password when saving or remove it after entering the correct one;
- "from images → PDF": select photos, set order and orientation, preview, and save.

## Tech Stack
- Android Studio
- Kotlin
- Jetpack Compose (UI)
- PDFBox-Android (PDF manipulation)
- Storage Access Framework (SAF for file access)
- System page rendering for previews

## Website
https://pdfmanager.dev

---

## Screenshots
<img src = ".assets/screenshot-01.jpg" width = "240"/> <img src = ".assets/screenshot-02.jpg" width = "240"/> <img src = ".assets/screenshot-03.jpg" width = "240"/> 
<img src = ".assets/screenshot-04.jpg" width = "240"/> <img src = ".assets/screenshot-05.jpg" width = "240"/> <img src = ".assets/screenshot-06.jpg" width = "240"/>
<img src = ".assets/screenshot-07.jpg" width = "240"/>

---

## Demo
<img src = ".assets/demo_1.gif" width = "240"/> <img src = ".assets/demo_2.gif" width = "240"/> <img src = ".assets/demo_3.gif" width = "240"/> 
<img src = ".assets/demo_4.gif" width = "240"/> <img src = ".assets/demo_5.gif" width = "240"/> <img src = ".assets/demo_6.gif" width = "240"/>
<img src = ".assets/demo_7.gif" width = "240"/>

---

## Documentation
- [Project overview](docs/01_Project_Overview.md)
- [Architecture](docs/02_Architecture.md)
- [Kotlin concepts used in this codebase](docs/03_Kotlin_Concepts_Used.md)
- [Android + Jetpack Compose basics](docs/04_Android_and_Compose_Basics.md)
- [Code tour](docs/05_Code_Tour.md)
- [Known limitations / trade-offs](docs/06_Known_Limitations_and_Tradeoffs.md)

---

## Status

This app is under active development. 
The demo and screenshots are illustrative and do not represent the final product. 
Features, UI, and performance will continue to evolve, and changes may occur between previews.
