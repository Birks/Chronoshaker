package rs.ac.su.vts.vp.stopper;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;

import java.lang.reflect.Field;


//TODO megcsinálni h ha lezarom a telefont akk ne menyjen tovabb --kijavitva
//TODO megoldani h hosszu ido utan lekicsinyedjenek a betuk -- kijavitva
//TODO megcsinalni a kepernyoaranyokat -- kijavitva
//TODO helpet csinalni
//TODO nyelveket beletenni
//TODO csinalni kiraly resetelest -- kijavitva
//TODO tenni ikonokat a menübe
//TODO valamiert ha landscapeban nincs split -- kijavitva


public class MainActivity extends ActionBarActivity {

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private Chronometer mChronometer;
    private ImageButton startButton;
    private ImageButton resetButton;
    private long timeAtPause; //pilanatnyi ido pausekor
    private ProgressBar spinner;
    static final String activityResetTime = "playerScore";
    static final String savedStartState = "playerLevel";
    static final String pauseSavedTime = "playerTime";
    public enum Status {STARTED, PAUSED, STOPPED}
    private View mainLayout;
    private boolean isLongClick;
    private int orientation;
    private Status startState; // ez alapjan donti el hogy mikor kell start/pause
    private boolean resetState;
    private TextView splitText;
    private long tempSplit;
    private TextView tapText;
    private ImageButton dialogButton;
    private String[] splitDataArrayRight = new String[255];
    int rightCounter;
    private String[] splitDataArrayLeft = new String[255];
    SelectorDialog sd;
    private int timemode = 0;
    private long ts = 0;
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlResponder;
    BroadcastReceiver mReceiver=null;
    VolumeChangeObserver mVolumeChangeObserver;
    MediaPlayer mediaPlayer;
    Handler h;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("stopper_verbose", "onCreate()");


        //kezdeti inicializalasok
        startState = Status.STOPPED;
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        startButton = (ImageButton) findViewById(R.id.startPause);
        resetButton = (ImageButton) findViewById(R.id.resetSplit);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        mainLayout = findViewById(R.id.layout);
        splitText = (TextView) findViewById(R.id.splitText);
        dialogButton = (ImageButton) findViewById(R.id.dialogButton);
        dialogButton.setVisibility(View.INVISIBLE);
        resetState = true;
        Typeface robotoCondensedItalic = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Italic.ttf");
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        mChronometer.setTypeface(robotoLight);
        tapText = (TextView) findViewById(R.id.tapText);
        tapText.setTypeface(robotoCondensedItalic);
        mainLayoutListerner();
        resetChrono(); // kezdéskor resetelve legyen
        getOverflowMenu(); // ez kell h ne legyen a rotate a menuben benne + menu gomb API<=15
        rightCounter = 1;
        Log.v("stopper_verbose", String.valueOf(getResources().getDimension(R.dimen.abc_action_bar_default_height_material)));

