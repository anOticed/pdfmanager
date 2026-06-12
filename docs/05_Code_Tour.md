# Code Tour

This document describes the source structure of `PDF Manager` and explains where each part of the application is implemented.

## `app`

The `app` package starts the application, assembles shared services, and coordinates actions between features.

| File | Purpose |
|---|---|
| `MainActivity.kt` | Android entry point that starts Compose and receives external PDF intents |
| `PdfManagerApp.kt` | Creates feature ViewModels and installs shared providers for permissions, pickers, preview, export, page editing, and notifications |
| `AppDestinations.kt` | Defines the five main pager destinations |
| `AppScaffold.kt` | Contains the main `Scaffold`, top bars, bottom navigation, selection bar, and `HorizontalPager` |
| `AppActiveScreen.kt` | Handles the animated transition between the light and dark application themes |
| `AppStartup.kt` | Requests storage access and starts the initial PDF catalog load |
| `AppEventDispatcher.kt` | Handles file-list actions such as previewing, merging, splitting, compressing, editing, sharing, and printing PDFs |
| `AppOverlays.kt` | Places file options, details, dialogs, and compression UI above the main content |
| `ExternalPdfIntentHandler.kt` | Imports a PDF received from another application and offers preview, split, or merge actions |

## `core.pdf.model`

This package contains document models shared across PDF features.

| File | Purpose |
|---|---|
| `PdfFile.kt` | Represents a PDF with its URI, name, size, page count, storage information, timestamps, thumbnail, and password state |
| `PdfDocumentMetadata.kt` | Stores editable title, author, subject, and keyword values |

## `core.pdf.catalog`

The catalog package reads existing documents and applies operations to their stored properties.

| File | Purpose |
|---|---|
| `PdfCatalogRepository.kt` | Finds PDFs, loads metadata, renders thumbnails, detects locked documents, and caches visual data |
| `PdfFileService.kt` | Renames, deletes, and prints PDF files |
| `PdfMetadataService.kt` | Reads and updates standard PDF metadata |
| `PdfPasswordService.kt` | Adds password protection and removes it after password validation |

## `core.pdf.edit`

This package contains validated editing data and temporary editing sessions.

| File | Purpose |
|---|---|
| `PdfSplitPlan.kt` | Converts split settings into validated groups of page numbers |
| `PageEditorSession.kt` | Creates and manages temporary document versions used by `PageEditor` |

## `core.pdf.render`

This package contains logic for generating PDF previews and page thumbnails.

| File | Purpose |
|---|---|
| `GeneratedPreviewPdf.kt` | Stores basic information about a generated preview file |
| `PreviewPdfBuilder.kt` | Creates managed preview files and converts them into `PdfFile` models |
| `PdfThumbnailRenderer.kt` | Renders one PDF page into a bounded bitmap |

## `core.pdf.util`

This package provides shared PDF policies and calculations.

| File | Purpose |
|---|---|
| `FileSizeFormatter.kt` | Formats byte values using localized units |
| `PagesPerSheet.kt` | Defines the supported 1, 2, and 4 items per output page |
| `PagesPerSheetLayout.kt` | Calculates A4 page slots and places imported PDF pages or images inside them |
| `PdfBoxInitializer.kt` | Initializes PDFBox once before document operations |
| `PdfFileNamePolicy.kt` | Sanitizes names and normalizes the `.pdf` extension |

## `core.pdf.write`

This package performs PDF document transformations.

| File | Purpose |
|---|---|
| `MergePdfWriter.kt` | Writes selected PDFs in the requested order and page layout |
| `SplitPdfWriter.kt` | Generates split preview and writes each validated output part |
| `ImagesPdfWriter.kt` | Creates A4 PDF pages from ordered images |
| `CompressPdfWriter.kt` | Prepares original and compressed candidates and selects the smaller file |
| `PdfCompressor.kt` | Finds image resources, scales and replaces them with smaller JPEG data |
| `PageEditorWriter.kt` | Applies one page move or deletion to a cached PDF |

## `core.system.export`

This package defines the shared PDF save process.

| File | Purpose |
|---|---|
| `PdfExportRequest.kt` | Defines single-file and folder export requests |
| `PdfExportFile.kt` | Couples a prepared temporary file with its cleanup action |
| `PdfExportError.kt` | Defines common PDF processing and export exceptions |
| `PdfExportHost.kt` | Manages file naming, destination selection, export, result reporting, and partial-file cleanup |

## `core.system.files`

This package handles URI access, temporary files, and cache file sharing.

| File | Purpose |
|---|---|
| `DestinationWriter.kt` | Handles file and URI copying, document creation in selected folders, and failed-output cleanup |
| `TempFileStore.kt` | Creates cache directories and temporary PDFs and removes older generated files |
| `AppFileProvider.kt` | Converts cache file into a `content://` URI |

## `core.system.permissions`

This package manages runtime permissions and permission dialogs.

| File | Purpose |
|---|---|
| `AppPermissions.kt` | Checks storage and camera access and owns related Activity Result launchers |
| `PermissionDialog.kt` | Displays the permission explanation and grant actions |

## `core.system.pickers`

This package provides shared file, folder, and camera pickers.

| File | Purpose |
|---|---|
| `FilePickers.kt` | Wraps PDF, image, folder, output-document, and camera Activity Result contracts behind one interface |

## `core.system.toast`

This package manages in-app toast notifications.

| File | Purpose |
|---|---|
| `ToastHost.kt` | Provides the notification sender, ViewModel binding, queue behavior, timing, and custom animated toast UI |

## `feature.pdflist`

This package implements the document library and file actions.

