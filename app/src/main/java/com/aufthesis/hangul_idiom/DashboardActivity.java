package com.aufthesis.hangul_idiom;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import android.support.v7.app.AppCompatActivity;

// Created by yoichi75jp2 on 2017/03/04.
public class DashboardActivity extends Activity {

    private Context m_context = null;

    static public final List<Integer> m_listMax =
            new ArrayList<>(Arrays.asList(50,100,200,300,400,500,750,1000,1500,2000,2500,3000,3500,4000,4500,5000,6000,7000,8000,9000,10000));

    private AdView m_adView;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        m_context = this;

//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null)
//            actionBar.setDisplayHomeAsUpEnabled(true);

        TextView discText = findViewById(R.id.disc);
        TextView maxText = findViewById(R.id.max);
        ProgressBar bar = findViewById(R.id.progressbar);
        //bar.setBackgroundColor(Color.parseColor("#00FF00"));
        bar.setMinimumHeight(50);
        bar.setMax(50);
        bar.setProgress(0);
        this.setTitle(getString(R.string.dashboard_title, 1, 50));

        Intent intent = getIntent();
        ArrayList listIdiom = intent.getExtras().getStringArrayList("idiom");
        ArrayList listRead = intent.getExtras().getStringArrayList("read");
        if(listIdiom != null && listRead != null)
        {
            int sizeAnswer = listIdiom.size();
            List<Map<String, String>> listMapAnswer = new ArrayList<>();
            for(int i = 0; i < sizeAnswer; i++)
            {
                Map<String, String> data = new HashMap<>();
                data.put("idiom", listIdiom.get(i).toString());
                data.put("read", listRead.get(i).toString());
                listMapAnswer.add(data);
            }
            for(int i = 0; i < m_listMax.size(); i++)
            {
                int max = m_listMax.get(i);
                if(sizeAnswer <= max)
                {
                    bar.setMax(max);
                    maxText.setText(String.valueOf(max));
                    this.setTitle(getString(R.string.dashboard_title, i+1, max-sizeAnswer));
                    break;
                }
            }
            bar.setProgress(sizeAnswer);

            discText.setText(getString(R.string.desc_result, listMapAnswer.size()));
            Collections.shuffle(listMapAnswer);
            ListView listAnswer = findViewById(R.id.list_answer);
            BaseAdapter adapter = new SimpleAdapter(this,
                    listMapAnswer,
                    android.R.layout.simple_list_item_2,
                    new String[]{"idiom", "read"},
                    new int[]{android.R.id.text1, android.R.id.text2}
            );
            listAnswer.setAdapter(adapter);
            listAnswer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                //@SuppressWarnings("unchecked")
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Map<String, String> conMap = (Map<String, String>)arg0.getItemAtPosition(arg2);
                    String idiom = conMap.get("idiom");
                    Intent intent = new Intent(m_context, WebBrowserActivity.class);
                    intent.putExtra(SearchManager.QUERY, getString(R.string.search_word, idiom));
                    startActivity(intent);
                }
            });
        }
        else
            discText.setText(getString(R.string.desc_no_answer));

        //バナー広告
        m_adView = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        if(!MainActivity.g_isDebug)
            m_adView.loadAd(adRequest);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                // アニメーションの設定
                overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