        mVolumeChangeObserver = new VolumeChangeObserver(this, new Handler(),this);
        getSupportActionBar().setElevation(0);







        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mVolumeChangeObserver);
        mediaPlayer = MediaPlayer.create(this, R.raw.silent);



        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer chronometer) {
                CharSequence text = chronometer.getText();
                if (text.length() < 8) {
                    //mChronometer.setTextSize(86);
                    //chronometer.setText("0:"+text);
                } else if (text.length() >= 8) {
                    Resources res = getResources();
                    mChronometer.setTextSize(res.getInteger(R.integer.numberWHourSize));
                }



            }
        });


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                handleShakeEvent(count);
            }
        });


        // ha a részidőre kattintunk akkor elindulhjon a dialogactivity
        splitText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startDialog(v);

            }
        });
    }

    private void handleShakeEvent(int count) {
        if (isLongClick) {
            vibrate();
            if (startState == Status.STARTED)
                pauseChrono();
            else if (startState == Status.PAUSED || startState == Status.STOPPED)
                startChrono();
        }
    }

    public void mainLayoutListerner() {

        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tapText.setText(getResources().getString(R.string.shakeNow));
                    isLongClick = true;
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    tapText.setText(getResources().getString(R.string.tapText));
                    isLongClick = false;
                    return false;
                }
                return false;
            }
        });

    }


    // ezek mint kellenek a progressbar forgasahoz
    private class ProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            spinner.setVisibility(View.GONE);
        }
    }

    // overloadolás van
    public void onStartClick(View v) {
        vibrate();
        switch (startState) {
            //megnézi előzőleg milyen állapot volt kattintáskor
            case STOPPED: // amikor eloszor indul a program es startoljuk
                startChrono();
                break;

            case STARTED: // elso inditas utan amikor fut es lepauzaljuk
                pauseChrono();
                break;

            case PAUSED: // amikor eoszor lelett allitva es utana elinditjuk megint
                startChrono();
                break;

        }
    }


    public void onStartClick() {
        vibrate();
        switch (startState) {
            //megnézi előzőleg milyen állapot volt kattintáskor
            case STOPPED: // amikor eloszor indul a program es startoljuk
                startChrono();
                break;

            case STARTED: // elso inditas utan amikor fut es lepauzaljuk
                pauseChrono();
                break;

            case PAUSED: // amikor eoszor lelett allitva es utana elinditjuk megint
                startChrono();
                break;

        }
    }

    public void onResetClick(View v) {

        vibrate();
        if (resetState)
            resetClick();
        else
            splitClick();
    }


    public void onResetClick() {
        vibrate();
        if (resetState)
            resetClick();
        else
            splitClick();
    }

    public void startChrono() {
        if (startState == Status.STOPPED)
            mChronometer.setBase(SystemClock.elapsedRealtime()); //nullarol unduljon
            //mChronometer.setBase(SystemClock.elapsedRealtime()-3569000); //59-tol induljon
        else if (startState == Status.PAUSED)
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeAtPause);
        startState = Status.STARTED;
        mChronometer.start();
        startButton.setImageResource(R.drawable.ic_action_pause);
        checkRotationForSpinner(); // a spinnernek kell h landscapeben eltunjon
        resetState = false;
        resetButton.setImageResource(R.drawable.ic_action_data_usage);
        checkTimemode();
    }

    public void pauseChrono() {

        mChronometer.stop();
        startState = Status.PAUSED;
        timeAtPause = (mChronometer.getBase() - SystemClock.elapsedRealtime()); // elkapja az aktualis idot akkor amikor lepauselva van
        startButton.setImageResource(R.drawable.ic_action_play);
        spinner.setVisibility(View.INVISIBLE);
        resetState = true;
        resetButton.setImageResource(R.drawable.ic_action_replay);
        checkRotationForSpinner(); // a spinnernek kell h landscapeben eltunjon
    }

    public void resetChrono() {
        startState = Status.STOPPED;
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        //mChronometer.setBase(5000);
        startButton.setImageResource(R.drawable.ic_action_play);
        mChronometer.setText("00:00:0");
        spinner.setVisibility(View.INVISIBLE);
        splitText.setText("");
        tapText.setText(getResources().getString(R.string.tapText));
        dialogButton.setVisibility(View.INVISIBLE);
        rightCounter = 1;
        Resources res = getResources();
        mChronometer.setTextSize(res.getInteger(R.integer.numberSize));
        tempSplit = 0;

    }

    public void resetClick() {
        MyCustomView mcv = (MyCustomView) findViewById(R.id.myscustomview);
        mcv.onLayoutClose();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                resetChrono();
            }
        }, 650);


    }

    public void splitClick() {
        if (timemode == 0) { // ha split time
            String tempString;
            tapText.setText(getResources().getString(R.string.tapSplit));
            dialogButton.setVisibility(View.VISIBLE); //TODO kitorolni h megjelenjen
            Log.i("STOPPER_tempsplit_else", String.valueOf(tempSplit));
            Log.i("STOPPER_tempsplit_getTimeelapsed", String.valueOf(mChronometer.getTimeElapsed()) + "\n");
            tempString = mChronometer.getTextSplitTime(mChronometer.getTimeElapsed() - tempSplit);
            tempSplit = mChronometer.getTimeElapsed() + tempSplit;
            splitDataArrayLeft[rightCounter] = mChronometer.getTextSplitTime((mChronometer.getBase() - SystemClock.elapsedRealtime()) * (-1)); //ezzel teszuk be a 1:25:0-s szamokat
            splitDataArrayRight[rightCounter] = tempString; //ezzel teszuk be a +0stbt a tombbe amit majd atkuldunk
            splitDataArrayRight[0] = String.valueOf(rightCounter);
            splitText.setText(getResources().getString(R.string.split)+":  +" + tempString);
            rightCounter++;
        } else if (timemode == 1) { // ha lap time
            String tempString;
            String tempSplitString = "";
            tapText.setText(getResources().getString(R.string.tapLap));
            dialogButton.setVisibility(View.VISIBLE); //TODO kitorolni h megjelenjen
            Log.i("STOPPER_tempslap_else", String.valueOf(tempSplit));
            Log.i("STOPPER_tempsplap_getTimeelapsed", String.valueOf(mChronometer.getTimeElapsed()) + "\n");
            tempSplit = ((mChronometer.getBase() - SystemClock.elapsedRealtime()) * (-1)) - ts;
            ts = (mChronometer.getBase() - SystemClock.elapsedRealtime()) * (-1);
            tempString = mChronometer.getTextSplitTime(ts);
            if (tempSplit < 0) {
                tempSplitString += " -";
                tempSplit *= (-1);
            } else
                tempSplitString += "+";
            tempSplitString += mChronometer.getTextSplitTime(tempSplit);
            Log.d("stopper_verbose", "tempstring " + tempString);
            Log.d("stopper_verbose", "tempsplit " + tempSplitString);
            splitText.setText(getResources().getString(R.string.lap)+":  " + tempString);
            splitDataArrayLeft[rightCounter] = tempString;
            splitDataArrayRight[rightCounter] = tempSplitString;
            splitDataArrayRight[0] = String.valueOf(rightCounter);
            rightCounter++;
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.setText("00:00:0");
            mChronometer.start();
        }
    }

    public void rotateScreen() {
        orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            tapText.setVisibility(View.VISIBLE);
            if (startState == Status.STARTED)
                spinner.setVisibility(View.VISIBLE);

        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            tapText.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
        }

    }

    // ezaz amitol lesz rotate gomb
    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDialog(View v) {
        /*
        Intent intent = new Intent(this, DialogActivity.class);
        intent.put("splitDataArrayRight",splitDataArrayRight);
        startActivity(intent);

        */
        checkRotationForSpinner();
        Bundle b = new Bundle();
        b.putStringArray("key", splitDataArrayRight);
        b.putStringArray("key2", splitDataArrayLeft);
        checkTimemode();
        b.putInt("tm", timemode);
        Intent i = new Intent(this, DialogActivity.class);
        i.putExtras(b);
        for (int j = 0; j < rightCounter; j++) {
            Log.v("stopper_verbose", "mainben:  " + splitDataArrayLeft[j] + "   " + splitDataArrayRight[j]);
        }
        startActivity(i);
    }

    public void aboutClick() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.aboutTitle));
        alertDialog.setMessage(getResources().getString(R.string.aboutText));

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // here you can add functions
            }
        });
        alertDialog.setIcon(R.drawable.ic_launcher_inv);
        alertDialog.show();
    }

    public void rateClick() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }
    }

    public void checkRotationForSpinner() {
        if (orientation != Configuration.ORIENTATION_PORTRAIT) { // WTF!!!!
            if (startState == Status.STARTED)
                spinner.setVisibility(View.VISIBLE);
            else
                spinner.setVisibility(View.INVISIBLE);
        } else
            spinner.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i("stopper_verbose", "onSaveInstanceState()");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i("stopper_verbose", "onRestoreInstanceState()");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("stopper_verbose", "onResume()");
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        /*

        h = new Handler();

        h.postDelayed(new Runnable() {
            public void run() {
                int m = mVolumeChangeObserver.getKeyPressed();
                //Log.d("stopper_verbose", String.valueOf(m));
                shitcheck(m);
                //Log.i("stopper_verbose", "handler");
                mVolumeChangeObserver.setKeyPressed();
                h.postDelayed(this, 500);
            }
        }, 500);

         */


        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

        mSensorManager.unregisterListener(mShakeDetector);
        Log.i("stopper_verbose", "onPause()");

        if (startState==Status.STOPPED) {
            android.os.Process.killProcess(android.os.Process.myPid());
            getApplicationContext().getContentResolver().unregisterContentObserver(mVolumeChangeObserver);
            mediaPlayer.stop();
            mediaPlayer.release();
        }


    }


    public void shitcheck(int m) {
        switch (m) {
            case 1:
                //Log.d("stopper_verbose", "mainben up");
                onStartClick();
                break;
            case 2:
                //Log.d("stopper_verbose", "mainben down");
                onResetClick();
                break;
            default:
                //Log.d("stopper_verbose", "default");
                break;
        }
    }

    public void changeColor(){

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().getContentResolver().unregisterContentObserver(mVolumeChangeObserver);
        mediaPlayer.stop();
        mediaPlayer.release();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.i("stopper_verbose", "KeyEvent.ACTION_UP");
                    onStartClick();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.i("stopper_verbose", "KeyEvent.ACTION_DOWN");
                    onResetClick();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }



    private void selectTimemode() {
        sd = new SelectorDialog();
        FragmentManager fm = getSupportFragmentManager();
        sd.show(fm, "fragment_edit_name");
        pauseChrono();
        resetChrono();

    }

    private void checkTimemode() {
        try {
            timemode = sd.getWhich();
            Log.d("stopper_verbose", "mainben timemode " + String.valueOf(timemode));
        } catch (NullPointerException n) {
        }
        ;
    }

    public  void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(20);
    }

    public void mediaPlayerHandler() {
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });


    }

    public static void statikTest() {
        Log.d("stopper_verbose", "STATIKTESST!!!!!!!!!!");
        //onStartClick();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        */
        switch (item.getItemId()) {
            case R.id.action_rotate:
                rotateScreen();
                return true;
            case R.id.action_exit:
                MainActivity.this.finish();
                return true;
            case R.id.action_about:
                aboutClick();
                return true;
            case R.id.action_rate:
                rateClick();
                return true;
            case R.id.action_timemode:
                selectTimemode();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


}
