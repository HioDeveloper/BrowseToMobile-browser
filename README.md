# BrowseToMobile Browser

**A lightweight Android web browser built with Java and Android WebView.**

BrowseToMobile Browser provides a simple browsing experience with tab management, customizable settings, desktop and mobile browsing modes, and support for modern web features.

## Features

- **Multi-tab browsing** — create, switch between, and close tabs.
- **Address bar** — enter website URLs or search directly.
- **Built-in start page** — quick access to Google, YouTube, Wikipedia, and GitHub.
- **Custom homepage** — use the built-in start page or set your own URL.
- **Custom search engine URL** — configure a search URL using the `{text}` placeholder.
- **Mobile and desktop modes** — switch between mobile and desktop User-Agent settings.
- **JavaScript control** — enable or disable JavaScript.
- **Downloads** — download files using Android DownloadManager.
- **File uploads** — select files through the browser's file chooser.
- **Geolocation** — support for websites requesting location access.
- **Camera and microphone** — WebRTC permission support.
- **Fullscreen support** — custom fullscreen views and video playback.
- **External links** — handle `mailto:`, `tel:`, `sms:`, `geo:`, `intent:`, and `market:` links.
- **Browser history navigation** — use the Android back button to navigate backward through pages.

## Browser Settings

BrowseToMobile Browser supports several configurable options:

| Setting | Description |
|---|---|
| Homepage | Choose the start page or a custom URL |
| Search URL | Configure the search engine endpoint |
| User-Agent | Switch between mobile and desktop modes |
| JavaScript | Enable or disable JavaScript |
| Zoom | Configure page zoom behavior |

## Technical Details

| Property | Value |
|---|---|
| Application name | B2M Browser |
| Package name | `com.hiosw.b2m` |
| Language | Java |
| Browser engine | Android WebView |
| Minimum Android version | Android 6.0 (API 23) |
| Target SDK | Android 10 (API 29) |
| Version | 1.0 |

## Limitations

- Downloads from `blob:` and `data:` URLs are not supported.
- Website functionality may depend on the Android WebView version installed on the device.
- Some websites may behave differently in mobile and desktop modes.

## License

See the repository's license information.

---

**BrowseToMobile Browser**  
Developed by **HioSW**.

*Building things that actually work.*
