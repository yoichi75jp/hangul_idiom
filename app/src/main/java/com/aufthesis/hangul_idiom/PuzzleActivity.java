package com.aufthesis.hangul_idiom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import android.support.v7.app.AppCompatActivity;

// Created by yoichi75jp2 on 2017/03/04.
public class PuzzleActivity extends Activity implements View.OnClickListener {

    private Context m_context = null;

    private Map<Integer, Button> m_mapButton = new ConcurrentHashMap<>();
    private List<Integer> m_listID = new ArrayList<>();
    //private List<Pair<String,String>> m_listIdiom = new ArrayList<>();
    private List<Map<String,String>> m_listQuestion = new ArrayList<>();
    private List<Map<String,String>> m_listPreQuestion = new ArrayList<>();
    private List<Button> m_listClickButton = new ArrayList<>();
    private List<Button> m_listAnswerButton = new ArrayList<>();

    private ArrayList<String> m_answeredList = new ArrayList<>();

    private CheckBox m_checkHint;
    private TextView m_record;

    private TextView m_charAns1;
    private TextView m_charAns2;
    private TextView m_charAns3;
    private TextView m_charAns4;

    final private int m_tolerance = 50;

    private AdView m_adView;
//    private AdView m_adView2;
    private static InterstitialAd m_InterstitialAd;

    private class Idiom
    {
        String m_idiom;
        String m_read;
        int m_level;

        Idiom(String idiom, String read, int level)
        {
            m_idiom = idiom;
            m_read = read;
            m_level = level;
        }
    }
    private List<Idiom> m_listIdiom = new ArrayList<>();
    //private List<Idiom> m_listLowLevelIdiom = new ArrayList<>();
    private int m_sizeLevel1 = 0;
    private int m_sizeLevel2 = 0;

    private String m_mode;

    //private Context m_context;
    //private DBOpenHelper m_DbHelper;
    private SQLiteDatabase m_db;

    private int m_correctCount;
    private int m_preCorrectCount;

    //private final int m_defaultColor = Color.parseColor("#bcffff"); // LightCyan
    private final int m_defaultColor = Color.parseColor("#E0E0E0"); // Gray
    private final int m_onClickColor = Color.parseColor("#FFFFE0"); // LightYellow
    private final int m_hintColor1 = Color.parseColor("#ff4500"); // orange red
    private final int m_hintColor2 = Color.parseColor("#c71585"); // medium violet red
    //private final int m_correctColor = Color.parseColor("#00FF00"); // Lime

    private final List<Integer> m_listCorrectColor = new ArrayList<>(Arrays.asList(
            Color.parseColor("#B0FFB0"),
            Color.parseColor("#42FF42"),
            Color.parseColor("#00E100"),
            Color.parseColor("#00B400")
    ));

    // 効果音用
    final int SOUND_POOL_MAX = 6;
    private SoundPool m_soundPool;
    private int m_correctSound;
    private int m_incorrectSound;
    private int m_clearSoundID;
    private int m_levelUpID;
    private float m_volume;

    private SharedPreferences m_prefs;
    private DateFormat m_format;

    private boolean m_lookedAnswer = false;

    //private FirebaseAnalytics m_FirebaseAnalytics;
    private List<Integer> m_listNum = new ArrayList<>(Arrays.asList(1,2,3));

    private Integer m_twitterDisplayScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        m_context = this;

//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null)
//            actionBar.setDisplayHomeAsUpEnabled(true);

        // Obtain the FirebaseAnalytics instance.
        //m_FirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle fireLogBundle = new Bundle();
        fireLogBundle.putString("TEST", "MyApp MainActivity.onCreate() is called.");
        MyApp.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.APP_OPEN, fireLogBundle);

        m_prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        m_format = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        m_volume = m_prefs.getInt(getString(R.string.seek_volume), 100)/100.f;
        m_mode = m_prefs.getString(getString(R.string.level), getString(R.string.level_normal));

        // スマートフォンの液晶のサイズを取得を開始
        // ウィンドウマネージャのインスタンス取得
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        // スマートフォンの画面のサイズ
        Point point = new Point();
        disp.getSize(point);
        //int swsize = point.x;

