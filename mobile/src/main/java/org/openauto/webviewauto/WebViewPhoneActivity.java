package org.openauto.webviewauto;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewPhoneActivity extends AppCompatActivity {
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_phone_main);

        String homeURL = getResources().getString(R.string.home_url);

        WebView webview = findViewById(R.id.webview_phone_component);
        webview.setBackgroundColor(0x3b3b3b);
        WebSettings wvset = webview.getSettings();
        wvset.setJavaScriptEnabled(true);
        wvset.setDomStorageEnabled(true);

        SwipeRefreshLayout layout = findViewById(R.id.phone_layout);
        layout.setOnRefreshListener(webview::reload);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview,true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                layout.setRefreshing(false);
            }
        });

        webview.loadUrl(homeURL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        findViewById(R.id.webview_phone_blur).requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.webview_phone_component).requestFocus();
    }
}
