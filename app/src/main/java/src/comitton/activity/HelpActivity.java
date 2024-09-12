package src.comitton.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

import io.documentnode.epub4j.domain.Resource;
import jp.dip.muracoro.comittonx.R;

public class HelpActivity extends Activity {

    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Intentを取得する
        Intent intent = getIntent();
        String url = intent.getStringExtra("Url");

        mWebView = (WebView) new WebView(this);
        mWebView.loadUrl("file:///android_asset/" + url);
        setContentView(mWebView);

    }
}
