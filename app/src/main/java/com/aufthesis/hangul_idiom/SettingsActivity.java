package com.aufthesis.hangul_idiom;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

//import android.support.v7.app.AppCompatActivity;

/**
// Created by yoichi75jp2 on 2017/06/17.
 */

public class SettingsActivity extends Activity {

    private ImageView m_imageSound;
    private TextView m_text_volume;
    private SharedPreferences m_prefs;
    private AdView m_adView;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null)
//            actionBar.setDisplayHomeAsUpEnabled(true);

        final SeekBar soundSeekBar = findViewById(R.id.sound_seek_bar);
        final Button buttonReturn = findViewById(R.id.button_return);
        m_imageSound = findViewById(R.id.image_sound);
        m_text_volume = findViewById(R.id.text_volume);
        soundSeekBar.setMax(100);
        m_prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        int volume = m_prefs.getInt(getString(R.string.seek_volume), 100);
        soundSeekBar.setProgress(volume);
        this.setImageSound(volume);
        m_text_volume.setText(String.valueOf(volume));

        soundSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        // ツマミをドラッグしたときに呼ばれる
                        int volume = soundSeekBar.getProgress();
                        setImageSound(volume);
                        m_text_volume.setText(String.valueOf(volume));
                        SharedPreferences.Editor editor = m_prefs.edit();
                        editor.putInt(getString(R.string.seek_volume), volume);
                        editor.apply();
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // ツマミに触れたときに呼ばれる
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // ツマミを離したときに呼ばれる
                    }
                }
        );

        buttonReturn.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View view)
            {
                finish();
                // アニメーションの設定
                overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
            }
        });

//        Button linkBtn1 = findViewById(R.id.link_app1);
//        linkBtn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("searchidiom4://main?id=com.aufthesis.searchidiom4");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                try {
//                    startActivity(intent);
//                }
//                catch (ActivityNotFoundException activityNotFound)
//                {
//                    intent.setData(Uri.parse("market://details?id=com.aufthesis.searchidiom4"));
//                    startActivity(intent);
//                }
//            }
//        });
//        Button linkBtn2 = findViewById(R.id.link_app2);
//        linkBtn2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("aufthesis_idiom://main?id=com.aufthesis.idiom");
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                try {
//                    startActivity(intent);
//                }
//                catch (ActivityNotFoundException activityNotFound)
//                {
//                    intent.setData(Uri.parse("market://details?id=com.aufthesis.idiom"));
//                    startActivity(intent);
//                }
//            }
//        });

        //バナー広告
        m_adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        if(!MainActivity.g_isDebug)
            m_adView.loadAd(adRequest);
    }

    private void setImageSound(int volume)
    {
        if(volume == 0)
            m_imageSound.setImageResource(R.drawable.ic_volume_off_black_24dp);
        else if(volume <= 40)
            m_imageSound.setImageResource(R.drawable.ic_volume_mute_black_24dp);
        else if(volume <= 90)
            m_imageSound.setImageResource(R.drawable.ic_volume_down_black_24dp);
        else if(volume == 100)
            m_imageSound.setImageResource(R.drawable.ic_volume_up_black_24dp);
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
