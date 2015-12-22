package com.opticalix.component;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.opticalix.base.BaseActivity;
import com.opticalix.widget_reminder.R;

/**
 * Created by opticalix@gmail.com on 15/12/22.
 */
public class AboutActivity extends BaseActivity {

    private WebView webview;
    private ProgressBar mProgressBar;
    private final String mAboutPageUrl = "http://opticalix.github.io/about/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_about);
        webview = (WebView) findViewById(R.id.web_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
//                activity.setProgress(progress * 1000);
                mProgressBar.setProgress(progress * 1000);
                if (progress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        webview.loadUrl(mAboutPageUrl);
    }
}
