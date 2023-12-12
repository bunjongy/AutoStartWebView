package com.yosapa.auto_start_webview;

import android.annotation.SuppressLint;
import android.net.http.SslError;
import android.os.Handler;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {
    private String TAG = "MyWebViewClient";

    @SuppressLint("WebViewClientOnReceivedSslError")
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed(); // Ignore SSL certificate errors
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        Log.i(TAG, "onReceivedError=" + error.getDescription() + " code=" + error.getErrorCode());
        if (error.getErrorCode()==WebViewClient.ERROR_CONNECT){
/*            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.reload();
                }
            },5000);*/
        }
    }
}
