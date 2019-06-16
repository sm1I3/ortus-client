package com.sm1l3.ortus_client

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
import android.webkit.WebView
import android.webkit.WebViewClient
import com.sm1l3.ortus_client.database.Database
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

private const val MAX_PROGRESS = 100
private const val HOME_URL = "https://ortus.rtu.lv/"
private const val LOGIN_URL = "https://id2.rtu.lv/openam/UI/Login"
private const val NEWS_URL = "https://ortus.rtu.lv/f/u101l1s5/normal/render.uP"
private const val LEARNING_URL = "https://ortus.rtu.lv/f/u108l1s5/normal/render.uP"

class Browser(
    private val webView: WebView,
    private val progressBar: MaterialProgressBar,
    private val database: Database
) {
    private var justLoggedIn = false

    init {
        webView.webViewClient = createWebViewClient()
        setupWebView()
        setWebChromeClient()
    }

    fun start() = loadUrl(HOME_URL)

    private fun loadUrl(url: String) = webView.loadUrl(url)

    private fun setWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) = with(progressBar) {
                visibility = when {
                    isShown && newProgress == MAX_PROGRESS -> View.GONE
                    else -> View.VISIBLE
                }
            }
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            setAppCacheEnabled(true)
            layoutAlgorithm = TEXT_AUTOSIZING
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.apply {
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            isScrollbarFadingEnabled = true
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }

    private fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) = skipNewsIfNeeded(view, url)

            override fun onPageFinished(view: WebView?, url: String?) = loginIfNeeded(view, url)
        }
    }

    private fun skipNewsIfNeeded(view: WebView?, url: String?) {
        url ?: return; view ?: return

        if (!justLoggedIn || !url.contains(NEWS_URL)) {
            return
        }

        justLoggedIn = false

        view.loadUrl(LEARNING_URL)
    }

    private fun loginIfNeeded(view: WebView?, url: String?) {
        url ?: return; view ?: return

        if (justLoggedIn || !url.contains(LOGIN_URL)) {
            return
        }

        val user = database.getCurrentUser() ?: return

        injectCookies(view)

        view.evaluateJavascript(
            """(function() {
                    document.getElementById('IDToken1').value='${user.login}';
                    document.getElementById('IDToken2').value='${user.password}';
                    javascript:LoginSubmit('Pieteikties');
               })();"""
        ) {}

        justLoggedIn = true
    }

    private fun injectCookies(view: WebView) {
        view.evaluateJavascript(
            """(function(){
                    document.cookie = \"agreeCookieRules=true; path=/; expires=Fri, 31 Dec 9999 23:59:59 GMT\";
                })();"""
        ) { }
    }
}