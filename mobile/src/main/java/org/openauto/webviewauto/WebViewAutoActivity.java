package org.openauto.webviewauto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.wifi.WifiManager;

import com.google.android.apps.auto.sdk.CarActivity;

import org.openauto.webviewauto.fragments.BrowserFragment;

public class WebViewAutoActivity extends CarActivity {
    private static final String CURRENT_FRAGMENT_KEY = "app_current_fragment";
    private String mCurrentFragmentTag;

    public String homeURL;

    private boolean mustRestoreWifi;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme_Car);
        super.onCreate(bundle);
        setContentView(R.layout.activity_car_main);

        FragmentManager fragmentManager = getSupportFragmentManager();

        BrowserFragment browserFragment = new BrowserFragment();

        //Add fragments
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, browserFragment, BrowserFragment.TAG)
                .detach(browserFragment)
                .commitNow();

        String initialFragmentTag = BrowserFragment.TAG;

        if (bundle != null && bundle.containsKey(CURRENT_FRAGMENT_KEY)) {
            initialFragmentTag = bundle.getString(CURRENT_FRAGMENT_KEY);
        }
        switchToFragment(initialFragmentTag);

        homeURL = getResources().getString(R.string.home_url);

        //Status bar controller
        getCarUiController().getMenuController().hideMenuButton();
        getCarUiController().getStatusBarController().hideMicButton();
        getCarUiController().getStatusBarController().hideTitle();
        getCarUiController().getStatusBarController().hideAppHeader();
        getCarUiController().getStatusBarController().setAppBarAlpha(0f);

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(mFragmentLifecycleCallbacks, false);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString(CURRENT_FRAGMENT_KEY, mCurrentFragmentTag);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onStart() {
        super.onStart();
        switchToFragment(mCurrentFragmentTag);
    }

    @Override
    public void onStop() {
        super.onStop();
        restoreWifi();
    }

    private void disableWifi() {
        WifiManager wifimgr = wifiManager();
        if(!mustRestoreWifi) {
            mustRestoreWifi = true;
            int state = wifimgr.getWifiState();
            if (state == WifiManager.WIFI_STATE_DISABLED || state == WifiManager.WIFI_STATE_DISABLING) {
                mustRestoreWifi = false;
                return;
            }
        }
        wifimgr.setWifiEnabled(false);
    }

    private void restoreWifi() {
        if(mustRestoreWifi) {
            mustRestoreWifi = false;
            wifiManager().setWifiEnabled(true);
        }
    }

    private WifiManager wifiManager() {
        return (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void switchToFragment(String tag) {
        if (tag.equals(mCurrentFragmentTag)) {
            return;
        }
        FragmentManager manager = getSupportFragmentManager();
        Fragment currentFragment = mCurrentFragmentTag == null ? null : manager.findFragmentByTag(mCurrentFragmentTag);
        Fragment newFragment = manager.findFragmentByTag(tag);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != null) {
            transaction.detach(currentFragment);
        }
        transaction.attach(newFragment);
        transaction.commit();
        mCurrentFragmentTag = tag;
    }

    private final FragmentManager.FragmentLifecycleCallbacks mFragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentStarted(FragmentManager fm, Fragment f) {
            updateStatusBarTitle();
            updateFragmentContent(f);
        }
    };

    public void updateStatusBarTitle() {
        CarFragment fragment = (CarFragment) getSupportFragmentManager().findFragmentByTag(mCurrentFragmentTag);
        getCarUiController().getStatusBarController().setTitle(fragment.getTitle());
    }

    public void updateFragmentContent(Fragment fragment) {
        if(fragment instanceof BrowserFragment){
            updateBrowserFragment(fragment);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void updateBrowserFragment(Fragment fragment) {
        //load web view
        WebView webview = webview();
        WebSettings wvset = webview.getSettings();
        wvset.setJavaScriptEnabled(true);
        wvset.setDomStorageEnabled(true);
        wvset.setUseWideViewPort(true);
        wvset.setLoadWithOverviewMode(true);

        SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(R.id.root_layout);
        layout.setOnRefreshListener(webview::reload);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview,true);
        webview.setWebChromeClient(new WebChromeClient());
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                layout.setRefreshing(false);
            }
        });
        webview.addJavascriptInterface(new Object() {
            @JavascriptInterface
            @SuppressWarnings("unused")
            public void disableWifi() {
                WebViewAutoActivity.this.disableWifi();
            }
        }, "AADriveway");

        webview.loadUrl(homeURL);
    }

    private WebView webview() {
        return (WebView) findViewById(R.id.webview_component);
    }
}
