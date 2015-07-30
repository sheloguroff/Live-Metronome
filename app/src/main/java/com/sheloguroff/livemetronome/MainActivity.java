package com.sheloguroff.livemetronome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;


public class MainActivity extends Activity implements GestureDetector.OnGestureListener{

    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_SONG1 = "Song1";
    public static final String APP_PREFERENCES_SONG2 = "Song2";
    public static final String APP_PREFERENCES_SONG3 = "Song3";
    public static final String APP_PREFERENCES_SONG4 = "Song4";
    public static final String APP_PREFERENCES_SONG5 = "Song5";
    public static final String APP_PREFERENCES_BPM1 = "bpm1";
    public static final String APP_PREFERENCES_BPM2 = "bpm2";
    public static final String APP_PREFERENCES_BPM3 = "bpm3";
    public static final String APP_PREFERENCES_BPM4 = "bpm4";
    public static final String APP_PREFERENCES_BPM5 = "bpm5";
    public int bpm = 60;
    int tapCount = 0; // Counter for Tap Tempo
    int tapBpm; // Temp variable for Tap Tempo
    long timeFirstTap; // Time of the first tap
    long timeLastTap; // Time of the last tap
    double exactBpmMs; // Exact Bpm from Tap Tempo in millis
    double exactBpm; // Exact Bpm from Tap Tempo
    TextView bpmTextView; // Current Bpm
    TextView beatTextView; // Current beat
    Metronome metronome;
    ImageView flashSmall; // Flasher image
    Animation flashFadeout; // Flasher animation
    int songNumber = 1; // Current song
    static int bluetoothDelay = 0;
    static boolean stateChanged = false; // True when Bluetooth Headset has connected or disconnected
    int flasherDelay = 60; // Delay to compensate metronome marker offset

    // Settings for the songs
    int bpm1 = 80;
    int bpm2 = 90;
    int bpm3 = 100;
    int bpm4 = 110;
    int bpm5 = 120;
    String songName1 = "Song 1";
    String songName2 = "Song 2";
    String songName3 = "Song 3";
    String songName4 = "Song 4";
    String songName5 = "Song 5";

    GestureDetector detector;
    float bpmf;
    byte flasherState = 1; // turns on the flasher
    int beat = 0;
    int playStatePrevious = 0; // Play state before interacting with the screen
    boolean stop = false;
    private Handler mHandler;
    private SharedPreferences mSettings;


   // this Handler compensates computing delay when starting the metromome
    private Handler startDelayHandler = new Handler();
    private Runnable startDelay = new Runnable() {
        @Override
        public void run() {
            metronome.compensateDelay();
        }
    };

    // Bluetooth Headset has connected or disconnected
    public static void StateChanged (){
        stateChanged = true;
    }