| File | Purpose |
|---|---|
| `PdfListViewModel.kt` | Loads the catalog and manages search, sorting, selection, details, dialogs, metadata, passwords, rename, delete, and sharing |
| `PdfListActions.kt` | Defines actions handled by `AppEventDispatcher` |
| `PdfListModels.kt` | Defines available file actions and their UI metadata |
| `PdfListScreen.kt` | Displays loading, empty, and document-list content |
| `DocumentInfoRow.kt` | Displays document name and metadata |
| `PdfListTopBar.kt` | Provides search, sorting, and normal top-bar actions |
| `PdfListSelectionBar.kt` | Provides actions for multiple selected documents |
| `PdfListOptionsSheet.kt` | Displays actions available for one document |
| `PdfDetailsSheet.kt` | Displays document path, size, page count, and timestamps |
| `PdfListDialogs.kt` | Contains rename, delete, metadata, and password dialogs |

## `feature.merge`

This package manages PDF merging, document order, and page layout.

| File | Purpose |
|---|---|
| `MergeViewModel.kt` | Manages selected PDFs, order, page layout, preview generation, and export requests |
| `MergeScreen.kt` | Chooses between empty and active content |
| `MergeActiveScreen.kt` | Displays the reorderable document list and merge options |
| `MergeTopBar.kt` | Provides add, save, and clear actions |

## `feature.split`

This package manages PDF splitting methods, validation, and output settings.

| File | Purpose |
|---|---|
| `SplitViewModel.kt` | Manages the source PDF, split configuration, validation result, preview, and folder export |
| `SplitScreen.kt` | Chooses between empty and active content |
| `SplitActiveScreen.kt` | Displays split methods, inputs, validation summary, and layout options |
| `SplitTopBar.kt` | Provides source selection, save, and clear actions |

## `feature.images`

This package creates PDF documents from selected images.

| File | Purpose |
|---|---|
| `ImagesModels.kt` | Defines the selected image model |
| `ImageReader.kt` | Reads image name, dimensions, and size from a URI |
| `ImagesViewModel.kt` | Manages selected images, ordering, layout, preview, and export |
| `ImagesScreen.kt` | Chooses between empty and active content |
| `ImagesActiveScreen.kt` | Displays the reorderable image list, thumbnails, and layout options |
| `ImagesTopBar.kt` | Provides gallery, camera, save, and clear actions |

## `feature.compress`

This package manages PDF compression settings and export.

| File | Purpose |
|---|---|
| `CompressionPreset.kt` | Defines JPEG quality and maximum image dimensions for each preset |
| `CompressViewModel.kt` | Stores the selected PDF and preset and creates the export request |
| `CompressSheet.kt` | Displays preset selection and save controls |

## `feature.pageeditor`

This package provides page reordering, deletion, and live preview.

| File | Purpose |
|---|---|
| `PageEditorModels.kt` | Defines stable page identity and thumbnail position |
| `PageEditorViewModel.kt` | Manages the editing session, page order, deletion, live preview, rollback, and export |
| `PageEditorHost.kt` | Provides page editor navigation and the animated full-size overlay |
| `PageEditorScreen.kt` | Displays the PDF viewer, page drawer, thumbnails, drag controls, delete and save actions |

## `feature.preview`

This package displays existing and generated PDF previews.

| File | Purpose |
|---|---|
| `PreviewModels.kt` | Defines requests for existing documents and generated PDF previews |
| `PreviewHost.kt` | Provides preview navigation and the animated full-size overlay |
| `PreviewScaffold.kt` | Combines preview bars, content, insets, and search state |
| `PreviewScreen.kt` | Opens an existing PDF or prepares split preview asynchronously |
| `PdfPreviewFragment.kt` | Configures AndroidX `PdfViewerFragment` |
| `PdfPreviewView.kt` | Hosts the PDF viewer fragment inside Compose |
| `PreviewTopBar.kt` | Provides back, title, and text-search actions |
| `PreviewBottomBar.kt` | Provides the lower preview layout area |

## `feature.settings`

This package manages application preferences and information.

| File | Purpose |
|---|---|
| `AppLanguage.kt` | Defines supported languages and resolves locale codes |
| `SettingsViewModel.kt` | Stores theme and language preferences and handles repository, license, and sharing actions |
| `SettingsScreen.kt` | Displays appearance, language, and application information settings |
| `SettingsTopBar.kt` | Provides the settings title bar |

## `ui.components`

This package provides reusable UI components shared across features.

| File | Purpose |
|---|---|
| `PdfThumbnail.kt` | Displays a PDF thumbnail or the correct locked and missing-thumbnail fallback |
| `PagesPerSheetSelector.kt` | Provides the shared 1, 2, or 4 pages-per-sheet control |

## `ui.theme`

This package defines application colors, typography, and theme behavior.

| File | Purpose |
|---|---|
| `Color.kt` | Defines light and dark semantic color palettes and exposes them through `LocalAppPalette` |
| `Theme.kt` | Applies palette, Material color scheme, shapes, typography, and system bar appearance |
| `Type.kt` | Defines application typography |

## Resources and build files

| Path | Purpose |
|---|---|
| `src/main/AndroidManifest.xml` | Declares activity, permissions, PDF intents, launcher icon, and `FileProvider` |
| `src/main/res/values/strings.xml` | Contains default strings and plurals |
| `src/main/res/values-*/strings.xml` | Contains translated resources |
| `src/main/res/xml/file_paths.xml` | Defines paths exposed through `FileProvider` |
| `src/main/res/mipmap-*` | Contains launcher icon assets |
| `app/build.gradle.kts` | Defines Android SDK levels, application version, Compose support, and dependencies |
| `gradle/libs.versions.toml` | Stores plugin and library versions |