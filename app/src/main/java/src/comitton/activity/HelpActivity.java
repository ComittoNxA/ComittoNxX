package src.comitton.activity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import jp.dip.muracoro.comittonx.R;

public class HelpActivity extends AppCompatActivity {

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
