# BrowseToMobile Browser

**A lightweight Android web browser built for convenient everyday browsing.**

BrowseToMobile (B2M) is an Android browser based on the native Android WebView engine. It provides a straightforward browsing experience with tab management, customizable search settings, mobile and desktop modes, and support for modern web content.

## Features

- **Tabbed Browsing** — Open, switch between, and close multiple browser tabs.
- **Address Bar** — Navigate directly to websites or search the web using your configured search engine.
- **Custom Start Page** — Built-in start page with a search field and quick links to popular websites.
- **Mobile Mode** — Optimizes websites for mobile screens using a mobile User-Agent and viewport adjustments.
- **Desktop Mode** — Browse websites using a desktop browser User-Agent.
- **JavaScript Support** — Enable or disable JavaScript through browser settings.
- **Download Manager** — Download files directly to the Android Downloads directory.
- **Fullscreen Support** — View fullscreen web content, including video playback.
- **Web Compatibility** — Supports cookies, DOM storage, website zooming, and multiple browser windows.
- **Geolocation Support** — Allows websites to request access to device location.
- **File Uploads** — Supports file selection and uploads through web forms.
- **External App Integration** — Handles links such as `mailto:`, `tel:`, `sms:`, `geo:`, and Android intent URLs.

## Screenshots

Screenshots will be added here.

## Browser Settings

BrowseToMobile provides several configurable options:

| Setting | Description |
|---|---|
| Homepage | Configure the browser's homepage or use the built-in start page. |
| Search Engine | Set a custom search URL using `{text}` as the search query placeholder. |
| Mobile Mode | Switch between mobile and desktop browsing modes. |
| JavaScript | Enable or disable JavaScript execution. |

### Custom Search Engine

You can configure your preferred search provider using a URL template.

Example:

```text
https://www.google.com/search?q={text}
```

The `{text}` placeholder is automatically replaced with your search query.

## Technology

- **Platform:** Android
- **Language:** Java
- **Browser Engine:** Android WebView
- **Application ID:** `com.hiosw.b2m`
- **Minimum Android SDK:** 23 (Android 6.0)
- **Target Android SDK:** 29
- **UI:** Native Android UI

## Project Structure

```text
BrowseToMobile-browser/
├── .github/
│   └── workflows/
│       ├── blank.yml
│       └── extract-archive.yml
├── resources/
│   ├── AndroidManifest.xml
│   ├── META-INF/
│   └── res/
└── sources/
    ├── android/
    ├── androidx/
    └── com/
        └── hiosw/
            └── b2m/
                ├── BuildConfig.java
                ├── MainActivity.java
                └── R.java
```

## Permissions

The application declares Android permissions for:

- Internet access
- Network state
- Camera
- Microphone and audio settings
- Device location
- Storage and file access
- Notifications

Some permissions are requested only when the corresponding functionality is used.

## Source Code

This repository contains the Android application source files and resources.

The source archive import workflow is available through GitHub Actions:

1. Place the source ZIP archive in the repository root.
2. Open **Actions**.
3. Select **Extract browser archive**.
4. Run the workflow manually.

The workflow extracts the `android-app-b2m-source` directory, imports its contents into the repository root, and commits the changes.

## Development

The repository currently contains extracted Android source files and resources. A complete Gradle build configuration is not included.

## About

BrowseToMobile Browser is a HioSW project focused on providing a simple and practical Android browsing experience.

**Building things that actually work.**

Developed by **HioSW**.

- GitHub: https://github.com/HioDeveloper
- Project: https://github.com/HioDeveloper/BrowseToMobile-browser

---

*BrowseToMobile — Browse the web your way.*
