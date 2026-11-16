package com.hiosw.b2m;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class MainActivity extends Activity {
    static final String DEFAULT_HOME = "start";
    static final String DEFAULT_SEARCH = "https://www.google.com/search?q={text}";
    static final String DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    static final String MOBILE_UA = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
    static final int REQ_DOWNLOAD = 500;
    static final int REQ_FILE_CHOOSER = 100;
    static final int REQ_GEO = 300;
    static final int REQ_WEBRTC = 400;
    TextView btnHome;
    ImageButton btnNewTab;
    TextView btnSettings;
    View customView;
    WebChromeClient.CustomViewCallback customViewCallback;
    ValueCallback<Uri[]> filePathCallback;
    FrameLayout fullscreenContainer;
    GeolocationPermissions.Callback geoCallback;
    String geoOrigin;
    Button goButton;
    String pendingDlDisposition;
    String pendingDlMime;
    String pendingDlUa;
    String pendingDlUrl;
    SharedPreferences prefs;
    ProgressBar progressBar;
    LinearLayout tabContainer;
    LinearLayout urlBar;
    TextView urlIcon;
    EditText urlInput;
    FrameLayout webContainer;
    PermissionRequest webRtcRequest;
    final List<Tab> tabs = new ArrayList();
    int currentIndex = -1;
    private boolean closingTab = false;

    static class Tab {
        String title = "Новая вкладка";
        String url = "about:start";
        WebView webView;

        Tab() {
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        this.prefs = getSharedPreferences("b2m", 0);
        setContentView(R.layout.activity_main);
        this.tabContainer = (LinearLayout) findViewById(R.id.tab_container);
        this.urlBar = (LinearLayout) findViewById(R.id.url_bar);
        this.btnNewTab = (ImageButton) findViewById(R.id.btn_new_tab);
        this.btnSettings = (TextView) findViewById(R.id.btn_settings);
        this.btnHome = (TextView) findViewById(R.id.btn_home);
        this.urlIcon = (TextView) findViewById(R.id.url_icon);
        this.urlInput = (EditText) findViewById(R.id.url_input);
        this.goButton = (Button) findViewById(R.id.go_button);
        this.progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        this.webContainer = (FrameLayout) findViewById(R.id.web_container);
        this.fullscreenContainer = (FrameLayout) findViewById(R.id.fullscreen_container);
        setupUrlBar();
        this.btnNewTab.setOnClickListener(new View.OnClickListener() { // from class: com.hiosw.b2m.MainActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MainActivity.this.createTab(MainActivity.DEFAULT_HOME);
            }
        });
        this.btnHome.setOnClickListener(new View.OnClickListener() { // from class: com.hiosw.b2m.MainActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MainActivity.this.goHome();
            }
        });
        this.btnSettings.setOnClickListener(new View.OnClickListener() { // from class: com.hiosw.b2m.MainActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MainActivity.this.showSettings();
            }
        });
        createTab(DEFAULT_HOME);
    }

    void setupUrlBar() {
        this.urlInput.setBackground(makeRoundBg(-15066598, 18));
        this.goButton.setBackground(makeRoundBg(-12944427, 18));
        this.urlBar.setBackground(makeRoundBg(-13816531, 0));
        this.goButton.setOnClickListener(new View.OnClickListener() { // from class: com.hiosw.b2m.MainActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                MainActivity.this.loadUrl();
            }
        });
        this.urlInput.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.hiosw.b2m.MainActivity.5
            @Override // android.widget.TextView.OnEditorActionListener
            public boolean onEditorAction(TextView v, int a, KeyEvent e) {
                if (a != 2) {
                    return false;
                }
                MainActivity.this.loadUrl();
                return true;
            }
        });
        this.urlInput.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.hiosw.b2m.MainActivity.6
            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    MainActivity.this.urlInput.selectAll();
                }
                MainActivity.this.updateUrlBarStyle();
            }
        });
    }

    GradientDrawable makeRoundBg(int color, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(dp(radiusDp));
        return gd;
    }

    void loadUrl() {
        Tab t = currentTab();
        if (t == null) {
            return;
        }
        String url = this.urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            return;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file:") && !url.startsWith("about:")) {
            if (url.contains(" ") || !url.contains(".")) {
                String sUrl = this.prefs.getString("search_url", DEFAULT_SEARCH);
                url = sUrl.replace("{text}", Uri.encode(url));
            } else {
                url = "https://" + url;
            }
        }
        t.webView.loadUrl(url);
        this.urlInput.clearFocus();
    }

    void updateUrlBarStyle() {
        int bg;
        int iconColor;
        String icon;
        Tab t = currentTab();
        String u = t != null ? t.webView.getUrl() : null;
        if (this.urlInput.hasFocus()) {
            bg = -12961222;
            iconColor = -5197648;
            icon = "✎";
        } else if (u == null || u.equals("about:blank") || u.startsWith("data:")) {
            bg = -13816531;
            iconColor = -7829368;
            icon = "🌐";
        } else if (u.startsWith("https://")) {
            bg = -13816531;
            iconColor = -11751600;
            icon = "🔒";
        } else if (u.startsWith("http://")) {
            bg = -12964563;
            iconColor = -26624;
            icon = "⚠";
        } else {
            bg = -13816531;
            iconColor = -7829368;
            icon = "🌐";
        }
        this.urlBar.setBackground(makeRoundBg(bg, 0));
        this.urlInput.setBackground(makeRoundBg(-15066598, 18));
        this.urlIcon.setText(icon);
        this.urlIcon.setTextColor(iconColor);
    }

    Tab currentTab() {
        int i = this.currentIndex;
        if (i < 0 || i >= this.tabs.size()) {
            return null;
        }
        return this.tabs.get(this.currentIndex);
    }

    WebView getActiveWebView() {
        Tab t = currentTab();
        if (t == null) {
            return null;
        }
        return t.webView;
    }

    void createTab(String url) {
        Tab tab = new Tab();
        tab.webView = buildWebView();
        this.webContainer.addView(tab.webView, new FrameLayout.LayoutParams(-1, -1));
        this.tabs.add(tab);
        switchTo(this.tabs.size() - 1);
        loadInto(tab, url);
    }

    void loadInto(Tab t, String url) {
        if (url == null || url.isEmpty() || url.equals(DEFAULT_HOME) || url.equals("about:start")) {
            t.url = "about:start";
            t.webView.loadDataWithBaseURL(null, buildStartPage(), "text/html", "UTF-8", null);
        } else {
            t.url = url;
            t.webView.loadUrl(url);
        }
    }

    void goHome() {
        Tab t = currentTab();
        if (t == null) {
            createTab(DEFAULT_HOME);
        } else {
            loadInto(t, this.prefs.getString("home_page", DEFAULT_HOME));
        }
    }

    void switchTo(int idx) {
        if (idx < 0 || idx >= this.tabs.size()) {
            return;
        }
        this.currentIndex = idx;
        int i = 0;
        while (i < this.tabs.size()) {
            this.tabs.get(i).webView.setVisibility(i == idx ? 0 : 8);
            i++;
        }
        Tab t = this.tabs.get(idx);
        String url = t.webView.getUrl();
        if (url == null || url.equals("about:blank") || url.startsWith("data:") || "about:start".equals(t.url)) {
            this.urlInput.setText(BuildConfig.FLAVOR);
        } else {
            this.urlInput.setText(url);
        }
        updateTabBar();
        updateUrlBarStyle();
    }

    void closeTab(final int idx) {
        if (this.closingTab || idx < 0 || idx >= this.tabs.size()) {
            return;
        }
        this.closingTab = true;
        this.tabContainer.post(new Runnable() { // from class: com.hiosw.b2m.MainActivity.7
            @Override // java.lang.Runnable
            public void run() {
                try {
                    MainActivity.this.doCloseTab(idx);
                } finally {
                    MainActivity.this.closingTab = false;
                }
            }
        });
    }

    void doCloseTab(int idx) {
        if (idx < 0 || idx >= this.tabs.size()) {
            return;
        }
        Tab t = this.tabs.remove(idx);
        t.webView.setWebChromeClient(null);
        t.webView.setWebViewClient(null);
        t.webView.stopLoading();
        if (t.webView.getParent() != null) {
            this.webContainer.removeView(t.webView);
        }
        try {
            t.webView.destroy();
        } catch (Exception e) {
        }
        if (this.tabs.isEmpty()) {
            createTab(DEFAULT_HOME);
            return;
        }
        int i = this.currentIndex;
        if (i > idx) {
            this.currentIndex = i - 1;
        } else if (i == idx) {
            this.currentIndex = Math.min(idx, this.tabs.size() - 1);
        }
        if (this.currentIndex >= this.tabs.size()) {
            this.currentIndex = this.tabs.size() - 1;
        }
        if (this.currentIndex < 0) {
            this.currentIndex = 0;
        }
        switchTo(this.currentIndex);
    }

    void updateTabBar() {
        this.tabContainer.removeAllViews();
        for (int i = 0; i < this.tabs.size(); i++) {
            final int idx = i;
            Tab t = this.tabs.get(i);
            boolean active = idx == this.currentIndex;
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(0);
            ll.setGravity(16);
            ll.setPadding(dp(10), dp(4), dp(4), dp(4));
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(active ? -1 : -12961222);
            bg.setCornerRadii(new float[]{dp(8), dp(8), dp(8), dp(8), 0.0f, 0.0f, 0.0f, 0.0f});
            ll.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(150), dp(40));
            lp.setMargins(dp(4), dp(6), 0, 0);
            ll.setLayoutParams(lp);
            TextView tv = new TextView(this);
            tv.setText((t.title == null || t.title.isEmpty()) ? "Без названия" : t.title);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setTextColor(active ? -14540254 : -1118482);
            tv.setTextSize(12.0f);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.0f));
            ll.addView(tv);
            TextView close = new TextView(this);
            close.setText("✕");
            close.setTextSize(14.0f);
            close.setPadding(dp(8), 0, dp(4), 0);
            close.setTextColor(active ? -7829368 : -5592406);
            close.setOnClickListener(new View.OnClickListener() { // from class: com.hiosw.b2m.MainActivity.8
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    MainActivity.this.closeTab(idx);
                }
            });
            ll.addView(close);
            ll.setOnClickListener(new View.OnClickListener() { // from class: com.hiosw.b2m.MainActivity.9
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    MainActivity.this.switchTo(idx);
                }
            });
            this.tabContainer.addView(ll);
        }
    }

    void updateTabMeta(WebView v, String url, String title) {
        for (int i = 0; i < this.tabs.size(); i++) {
            if (this.tabs.get(i).webView == v) {
                if (url != null) {
                    this.tabs.get(i).url = url;
                }
                if (title != null && !title.isEmpty()) {
                    this.tabs.get(i).title = title;
                }
                updateTabBar();
                return;
            }
        }
    }

    WebView buildWebView() {
        WebView wv = new WebView(this);
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(this.prefs.getBoolean("js_enabled", true));
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setUserAgentString(this.prefs.getBoolean("mobile_mode", true) ? MOBILE_UA : DESKTOP_UA);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(-1);
        if (Build.VERSION.SDK_INT >= 21) {
            s.setMixedContentMode(0);
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        }
        CookieManager.getInstance().setAcceptCookie(true);
        wv.setWebViewClient(new WebViewClient() { // from class: com.hiosw.b2m.MainActivity.10
            @Override // android.webkit.WebViewClient
            public void onPageStarted(WebView v, String url, Bitmap f) {
                if (url != null && !url.equals("about:blank") && !url.startsWith("data:")) {
                    MainActivity.this.updateTabMeta(v, url, null);
                    if (v == MainActivity.this.getActiveWebView()) {
                        MainActivity.this.urlInput.setText(url);
                        MainActivity.this.updateUrlBarStyle();
                    }
                }
                if (v == MainActivity.this.getActiveWebView()) {
                    MainActivity.this.progressBar.setVisibility(0);
                }
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView v, String url) {
                if (url != null && !url.equals("about:blank") && !url.startsWith("data:")) {
                    MainActivity.this.updateTabMeta(v, url, null);
                }
                if (v == MainActivity.this.getActiveWebView()) {
                    MainActivity.this.progressBar.setVisibility(8);
                    MainActivity.this.updateUrlBarStyle();
                }
                if (MainActivity.this.prefs.getBoolean("mobile_mode", true)) {
                    MainActivity.this.makeMobile(v);
                }
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("geo:") || url.startsWith("intent:") || url.startsWith("market:")) {
                    try {
                        MainActivity.this.startActivity(Intent.parseUri(url, 1));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Нет приложения", 0).show();
                    }
                    return true;
                }
                v.loadUrl(url);
                return true;
            }
        });
        wv.setWebChromeClient(new WebChromeClient() { // from class: com.hiosw.b2m.MainActivity.11
            @Override // android.webkit.WebChromeClient
            public void onProgressChanged(WebView v, int p) {
                if (v == MainActivity.this.getActiveWebView()) {
                    MainActivity.this.progressBar.setProgress(p);
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onReceivedTitle(WebView v, String title) {
                MainActivity.this.updateTabMeta(v, null, title);
            }

            @Override // android.webkit.WebChromeClient
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, WebChromeClient.FileChooserParams params) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = cb;
                Intent ci = new Intent("android.intent.action.GET_CONTENT");
                ci.addCategory("android.intent.category.OPENABLE");
                ci.setType("*/*");
                String[] accept = params.getAcceptTypes();
                if (accept != null && accept.length > 0 && accept[0] != null && !accept[0].trim().isEmpty()) {
                    String first = accept[0].trim();
                    if (first.contains("/")) {
                        if (accept.length == 1) {
                            ci.setType(first);
                        } else {
                            ci.putExtra("android.intent.extra.MIME_TYPES", accept);
                        }
                    }
                }
                try {
                    MainActivity.this.startActivityForResult(Intent.createChooser(ci, "Выбор файла"), 100);
                    return true;
                } catch (Exception e) {
                    MainActivity.this.filePathCallback = null;
                    return false;
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onPermissionRequest(PermissionRequest req) {
                if (Build.VERSION.SDK_INT < 23) {
                    req.grant(req.getResources());
                    return;
                }
                boolean needMic = false;
                boolean needCam = false;
                for (String r : req.getResources()) {
                    if ("android.webkit.resource.VIDEO_CAPTURE".equals(r)) {
                        needCam = true;
                    }
                    if ("android.webkit.resource.AUDIO_CAPTURE".equals(r)) {
                        needMic = true;
                    }
                }
                List<String> need = new ArrayList<>();
                if (needCam && !MainActivity.this.hasPermission("android.permission.CAMERA")) {
                    need.add("android.permission.CAMERA");
                }
                if (needMic && !MainActivity.this.hasPermission("android.permission.RECORD_AUDIO")) {
                    need.add("android.permission.RECORD_AUDIO");
                }
                if (!need.isEmpty()) {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.webRtcRequest = req;
                    mainActivity.requestPermissions((String[]) need.toArray(new String[0]), MainActivity.REQ_WEBRTC);
                    return;
                }
                req.grant(req.getResources());
            }

            @Override // android.webkit.WebChromeClient
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb) {
                if (Build.VERSION.SDK_INT >= 23 && !MainActivity.this.hasPermission("android.permission.ACCESS_FINE_LOCATION")) {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.geoCallback = cb;
                    mainActivity.geoOrigin = origin;
                    mainActivity.requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, MainActivity.REQ_GEO);
                    return;
                }
                cb.invoke(origin, true, false);
            }

            @Override // android.webkit.WebChromeClient
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback cb) {
                if (MainActivity.this.customView != null) {
                    cb.onCustomViewHidden();
                    return;
                }
                MainActivity mainActivity = MainActivity.this;
                mainActivity.customView = view;
                mainActivity.customViewCallback = cb;
                mainActivity.fullscreenContainer.addView(view, new FrameLayout.LayoutParams(-1, -1));
                MainActivity.this.fullscreenContainer.setVisibility(0);
                MainActivity.this.getWindow().addFlags(1024);
                if (MainActivity.this.getActiveWebView() != null) {
                    MainActivity.this.getActiveWebView().setVisibility(8);
                }
            }

            @Override // android.webkit.WebChromeClient
            public void onHideCustomView() {
                MainActivity.this.exitFullscreen();
            }

            @Override // android.webkit.WebChromeClient
            public boolean onCreateWindow(WebView v, boolean d, boolean u, Message msg) {
                MainActivity.this.createTab(null);
                WebView nv = MainActivity.this.getActiveWebView();
                ((WebView.WebViewTransport) msg.obj).setWebView(nv);
                msg.sendToTarget();
                return true;
            }
        });
        wv.setDownloadListener(new DownloadListener() { // from class: com.hiosw.b2m.MainActivity.12
            @Override // android.webkit.DownloadListener
            public void onDownloadStart(String url, String ua, String cd, String mime, long len) {
                boolean z = false;
                if (url.startsWith("blob:") || url.startsWith("data:")) {
                    Toast.makeText(MainActivity.this, "Blob/data не поддерживается", 0).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29 && !MainActivity.this.hasPermission("android.permission.WRITE_EXTERNAL_STORAGE")) {
                    z = true;
                }
                boolean needPerm = z;
                if (needPerm) {
                    MainActivity mainActivity = MainActivity.this;
                    mainActivity.pendingDlUrl = url;
                    mainActivity.pendingDlUa = ua;
                    mainActivity.pendingDlDisposition = cd;
                    mainActivity.pendingDlMime = mime;
                    mainActivity.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, MainActivity.REQ_DOWNLOAD);
                    return;
                }
                MainActivity.this.startDownload(url, ua, cd, mime);
            }
        });
        return wv;
    }

    void startDownload(String url, String ua, String cd, String mime) {
        String fileName = URLUtil.guessFileName(url, cd, mime);
        try {
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setMimeType(mime);
            if (ua != null) {
                req.addRequestHeader("User-Agent", ua);
            }
            if (cd != null) {
                req.addRequestHeader("Content-Disposition", cd);
            }
            req.setNotificationVisibility(1);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager dm = (DownloadManager) getSystemService("download");
            dm.enqueue(req);
            Toast.makeText(this, "Загрузка: " + fileName, 0).show();
        } catch (Exception e) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
            } catch (Exception e2) {
                Toast.makeText(this, "Ошибка скачивания", 0).show();
            }
        }
    }

    String buildStartPage() {
        String sUrl = this.prefs.getString("search_url", DEFAULT_SEARCH);
        String escaped = sUrl.replace("\\", "\\\\").replace("\"", "\\\"");
        return "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1'><style>body{margin:0;background:#1E1E1E;color:#fff;font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;padding:20px;box-sizing:border-box;}h1{color:#3A7BD5;font-size:42px;margin:0 0 8px;}p{color:#888;margin:0 0 24px;}form{width:100%;max-width:420px;}input{width:100%;padding:14px 18px;border-radius:24px;border:none;background:#2D2D2D;color:#fff;font-size:16px;box-sizing:border-box;}input::placeholder{color:#888;}input:focus{outline:none;box-shadow:0 0 0 2px #3A7BD5;}.links{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-top:32px;max-width:420px;width:100%;}.links a{color:#ccc;text-decoration:none;text-align:center;font-size:12px;padding:14px 6px;background:#2D2D2D;border-radius:12px;display:flex;flex-direction:column;align-items:center;gap:4px;}.links a:active{background:#3A7BD5;}.links a .ico{font-size:22px;}</style></head><body><h1>B2M</h1><p>Быстрый поиск</p><form onsubmit='return goSearch(event)'><input id='q' type='search' placeholder='Поиск или адрес' autofocus></form><div class='links'><a href='https://www.google.com'><span class='ico'>🔍</span>Google</a><a href='https://m.youtube.com'><span class='ico'>▶</span>YouTube</a><a href='https://ru.wikipedia.org'><span class='ico'>📖</span>Wiki</a><a href='https://github.com'><span class='ico'>💻</span>GitHub</a></div><script>function goSearch(e){e.preventDefault();var q=document.getElementById('q').value.trim();if(!q)return false;var u=\"" + escaped + "\".replace('{text}',encodeURIComponent(q));location.href=u;return false;}</script></body></html>";
    }

    void showSettings() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(1);
        ll.setPadding(dp(20), dp(8), dp(20), dp(8));
        scrollView.addView(ll);
        TextView lbl1 = new TextView(this);
        lbl1.setText("Домашняя страница");
        lbl1.setTextSize(13.0f);
        lbl1.setTextColor(-7829368);
        lbl1.setPadding(0, dp(12), 0, dp(4));
        ll.addView(lbl1);
        final EditText etHome = new EditText(this);
        etHome.setText(this.prefs.getString("home_page", DEFAULT_HOME));
        etHome.setHint("start = стартовая, или URL");
        ll.addView(etHome);
        TextView lbl2 = new TextView(this);
        lbl2.setText("Поиск ({text} = запрос)");
        lbl2.setTextSize(13.0f);
        lbl2.setTextColor(-7829368);
        lbl2.setPadding(0, dp(16), 0, dp(4));
        ll.addView(lbl2);
        final EditText etSearch = new EditText(this);
        etSearch.setText(this.prefs.getString("search_url", DEFAULT_SEARCH));
        ll.addView(etSearch);
        final CheckBox cbMobile = new CheckBox(this);
        cbMobile.setText("Мобильный режим (UA + viewport)");
        cbMobile.setChecked(this.prefs.getBoolean("mobile_mode", true));
        cbMobile.setPadding(0, dp(12), 0, 0);
        ll.addView(cbMobile);
        final CheckBox cbJs = new CheckBox(this);
        cbJs.setText("JavaScript");
        cbJs.setChecked(this.prefs.getBoolean("js_enabled", true));
        ll.addView(cbJs);
        new AlertDialog.Builder(this).setTitle("Настройки").setView(scrollView).setPositiveButton("Сохранить", new DialogInterface.OnClickListener() { // from class: com.hiosw.b2m.MainActivity.13
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface d, int w) {
                String home = etHome.getText().toString().trim();
                String search = etSearch.getText().toString().trim();
                if (home.isEmpty()) {
                    home = MainActivity.DEFAULT_HOME;
                }
                if (search.isEmpty() || !search.contains("{text}")) {
                    search = MainActivity.DEFAULT_SEARCH;
                }
                MainActivity.this.prefs.edit().putString("home_page", home).putString("search_url", search).putBoolean("mobile_mode", cbMobile.isChecked()).putBoolean("js_enabled", cbJs.isChecked()).apply();
                MainActivity.this.applySettings();
                Toast.makeText(MainActivity.this, "Сохранено", 0).show();
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    void applySettings() {
        boolean js = this.prefs.getBoolean("js_enabled", true);
        boolean mobile = this.prefs.getBoolean("mobile_mode", true);
        String ua = mobile ? MOBILE_UA : DESKTOP_UA;
        for (Tab t : this.tabs) {
            WebSettings ws = t.webView.getSettings();
            if (ws.getJavaScriptEnabled() != js) {
                ws.setJavaScriptEnabled(js);
            }
            if (!ua.equals(ws.getUserAgentString())) {
                ws.setUserAgentString(ua);
                t.webView.reload();
            }
        }
    }

    void makeMobile(WebView view) {
        view.loadUrl("javascript:(function(){try{var h=document.head||document.getElementsByTagName('head')[0];if(!h)return;var m=document.querySelector('meta[name=viewport]');if(!m){m=document.createElement('meta');m.setAttribute('name','viewport');h.appendChild(m);}m.setAttribute('content','width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes');var st=document.getElementById('__b2m_style__');if(!st){st=document.createElement('style');st.id='__b2m_style__';h.appendChild(st);}st.innerHTML='html,body{max-width:100%!important;overflow-x:hidden!important;}*{box-sizing:border-box!important;}img,video,iframe,canvas,table,pre{max-width:100%!important;height:auto!important;}table{display:block!important;overflow-x:auto!important;}';}catch(e){}})()");
    }

    boolean hasPermission(String p) {
        return Build.VERSION.SDK_INT < 23 || checkCallingOrSelfPermission(p) == 0;
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        String str;
        GeolocationPermissions.Callback callback;
        PermissionRequest permissionRequest;
        super.onRequestPermissionsResult(code, perms, res);
        boolean all = true;
        for (int r : res) {
            if (r != 0) {
                all = false;
            }
        }
        if (code == REQ_WEBRTC && (permissionRequest = this.webRtcRequest) != null) {
            if (all) {
                permissionRequest.grant(permissionRequest.getResources());
            } else {
                permissionRequest.deny();
                Toast.makeText(this, "Доступ запрещён", 0).show();
            }
            this.webRtcRequest = null;
            return;
        }
        if (code == REQ_GEO && (callback = this.geoCallback) != null) {
            callback.invoke(this.geoOrigin, all, false);
            this.geoCallback = null;
            return;
        }
        if (code == REQ_DOWNLOAD) {
            if (all && (str = this.pendingDlUrl) != null) {
                startDownload(str, this.pendingDlUa, this.pendingDlDisposition, this.pendingDlMime);
            } else if (!all) {
                Toast.makeText(this, "Нет разрешения", 0).show();
            }
            this.pendingDlMime = null;
            this.pendingDlDisposition = null;
            this.pendingDlUa = null;
            this.pendingDlUrl = null;
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != 100 || this.filePathCallback == null) {
            return;
        }
        Uri[] result = null;
        if (res == -1 && data != null) {
            if (data.getData() != null) {
                result = new Uri[]{data.getData()};
            } else if (data.getClipData() != null) {
                int n = data.getClipData().getItemCount();
                result = new Uri[n];
                for (int i = 0; i < n; i++) {
                    result[i] = data.getClipData().getItemAt(i).getUri();
                }
            }
        }
        this.filePathCallback.onReceiveValue(result);
        this.filePathCallback = null;
    }

    void exitFullscreen() {
        View view = this.customView;
        if (view == null) {
            return;
        }
        this.fullscreenContainer.removeView(view);
        this.customView = null;
        this.fullscreenContainer.setVisibility(8);
        WebView aw = getActiveWebView();
        if (aw != null) {
            aw.setVisibility(0);
        }
        getWindow().clearFlags(1024);
        WebChromeClient.CustomViewCallback customViewCallback = this.customViewCallback;
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            this.customViewCallback = null;
        }
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (this.customView != null) {
                exitFullscreen();
                return true;
            }
            WebView aw = getActiveWebView();
            if (aw != null && aw.canGoBack()) {
                aw.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        for (Tab t : this.tabs) {
            if (t.webView != null) {
                t.webView.loadUrl("about:blank");
                t.webView.destroy();
            }
        }
        this.tabs.clear();
        super.onDestroy();
    }
}