//        int textSize1 = 30;
//        int textSize2 = 13;
//        int textSize3 = 45;
        int textSize1 = 29;
        int textSize2 = 12;
        int textSize3 = 41;
        int textSize4 = 15;
        if(this.getResources().getConfiguration().smallestScreenWidthDp >= 600)
        {
            //Tabletの場合
            textSize1 = 63;
            textSize2 = 18;
            textSize3 = 85;
            textSize4 = 20;
        }

        //フォントを変える場合
        //Typeface typefaceOriginal = Typeface.createFromAsset(getAssets(), "fonts/hkgyoprokk.ttf");

        m_listID.clear();
        m_listID.add(R.id.char1);
        m_listID.add(R.id.char2);
        m_listID.add(R.id.char3);
        m_listID.add(R.id.char4);
        m_listID.add(R.id.char5);
        m_listID.add(R.id.char6);
        m_listID.add(R.id.char7);
        m_listID.add(R.id.char8);
        m_listID.add(R.id.char9);
        m_listID.add(R.id.char10);
        m_listID.add(R.id.char11);
        m_listID.add(R.id.char12);
        m_listID.add(R.id.char13);
        m_listID.add(R.id.char14);
        m_listID.add(R.id.char15);
        m_listID.add(R.id.char16);
        m_listID.add(R.id.renew);
        m_listID.add(R.id.put_back);
        m_listID.add(R.id.erase);
        m_listID.add(R.id.answer_btn);
        m_listID.add(R.id.look_answer_btn);
        for(int i = 0; i < m_listID.size(); i++)
        {
            Button button = findViewById(m_listID.get(i));
            button.setOnClickListener(this);
            if(i < 16)
            {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                    button.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//                else
                    button.setTextSize(textSize1);
            }
            else
                button.setTextSize(textSize4);
            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Button charaButton = (Button)v;
                    String kanji = charaButton.getText().toString();
                    ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    // Creates a new text clip to put on the clipboard
                    ClipData clip = ClipData.newPlainText("idiom", kanji);
                    clipboard.setPrimaryClip(clip);

                    Toast toast = Toast.makeText(m_context, getString(R.string.copied, kanji),Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return true;
                }
            });
            m_mapButton.put(m_listID.get(i),button);
        }
        Button putBackButton = m_mapButton.get(R.id.put_back);
        putBackButton.setEnabled(false);

        m_checkHint = findViewById(R.id.check_hint);
        m_checkHint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(m_checkHint.isChecked()) {
                    if(m_listQuestion.size() != 4) return;
                    for(int i = 0; i < 16; i++)
                    {
                        Button button = m_mapButton.get(m_listID.get(i));
                        if(m_listClickButton.indexOf(button) >= 0) continue;
                        if(m_listAnswerButton.indexOf(button) >= 0) continue;
                        if(m_listQuestion.get(0).get("idiom").contains(button.getText().toString()) ||
                                m_listQuestion.get(1).get("idiom").contains(button.getText().toString()))
                            button.setBackgroundColor(m_hintColor1);
                        else
                            button.setBackgroundColor(m_hintColor2);
                        button.setTextColor(Color.YELLOW);
                    }
                }
                else {
                    for(int i = 0; i < 16; i++)
                    {
                        Button button = m_mapButton.get(m_listID.get(i));
                        if(m_listClickButton.indexOf(button) >= 0) continue;
                        if(m_listAnswerButton.indexOf(button) >= 0) continue;
                        button.setBackgroundColor(m_defaultColor);
                        button.setTextColor(Color.BLACK);
                    }
                }
            }
        });

        TextView instruction1 = findViewById(R.id.instruction1);
        m_record = findViewById(R.id.record);

        instruction1.setTextSize(textSize2);
        m_record.setTextSize(textSize2);

        m_charAns1 = findViewById(R.id.ans1);
        m_charAns2 = findViewById(R.id.ans2);
        m_charAns3 = findViewById(R.id.ans3);
        m_charAns4 = findViewById(R.id.ans4);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//        {