    /** Handles onMarkerReached event. Triggers flasher animation and sets marker again.**/
    private Handler handler() {
        return new Handler() {
            @Override
            public void handleMessage(final Message msg) {

                if (stop == false) {
                    handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flasher();
                        }
                    }, flasherDelay+bluetoothDelay);
                    int bugHP = metronome.headPosition();
                    int bugLP = metronome.getLoopPoints();
                    Log.i("Message", "Flasher triggered, HP = " + bugHP + "LP = " + bugLP);
                    if (stateChanged) {
                        metronome.setTempo(bpm);
                        stateChanged = false;
                        Log.i("Bug", "State Changed");
                    if (bugHP > bugLP)
                        Log.i("Bug", "Head Position Fix");
                        metronome.setTempo(bpm);

                    }

                } else {
                    metronome.stop();
                    stop = false;
                }
                handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        metronome.setMarker();
                    }
                }, 100);
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Flasher should always be visible for a musician
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        detector = new GestureDetector(this, this);

        metronome = new Metronome();
        InputStream in = getResources().openRawResource(R.raw.click_fine);
        mHandler = handler();
        metronome.createPlayer(bpm, in, mHandler); // creating an AudioTrack with the click sound

        bpmTextView = (TextView) findViewById(R.id.textViewBPM);
        beatTextView = (TextView) findViewById(R.id.textViewBeat);
        flashSmall = (ImageView) findViewById(R.id.flashSmall);
        flashFadeout = AnimationUtils.loadAnimation(this, R.anim.flash_fadeout);
        loadSong(songNumber);
        bpmTextView.setText(String.valueOf(bpm));

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Thank you for testing Live Metronome!")
                .setMessage("This is an alpha version, tested only on API10 (Android 2.3.6). " +
                        "It may fail to work on other API.")
                .setCancelable(false)
                .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();

        switch (id) {
            case R.id.action_edit: // editing current song name
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                final EditText edittext= new EditText(this);
                TextView songName = (TextView) findViewById(R.id.textViewSong);
                edittext.setText(songName.getText());
                edittext.setSelectAllOnFocus(true);
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {

                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // Force show soft keyboard when editing song name
                        } else {
                            imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0); // Force hide soft keyboard when finished editing song name
                        }

                    }
                });
                alert.setMessage("Enter the Song name");
                alert.setView(edittext);
                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
                        Editable newSongName = edittext.getText();

                        switch (songNumber) { // saving song name and bpm of current song
                            case 1:
                                songName1 = newSongName.toString();
                                bpm1 = bpm;
                                loadSong(1);
                                return;
                            case 2:
                                songName2 = newSongName.toString();
                                bpm2 = bpm;
                                loadSong(2);
                                return;
                            case 3:
                                songName3 = newSongName.toString();
                                bpm3 = bpm;
                                loadSong(3);
                                return;
                            case 4:
                                songName4 = newSongName.toString();
                                bpm4 = bpm;
                                loadSong(4);
                                return;
                            case 5:
                                songName5 = newSongName.toString();
                                bpm5 = bpm;
                                loadSong(5);
                                return;
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);

                    }
                });
                alert.show();
                return true;

            case R.id.action_save: // saving bpm of current song

                switch (songNumber){
                    case 1:
                        bpm1 = bpm;
                        return true;
                    case 2:
                        bpm2 = bpm;
                        return true;
                    case 3:
                        bpm3 = bpm;
                        return true;
                    case 4:
                        bpm4 = bpm;
                        return true;
                    case 5:
                        bpm5 = bpm;
                        return true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause(){

        super.onPause();

        // Saving song preferences
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_SONG1, songName1);
        editor.putString(APP_PREFERENCES_SONG2, songName2);
        editor.putString(APP_PREFERENCES_SONG3, songName3);
        editor.putString(APP_PREFERENCES_SONG4, songName4);
        editor.putString(APP_PREFERENCES_SONG5, songName5);
        editor.putInt(APP_PREFERENCES_BPM1, bpm1);
        editor.putInt(APP_PREFERENCES_BPM2, bpm2);
        editor.putInt(APP_PREFERENCES_BPM3, bpm3);
        editor.putInt(APP_PREFERENCES_BPM4, bpm4);
        editor.putInt(APP_PREFERENCES_BPM5, bpm5);
        editor.apply();
    }

    @Override
    protected void onResume() {


        super.onResume();

        // Loading song preferences
        if (mSettings.contains(APP_PREFERENCES_SONG1)) {
            songName1 = mSettings.getString(APP_PREFERENCES_SONG1, songName1);
            songName2 = mSettings.getString(APP_PREFERENCES_SONG2, songName2);
            songName3 = mSettings.getString(APP_PREFERENCES_SONG3, songName3);
            songName4 = mSettings.getString(APP_PREFERENCES_SONG4, songName4);
            songName5 = mSettings.getString(APP_PREFERENCES_SONG5, songName5);
            bpm1 = mSettings.getInt(APP_PREFERENCES_BPM1, bpm1);
            bpm2 = mSettings.getInt(APP_PREFERENCES_BPM2, bpm2);
            bpm3 = mSettings.getInt(APP_PREFERENCES_BPM3, bpm3);
            bpm4 = mSettings.getInt(APP_PREFERENCES_BPM4, bpm4);
            bpm5 = mSettings.getInt(APP_PREFERENCES_BPM5, bpm5);
            loadSong(1);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        metronome.stop(); // Prevents creating new AudioTrack over existing one
    }

    public void flasher() {
        flashSmall.startAnimation(flashFadeout);
        if (beat<4)
        beat++;
        else beat = 1;
        beatTextView.setText(String.valueOf(beat));

    }

    public static void setBluetoothDelay (int delay){
        bluetoothDelay = delay;
    }

    public void loadSong(int s){
        TextView songName = (TextView) findViewById(R.id.textViewSong);
        if (s == 1) {
            songName.setText(songName1);
            bpm=bpm1;
            metronome.setTempo(bpm);
        }
        if (s == 2) {
            songName.setText(songName2);
            bpm=bpm2;
            metronome.setTempo(bpm);
        }
        if (s == 3) {
            songName.setText(songName3);
            bpm=bpm3;
            metronome.setTempo(bpm);
        }
        if (s == 4) {
            songName.setText(songName4);
            bpm=bpm4;
            metronome.setTempo(bpm);
        }
        if (s == 5) {
            songName.setText(songName5);
            bpm=bpm5;
            metronome.setTempo(bpm);
        }
        bpmTextView.setText(String.valueOf(bpm));
    }


    @Override
    public boolean onDown(MotionEvent e) {

        tapTempo();
        return true;

    }

    /** Calculates average tempo of touches, sets it and starts metronome.
     * Calculates tempo from 57 to 200 bpm.
     * Re-calculate after 1100 ms pause**/
    private void tapTempo() {
        int hp = metronome.headPosition();
        bpmf = bpm;
        playStatePrevious = metronome.getPlayState();
        // If tapped while playing, just after the previous click, it doesn't click again, just delays next click.
        if (playStatePrevious == 3) {
            if (hp < 8820) {
                metronome.setDelay(hp / 44100 * 1000);
            }
        } else {
            flasher();
            metronome.play();
            startDelayHandler.postDelayed(startDelay, 20000 / bpm); //
        }

        if (tapCount == 0) { // On the first tap it only records time
            timeFirstTap = System.currentTimeMillis();
            timeLastTap = timeFirstTap;
            tapCount++;
        } else if ((System.currentTimeMillis() - timeLastTap) < 1050) { // Maximum time between taps is set to 1050 ms (57 bpm)
            if (hp > 8820) { // If tapped while playing, after less than 8820 samples from the previous click, it doesn't stop, just delays next click.
                metronome.stop();
            }
            timeLastTap = System.currentTimeMillis();
            exactBpmMs = (double) (timeLastTap - timeFirstTap) / tapCount;
            exactBpm = 60000 / exactBpmMs;
            tapBpm = (int) Math.round(exactBpm);
            Log.i("exactBpm", "" + exactBpm);
            Log.i("tapBpm", "" + tapBpm);
            if (tapBpm > 200)
                tapBpm = 200;
            metronome.setTempo(tapBpm);
            bpm = tapBpm;
            bpmTextView.setText(String.valueOf(tapBpm));
            tapCount++;
            if (hp > 7340) {
                metronome.play();
                startDelayHandler.postDelayed(startDelay, (20000 / bpm));
            }
        } else {
            tapCount = 1;
            timeFirstTap = System.currentTimeMillis();
            timeLastTap = timeFirstTap;

        }
    }


    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.i("msg", "onScroll X="+ distanceX + " Y=" + distanceY);
        if (Math.abs(distanceY) > Math.abs(distanceX)*1.5) {
            if (playStatePrevious != 3)  // prevents starting metronome when changing tempo
                stop = true;
            flasherState = 0;
            bpmf = bpmf + distanceY / 10;
            if (bpmf > 49 || bpmf < 201) {
            bpm = ((int) bpmf);
            bpmTextView.setText(String.valueOf(bpm));
            metronome.setTempo(bpm);}


        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        metronome.stop();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i("msg", "onFling velX = " + velocityX + " velY = " + velocityY);

        tapCount = 0;
        if (velocityX > 1000) {  // prevents tapping tempo when switching between songs
            if (songNumber > 1) songNumber--;
            loadSong(songNumber);
            if (playStatePrevious != 3)  // prevents starting metronome when changing songs
                stop = true;
        }
        if (velocityX < -1000) {
            if (songNumber < 5) songNumber++;
            loadSong(songNumber);
            if (playStatePrevious != 3)  // prevents starting metronome when changing songs
                stop = true;

        }
        return true;
    }



}







