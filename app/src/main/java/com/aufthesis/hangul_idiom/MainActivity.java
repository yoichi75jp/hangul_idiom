package com.aufthesis.hangul_idiom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

//import android.support.v7.app.AppCompatActivity;

// Created by yoichi75jp2 on 2017/03/04.
public class MainActivity extends Activity implements View.OnClickListener {

    //debug時はこの値をtrueに設定
    static public boolean g_isDebug = false;

    static public boolean g_doneReview = false;
    static public boolean g_isInduceReviewTarget = false;

    private TextView m_txtMode;
    private String m_mode;
    private AdView m_adView;

    // 効果音用
    final int SOUND_POOL_MAX = 6;
    private SoundPool m_soundPool;
    private int m_clickSound;
    private float m_volume;

    private SharedPreferences m_prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the FirebaseAnalytics instance.
        //m_FirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle fireLogBundle = new Bundle();
        fireLogBundle.putString("TEST", "MyApp MainActivity.onCreate() is called.");
        MyApp.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.APP_OPEN, fireLogBundle);

        m_prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        m_volume = m_prefs.getInt(getString(R.string.seek_volume), 100)/100.f;
        m_mode = m_prefs.getString(getString(R.string.level), getString(R.string.level_normal));
        g_doneReview = m_prefs.getBoolean(getString(R.string.review_done), false);

        int textSize1 = 30;
        int textSize2 = 13;
        if(this.getResources().getConfiguration().smallestScreenWidthDp >= 600)
        {
            //Tabletの場合
            textSize1 = 80;
            textSize2 = 20;
        }

        m_txtMode = findViewById(R.id.txt_level);
        Switch switchMode = findViewById(R.id.switch_level);
        switchMode.setChecked(!m_mode.equals(getString(R.string.level_normal)));
        switchMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_mode = isChecked ? getString(R.string.level_hard) : getString(R.string.level_normal);
                m_txtMode.setText(m_mode);
                SharedPreferences.Editor editor = m_prefs.edit();
                editor.putString(getString(R.string.level), m_mode);
                editor.apply();
            }
        });
        m_txtMode.setText(m_mode);

        switchMode.setTextSize(textSize2);
        m_txtMode.setTextSize(textSize2);

        TextView txtModeTitle = findViewById(R.id.txt_mode);
        txtModeTitle.setTextSize(textSize2);

        Button button = findViewById(R.id.puzzle);
        button.setOnClickListener(this);
        button.setTextSize(textSize1);
//        button = findViewById(R.id.read);
//        button.setOnClickListener(this);
//        button.setTextSize(textSize1);
//        button = findViewById(R.id.performance);
//        button.setOnClickListener(this);
//        button.setTextSize(textSize2);

        ImageView imageLogo = findViewById(R.id.logo_image);
        imageLogo.setOnClickListener(this);

        TextView version = findViewById(R.id.version);
        try
        {
            String sPackageName = getPackageName();
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(sPackageName, 0);
            String sVersionName = packageInfo.versionName;

            version.setText(getString(R.string.version, sVersionName));
            g_isInduceReviewTarget = m_prefs.getBoolean(getString(R.string.version, sVersionName), true);
            if(g_isInduceReviewTarget)
            {
                SharedPreferences.Editor editor = m_prefs.edit();
                editor.putBoolean(getString(R.string.version, sVersionName), false);
                editor.apply();
            }
        }
        catch(Exception e)
        {
            version.setText("");
        }

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, "ca-app-pub-1485554329820885~3571541058");

        //バナー広告
        m_adView = findViewById(R.id.adView0);
        AdRequest adRequest = new AdRequest.Builder().build();
        if(!g_isDebug)
            m_adView.loadAd(adRequest);
    }

    //Button押下時処理
    public void onClick(View view)
    {
        Intent intent;
        int requestCode;
        int id = view.getId();
        switch(id) {
//            case R.id.performance:
//                intent = new Intent(this, AchievementActivity.class);
//                requestCode = 0;
//                startActivityForResult(intent, requestCode);
//                break;

            case R.id.puzzle:
                intent = new Intent(this, PuzzleActivity.class);
                requestCode = 1;
                startActivityForResult(intent, requestCode);
                break;

//            case R.id.read:
//                intent = new Intent(this, HowToReadActivity.class);
//                requestCode = 2;
//                startActivityForResult(intent, requestCode);
//                break;

            case R.id.logo_image:
                //TODO:音鳴らし
                break;
        }
        // アニメーションの設定
        overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
        m_soundPool.play(m_clickSound, m_volume, m_volume, 0, 0, 1.0F);
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.final_title));
            dialog.setMessage(getString(R.string.final_message));
            dialog.setPositiveButton(getString(R.string.final_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    //moveTaskToBack(true);
                }
            });
            dialog.setNegativeButton(getString(R.string.final_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // 予め音声データを読み込む
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            m_soundPool = new SoundPool(SOUND_POOL_MAX, AudioManager.STREAM_MUSIC, 0);
        }
        else
        {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            m_soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(SOUND_POOL_MAX)
                    .build();
        }
        m_clickSound = m_soundPool.load(getApplicationContext(), R.raw.tiny_button_push, 1);
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
        m_soundPool.release();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //m_menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == R.id.close)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.final_title));
            dialog.setMessage(getString(R.string.final_message));
            dialog.setPositiveButton(getString(R.string.final_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    //moveTaskToBack(true);
                }
            });
            dialog.setNegativeButton(getString(R.string.final_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
        if(id == R.id.setting)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            int requestCode = 4;
            startActivityForResult(intent, requestCode);
            //startActivity(intent);
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
            case 1:
            case 2:
                if(g_isInduceReviewTarget)
                    induceReview();
                break;
            case 3:
                break;
            case 4:
                m_volume = m_prefs.getInt(getString(R.string.seek_volume), 100)/100.f;
                break;
            default:break;
        }
    }
    private void induceReview()
    {
        if(g_doneReview) return;
        try
        {
            Thread.sleep(500);
        }
        catch(Exception e){}

        g_isInduceReviewTarget = false;
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.induce_title));
        dialog.setMessage(getString(R.string.induce_message));
        dialog.setPositiveButton(getString(R.string.induce_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = m_prefs.edit();
                editor.putInt(getString(R.string.count_induce), 0);
                editor.putBoolean(getString(R.string.review_done), true);
                editor.apply();
                Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
                googlePlayIntent.setData(Uri.parse("market://details?id=com.aufthesis.hangul_idiom"));
                startActivity(googlePlayIntent);
            }
        });
        dialog.setNegativeButton(getString(R.string.induce_cancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}