//            m_charAns1.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//            m_charAns2.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//            m_charAns3.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//            m_charAns4.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//        }
//        else
        {
            m_charAns1.setTextSize(textSize3);
            m_charAns2.setTextSize(textSize3);
            m_charAns3.setTextSize(textSize3);
            m_charAns4.setTextSize(textSize3);
        }
        //m_charAns1.setTypeface(typefaceOriginal);
        //m_charAns2.setTypeface(typefaceOriginal);
        //m_charAns3.setTypeface(typefaceOriginal);

        DBOpenHelper dbHelper = new DBOpenHelper(this);
        m_db = dbHelper.getDataBase();

        this.setCharacterSet(true);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, "ca-app-pub-1485554329820885~3571541058");

        //バナー広告
        m_adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        if(!MainActivity.g_isDebug)
            m_adView.loadAd(adRequest);
//        m_adView2 = findViewById(R.id.adView4);
//        AdRequest adRequest2 = new AdRequest.Builder().build();
//        if(!MainActivity.g_isDebug)
//            m_adView2.loadAd(adRequest2);

        // AdMobインターステイシャル
        m_InterstitialAd = new InterstitialAd(this);
        m_InterstitialAd.setAdUnitId(getString(R.string.adUnitInterId));
        m_InterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (m_lookedAnswer && m_InterstitialAd.isLoaded()) {
                    m_InterstitialAd.show();
                }
            }
        });
    }
    //Button押下時処理
    public void onClick(View view)
    {
        Intent intent;
        int id = view.getId();
        final Button button = m_mapButton.get(id);
        switch(id)
        {
            case R.id.char1:
            case R.id.char2:
            case R.id.char3:
            case R.id.char4:
            case R.id.char5:
            case R.id.char6:
            case R.id.char7:
            case R.id.char8:
            case R.id.char9:
            case R.id.char10:
            case R.id.char11:
            case R.id.char12:
            case R.id.char13:
            case R.id.char14:
            case R.id.char15:
            case R.id.char16:
                if(m_listClickButton.indexOf(button) >= 0) break;
                if(m_listAnswerButton.indexOf(button) >= 0) break;
                if(m_listClickButton.size() == 4) break;

                String character = button.getText().toString();
                if(m_charAns1.getText().equals(getString(R.string.blank)))
                    m_charAns1.setText(character);
                else if(m_charAns2.getText().equals(getString(R.string.blank)))
                    m_charAns2.setText(character);
                else if(m_charAns3.getText().equals(getString(R.string.blank)))
                    m_charAns3.setText(character);
                else if(m_charAns4.getText().equals(getString(R.string.blank)))
                    m_charAns4.setText(character);

                m_listClickButton.add(button);
                button.setBackgroundColor(m_onClickColor);
                if(m_checkHint.isChecked())
                    button.setTextColor(Color.BLACK);
                break;

            case R.id.renew:
                m_charAns4.setText(getString(R.string.blank));
                m_charAns3.setText(getString(R.string.blank));
                m_charAns2.setText(getString(R.string.blank));
                m_charAns1.setText(getString(R.string.blank));
                intent = new Intent(this, DummyActivity.class);
                startActivityForResult(intent, 1);
                //this.setCharacterSet();
                //add 2018/01/08
                m_preCorrectCount = m_correctCount;
                m_listPreQuestion.clear();
                m_listPreQuestion.addAll(m_listQuestion);
                Button putBackButton = m_mapButton.get(R.id.put_back);
                putBackButton.setEnabled(true);
                break;
            case R.id.put_back:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.back_to_question_title))
                        .setMessage(getString(R.string.back_to_question_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            //m_listPreQuestionを表示
                            //TODO:前の画面で正解していたカウント数だけ戻す必要がある
                            int preCount = 0;
                            if(m_preCorrectCount > 0)
                            {
                                for(int i = 0; i < m_listPreQuestion.size(); i++)
                                {
                                    String idiom = m_listPreQuestion.get(i).get("idiom");
                                    for(int j = m_answeredList.size() - 1; j >= 0; j--)
                                    {
                                        if(m_answeredList.get(j).equals(idiom))
                                        {
                                            m_answeredList.remove(j);
                                            preCount++;
                                            break;
                                        }
                                    }
                                    if(preCount == m_preCorrectCount) break;
                                }
                            }
                            saveList(getString(R.string.answered_list), m_answeredList);
                            m_record.setText(getString(R.string.record, m_answeredList.size()));

                            setCharacterSet(false);
                            button.setEnabled(false);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                break;
            case R.id.erase:
                if(m_charAns4.getText() != getString(R.string.blank))
                    m_charAns4.setText(getString(R.string.blank));
                else if(m_charAns3.getText() != getString(R.string.blank))
                    m_charAns3.setText(getString(R.string.blank));
                else if(m_charAns2.getText() != getString(R.string.blank))
                    m_charAns2.setText(getString(R.string.blank));
                else if(m_charAns1.getText() != getString(R.string.blank))
                    m_charAns1.setText(getString(R.string.blank));

                if(m_listClickButton.size() > 0)
                {
                    Button btnBack = m_listClickButton.get(m_listClickButton.size()-1);

                    if(m_checkHint.isChecked())
                    {
                        if(m_listQuestion.get(0).get("idiom").contains(btnBack.getText().toString()) ||
                                m_listQuestion.get(1).get("idiom").contains(btnBack.getText().toString()))
                            btnBack.setBackgroundColor(m_hintColor1);
                        else
                            btnBack.setBackgroundColor(m_hintColor2);
                        btnBack.setTextColor(Color.YELLOW);
                    }
                    else
                        btnBack.setBackgroundColor(m_defaultColor);
                    m_listClickButton.remove(m_listClickButton.size()-1);
                }
                break;
            case R.id.answer_btn:
                if(m_correctCount == 4)
                    this.showAnswer();
                else {
                    if (!m_charAns1.getText().toString().equals(getString(R.string.blank)) &&
                            !m_charAns2.getText().toString().equals(getString(R.string.blank)) &&
                            !m_charAns3.getText().toString().equals(getString(R.string.blank)) &&
                            !m_charAns4.getText().toString().equals(getString(R.string.blank))) {
                        String idiom = m_charAns1.getText().toString() + m_charAns2.getText().toString() + m_charAns3.getText().toString() + m_charAns4.getText().toString();
                        boolean isCorrect = false;
                        for (int i = 0; i < 4; i++) {
                            if (m_listQuestion.get(i).containsValue(idiom)) isCorrect = true;
                        }
                        if (isCorrect) {
                            for (int i = 0; i < 4; i++) {
                                Button btn = m_listClickButton.get(i);
                                m_listAnswerButton.add(btn);
                                btn.setBackgroundColor(m_listCorrectColor.get(m_correctCount));
                            }
                            if(!m_lookedAnswer) {
                                m_answeredList.add(idiom);
                                saveList(getString(R.string.answered_list), m_answeredList);
                            }
                            m_record.setText(getString(R.string.record, m_answeredList.size()));
                            m_listClickButton.clear();
                            m_correctCount++;
                            if(m_correctCount >= 2)
                                m_checkHint.setEnabled(false);

                            if (DashboardActivity.m_listMax.indexOf(m_answeredList.size()) < 0)
                                m_soundPool.play(m_correctSound, m_volume, m_volume, 0, 0, 1.0F);
                            else {
                                m_soundPool.play(m_levelUpID, m_volume, m_volume, 0, 0, 1.0F);
                                LayoutInflater inflater = getLayoutInflater();
                                view = inflater.inflate(R.layout.toast_layout, null);
                                TextView text = view.findViewById(R.id.toast_text);
                                text.setText(getString(R.string.goal_achievement));
                                text.setGravity(Gravity.CENTER);
                                Toast toast = Toast.makeText(this, getString(R.string.goal_achievement), Toast.LENGTH_LONG);
                                toast.setView(view);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                this.induceReview();
                            }
                        } else {
                            for (int i = 0; i < 4; i++) {
                                Button btn = m_listClickButton.get(i);
                                btn.setBackgroundColor(m_defaultColor);
                            }
                            m_listClickButton.clear();
                            m_soundPool.play(m_incorrectSound, m_volume, m_volume, 0, 0, 1.0F);
                        }
                        m_charAns4.setText(getString(R.string.blank));
                        m_charAns3.setText(getString(R.string.blank));
                        m_charAns2.setText(getString(R.string.blank));
                        m_charAns1.setText(getString(R.string.blank));
                        if (m_correctCount == 4) {
                            m_soundPool.play(m_clearSoundID, m_volume, m_volume, 0, 0, 1.0F);
                            button.setBackgroundResource(R.drawable.circle2);
                            button.setText(getString(R.string.look_answer));
                            Button look_answer_btn = m_mapButton.get(R.id.look_answer_btn);
                            look_answer_btn.setEnabled(false);

                            if(!MainActivity.g_doneReview)
                            {
                                int count = m_prefs.getInt(getString(R.string.count_induce), 0);
                                count++;
                                SharedPreferences.Editor editor = m_prefs.edit();
                                editor.putInt(getString(R.string.count_induce), count);
                                editor.apply();
                            }
                        }
                    }
                }
                break;
            case R.id.look_answer_btn:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_message1)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showAnswer();
                                m_lookedAnswer = true;
                                //Intent intent = new Intent(m_context, DummyActivity.class);
                                //startActivityForResult(intent, 1);
                                int count = m_prefs.getInt(getString(R.string.look_count), 0);
                                count++;
                                if(count >= 4)
                                {
                                    Collections.shuffle(m_listNum);
                                    if(m_listNum.get(0) == 1) {
                                        count = 0;
                                        m_InterstitialAd.loadAd(new AdRequest.Builder().build());
                                    }
                                }
                                SharedPreferences.Editor editor = m_prefs.edit();
                                editor.putInt(getString(R.string.look_count), count);
                                editor.apply();

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                break;
            case R.id.twitter:
                String shareMessage = Uri.encode(getString(R.string.share_message, m_twitterDisplayScore.toString()));
                String url = getString(R.string.twitter_url, shareMessage);
                Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(twitterIntent);
                break;
            default:break;
        }
    }

    // 答えを見せてWeb検索もできる
    @SuppressWarnings("unchecked")
    private void showAnswer()
    {
        BaseAdapter adapter = new SimpleAdapter(this,
                m_listQuestion,
                android.R.layout.simple_list_item_2,
                new String[]{"idiom", "read"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        ListView listView = new ListView(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        listView.setAdapter(adapter);
        AlertDialog.Builder listDlg = new AlertDialog.Builder(this);
        listDlg.setTitle(getString(R.string.search_disc));
        listDlg.setView(listView);
        listDlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        listDlg.setCancelable(false);
        listDlg.show();
    }

    // 漢字をバラして設定する
    private void setCharacterSet(Boolean isNormalQuestion) {
        //既に本日解答した熟語を取得する

        boolean isJP = Locale.getDefault().toString().equals(Locale.JAPAN.toString());
        m_checkHint.setEnabled(true);
        m_checkHint.setChecked(false);

        m_lookedAnswer = false;
        m_answeredList = loadList(getString(R.string.answered_list));
        int lastSize = m_answeredList.size();
        if(lastSize == 0)
            MainActivity.g_isInduceReviewTarget = false;
        String saveDay = m_prefs.getString(getString(R.string.save_day), "");
        //saveDay = "2016/11/06";
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        if(isJP)
            sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);

        Date formatSaveDate = new Date();
        try {
            // 文字列→Date型変換
            if (saveDay.equals("")) saveDay = m_format.format(new Date());
            formatSaveDate = sdf.parse(saveDay);
        } catch (ParseException exp) {
            Toast toast = Toast.makeText(this, exp.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        //int diffDay = differenceDays(new Date(), formatSaveDate);

        Button look_answer_btn = m_mapButton.get(R.id.look_answer_btn);
        look_answer_btn.setEnabled(true);

        Calendar calendar = Calendar.getInstance();
        //int date = calender.get(Calendar.DATE);
        int thisMonth = calendar.get(Calendar.MONTH);
        calendar.setTime(formatSaveDate);
        int saveMonth = calendar.get(Calendar.MONTH);

        if (lastSize > 0 && thisMonth != saveMonth)
        {
            int maxScore = m_prefs.getInt(getString(R.string.max_score), 0);
            showMaxScoreMessage(maxScore, lastSize);
            if(maxScore < lastSize)
            {
                SharedPreferences.Editor editor = m_prefs.edit();
                editor.putInt(getString(R.string.max_score), lastSize);
                editor.apply();
            }
            m_answeredList.clear();
            saveList(getString(R.string.answered_list), m_answeredList);
        }
        m_record.setText(getString(R.string.record, m_answeredList.size()));
        m_charAns4.setText(getString(R.string.blank));
        m_charAns3.setText(getString(R.string.blank));
        m_charAns2.setText(getString(R.string.blank));
        m_charAns1.setText(getString(R.string.blank));

        m_correctCount = 0;
        m_listQuestion.clear();
        if(m_listIdiom.size() <= 0)
        {
            try
            {
                String lang = "\"read-en\"";
                if(isJP) lang = "\"read-ja\"";
                String sql  = "select idiom, "
                        + lang
                        + " , level"
                        + " from hangul_idiom where not "
                        + lang
                        + " is null";
                Cursor cursor = m_db.rawQuery(sql, null);
                cursor.moveToFirst();
                if (cursor.getCount() != 0) {

                    for (int i = 0; i < cursor.getCount(); i++) {
                        String idiom = (cursor.getString(0));
                        String read = (cursor.getString(1));
                        int level = 0;
                        if(!cursor.isNull(2)) {
                            level = (cursor.getInt(2));
                            if(level == 1) m_sizeLevel1++;
                            if(level == 2) m_sizeLevel2++;
                        }
                        //Pair<String,String> keyValue = new Pair<>(idiom, read);
                        //m_listIdiom.add(keyValue);
                        m_listIdiom.add(new Idiom(idiom, read, level));
                        //if(level == 1 || level == 2)
                        //    m_listLowLevelIdiom.add(new Idiom(idiom, read, level));
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }
            catch(Exception exp)
            {
                Toast toast = Toast.makeText(this, String.valueOf(exp.getMessage()),Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
        if(m_listIdiom.size() > 0 && isNormalQuestion)
        {
            Collections.shuffle(m_listIdiom);
            List<String> listCharacter = new ArrayList<>();
            for(int i = 0; i < m_listIdiom.size(); i++)
            {
                boolean isExist = false;
                String idiom = m_listIdiom.get(i).m_idiom;
                String read = m_listIdiom.get(i).m_read;
                int level = m_listIdiom.get(i).m_level;
                //if(read.equals("")) continue;
                if(m_answeredList.indexOf(idiom) >= 0) continue;  // 既に解答した熟語を省く
                if(m_mode.equals(getString(R.string.level_normal)))
                {
                    if(m_sizeLevel1 + m_sizeLevel2 - m_tolerance > m_answeredList.size() && level == 0 || level >= 3) continue;
                    if(m_sizeLevel1 - m_tolerance > m_answeredList.size() && level == 2) continue;
                }
                else if (level == 1 || level == 2) continue;

                for(int j = 0; j < 4; j++)
                {
                    if(listCharacter.indexOf(idiom.substring(j, j+1)) >= 0) isExist = true;
                }
                if(isExist) continue;

                Map<String,String> mapQuestion = new HashMap<>();
                mapQuestion.put("idiom",idiom);
                mapQuestion.put("read",read);
                m_listQuestion.add(mapQuestion);
                for(int j = 0; j < 4; j++)
                    listCharacter.add(idiom.substring(j, j+1));

                if(listCharacter.size() >= 16) break;
            }

            Collections.shuffle(listCharacter);
            for(int i = 0; i < listCharacter.size(); i++)
            {
                Button button = m_mapButton.get(m_listID.get(i));
                button.setText(listCharacter.get(i));
                button.setBackgroundColor(m_defaultColor);
            }
            Button button = m_mapButton.get(R.id.answer_btn);
            button.setText(getString(R.string.to_answer));
            button.setBackgroundResource(R.drawable.circle);

            m_listClickButton.clear();
            m_listAnswerButton.clear();
        }
        //前の画面に戻る 2018/01/30
        else if(m_listPreQuestion.size() > 0 && !isNormalQuestion)
        {
            List<String> listCharacter = new ArrayList<>();
            for(int i = 0; i < m_listPreQuestion.size(); i++)
            {
                String idiom = m_listPreQuestion.get(i).get("idiom");
                String read = m_listPreQuestion.get(i).get("read");

                Map<String,String> mapQuestion = new HashMap<>();
                mapQuestion.put("idiom",idiom);
                mapQuestion.put("read",read);
                m_listQuestion.add(mapQuestion);
                for(int j = 0; j < 4; j++)
                    listCharacter.add(idiom.substring(j, j+1));

                if(listCharacter.size() >= 16) break;
            }
            Collections.shuffle(listCharacter);
            for(int i = 0; i < listCharacter.size(); i++)
            {
                Button button = m_mapButton.get(m_listID.get(i));
                button.setText(listCharacter.get(i));
                button.setBackgroundColor(m_defaultColor);
            }
            Button button = m_mapButton.get(R.id.answer_btn);
            button.setText(getString(R.string.to_answer));
            button.setBackgroundResource(R.drawable.circle);

            m_listClickButton.clear();
            m_listAnswerButton.clear();
        }
    }

/*
    //日付の差（日数）を算出する
    public int differenceDays(Date date1, Date date2) {
        long datetime1 = date1.getTime();
        long datetime2 = date2.getTime();
        long one_date_time = 1000 * 60 * 60 * 24;
        long diffDays = (datetime1 - datetime2) / one_date_time;
        return (int)diffDays;
    }
*/
    //１週間のスコアがこれまで最高スコアであるときに表示する
    public void showMaxScoreMessage(int score1, int score2)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.next_title));

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.max_score_layout, null);
        TextView highScoreText = view.findViewById(R.id.high_score);
        TextView lastWeeksScoreText = view.findViewById(R.id.last_week_score);
        TextView messageText = view.findViewById(R.id.message);

        highScoreText.setText(String.valueOf(score1));
        lastWeeksScoreText.setText(String.valueOf(score2));

        if(score1 > score2)
        {
            highScoreText.setTextSize(30);
            highScoreText.setTextColor(Color.RED);
            messageText.setText(getString(R.string.next_message1));
            messageText.setTextColor(Color.BLUE);
        }
        else if(score1 <= score2)
        {
            lastWeeksScoreText.setTextSize(30);
            lastWeeksScoreText.setTextColor(Color.RED);
            messageText.setText(getString(R.string.next_message2));
            messageText.setTextColor(Color.RED);
        }
        messageText.setGravity(Gravity.CENTER);

        m_twitterDisplayScore = score2;
        ImageButton shareButton = view.findViewById(R.id.twitter);
        shareButton.setOnClickListener(this);

        dialog.setView(view);
        dialog.setPositiveButton(getString(R.string.next_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            /*
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
            */
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 設定値 ArrayList<String> を保存（Context は Activity や Application や Service）
    private void saveList(String key, ArrayList<String> list) {
        JSONArray jsonAry = new JSONArray();
        for(int i = 0; i < list.size(); i++) {
            jsonAry.put(list.get(i));
        }
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putString(key, jsonAry.toString());
        editor.putString(getString(R.string.save_day), m_format.format(new Date()));
        editor.apply();
    }

    // 設定値 ArrayList<String> を取得（Context は Activity や Application や Service）
    private ArrayList<String> loadList(String key) {
        ArrayList<String> list = new ArrayList<>();
        String strJson = m_prefs.getString(key, ""); // 第２引数はkeyが存在しない時に返す初期値
        if(!strJson.equals("")) {
            try {
                JSONArray jsonAry = new JSONArray(strJson);
                for(int i = 0; i < jsonAry.length(); i++) {
                    list.add(jsonAry.getString(i));
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return list;
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
        m_correctSound = m_soundPool.load(getApplicationContext(), R.raw.correct2, 1);
        m_incorrectSound = m_soundPool.load(getApplicationContext(), R.raw.incorrect1, 1);
        m_clearSoundID = m_soundPool.load(getApplicationContext(), R.raw.cheer, 1);
        m_levelUpID = m_soundPool.load(getApplicationContext(), R.raw.ji_023, 1);
        if (m_adView != null) {
            m_adView.resume();
        }
//        if (m_adView2 != null) {
//            m_adView2.resume();
//        }
    }

    @Override
    public void onPause() {
        if (m_adView != null) {
            m_adView.pause();
        }
//        if (m_adView2 != null) {
//            m_adView2.pause();
//        }
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
//        if (m_adView2 != null) {
//            m_adView2.destroy();
//        }
        super.onDestroy();
        setResult(RESULT_OK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        //m_menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == R.id.restore)
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm)
                    .setMessage(getString(R.string.confirm_message2))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int lastSize = m_answeredList.size();
                            if (lastSize > 0)
                            {
                                int maxScore = m_prefs.getInt(getString(R.string.max_score), 0);
                                showMaxScoreMessage(maxScore, lastSize);
                                if(maxScore < lastSize)
                                {
                                    SharedPreferences.Editor editor = m_prefs.edit();
                                    editor.putInt(getString(R.string.max_score), lastSize);
                                    editor.apply();
                                }
                            }
                            m_answeredList.clear();
                            saveList(getString(R.string.answered_list), m_answeredList);
                            m_record.setText(getString(R.string.record, m_answeredList.size()));
                            Intent intent = new Intent(m_context, DummyActivity.class);
                            startActivityForResult(intent, 1);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        if(id == R.id.close)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.final_title));
            dialog.setMessage(getString(R.string.final_message));
            dialog.setPositiveButton(getString(R.string.final_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                    moveTaskToBack(true);
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
        if(id == R.id.dashboard)
        {
            //既に本日解答した熟語を取得する
            ArrayList<String> answerList = loadList(getString(R.string.answered_list));
            ArrayList<String> readList = new ArrayList<>();
            for(int i = 0; i < answerList.size(); i++)
            {
                for(int j = 0; j < m_listIdiom.size(); j++)
                {
                    if(m_listIdiom.get(j).m_idiom.equals(answerList.get(i)))
                    {
                        readList.add(m_listIdiom.get(j).m_read);
                        break;
                    }
                }
            }
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putStringArrayListExtra("idiom", answerList);
            intent.putStringArrayListExtra("read", readList);
            int requestCode = 1;
            startActivityForResult(intent, requestCode);
            //startActivity(intent);
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
            return true;
        }

        if(id == R.id.setting)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            int requestCode = 2;
            startActivityForResult(intent, requestCode);
            //startActivity(intent);
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
            return true;
        }

        if(id == android.R.id.home)
        {
            finish();
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                this.setCharacterSet(true);
                break;
            case 2:
                m_volume = m_prefs.getInt(getString(R.string.seek_volume), 100)/100.f;
                break;
            case 3:
                break;
            case 4:
                break;
            default:break;
        }
    }

    private void induceReview()
    {
        if(MainActivity.g_doneReview) return;
        int count = m_prefs.getInt(getString(R.string.count_induce), 0);
        if(count >= 300)
        {
            try
            {
                Thread.sleep(500);
            }
            catch(Exception e){}
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

}
