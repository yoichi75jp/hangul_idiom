package com.aufthesis.hangul_idiom;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

//import android.support.v7.app.AppCompatActivity;


/**
 // Created by yoichi75jp2 on 2017/07/04.
 */

public class WebBrowserActivity extends Activity {

    private WebView  m_webView;
    private AdView m_adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webbrowser);

//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null)
//            actionBar.setDisplayHomeAsUpEnabled(true);

        //レイアウトで指定したWebViewのIDを指定する。
        m_webView = findViewById(R.id.webView);

        //リンクをタップしたときに標準ブラウザを起動させない
        m_webView.setWebViewClient(new WebViewClient());
        //拡大縮小機能をオンにする
        m_webView.getSettings().setSupportZoom(true);
        //JavaScriptの設定をオンにする
        m_webView.getSettings().setJavaScriptEnabled(true);

        Intent intent = getIntent();
        String query = intent.getExtras().getString(SearchManager.QUERY);
        String url = "https://www.google.co.jp/search?q=" + query;
        m_webView.loadUrl(url);

        //バナー広告
        m_adView = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        if(!MainActivity.g_isDebug)
            m_adView.loadAd(adRequest);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            finish();
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(m_webView.canGoBack())
                m_webView.goBack();
            else
                finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (m_adView != null) {
            m_adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (m_adView != null) {
            m_adView.pause();
        }
        super.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onDestroy()
    {
        if (m_adView != null) {
            m_adView.destroy();
        }
        super.onDestroy();
        setResult(RESULT_OK);
    }

}
