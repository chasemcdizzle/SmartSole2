package com.chase.smartsole2;

import android.app.ActivityGroup;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;


public class MainActivity extends ActivityGroup {
    String TAG = MainActivity.class.getSimpleName();
    Bitmap myImage;
    int[] imageColors;
    Canvas tab1Canvas;

    //bluetooth stuff
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    InputStream mmInputStream;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;

    //heatpoint buffer / index
    HeatPoint[] myPoints;
    int pointIndex;

    //save variables
    public static boolean saveData = false;
    public static String saveFileName;
    public static String tempFileName;
    private SecureRandom random = new SecureRandom();

    //playback variable
    public static ArrayList<HeatPoint> pointList;

    //public static MyGLSurfaceView mGLView;
    public static MyGLSurfaceView mGLView;
    //Random myRandom = new Random();

    ImageView navigationImage;
    public static ImageView bluetoothImage;
    public static FrameLayout bluetoothFrame;
    public static Drawable bluetoothIcon;
    public static ToggleButton recordButton;
    FrameLayout backFrame;

    public static boolean seekTouching = false;
    public static SeekBar seekBar;
    public static Drawable pauseIcon;
    public static Drawable playIcon;
    public static boolean paused = false;
    public static boolean playbackMode = false;

    public static boolean bluetoothOn = false;

    public static Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            //int array if session is to be played back
            int[] pointArray = msg.getData().getIntArray("playback");
            //boolean if should save data
            saveData = msg.getData().getBoolean("save");
            //boolean if should change bluetooth icon
            boolean bluetooth = msg.getData().getBoolean("bluetooth");
            //int for setting seekbar "progress"
            int seekProgress = msg.getData().getInt("seekProgress");

            //send save message if the message contains save boolean
            if(saveData == true) {
                Log.d(MainActivity.class.getSimpleName(), "handling save message");
                saveData = msg.getData().getBoolean("save");
                saveFileName = msg.getData().getString("filename");
                mGLView.setSave(saveData, saveFileName);
            }
            else if(bluetooth){
                if(!bluetoothOn) {
                    bluetoothIcon.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
                    bluetoothImage.setImageDrawable(bluetoothIcon);
                    Log.d(MainActivity.class.getSimpleName(), "bluetooth on");
                    bluetoothOn = true;
                }
                else{
                    bluetoothIcon.setColorFilter(null);
                    bluetoothImage.setImageDrawable(bluetoothIcon);
                    Log.d(MainActivity.class.getSimpleName(), "bluetooth off");
                    bluetoothOn = false;
                }
            }
            //we will never set seekProgress to 0, only to 1, so we dont need to have
            //to pass two variables in the message, like boolean seekChanged, int seekProgress
            else if(seekProgress > 0){
                if(seekTouching == false)
                    seekBar.setProgress(seekProgress);
            }
            //otherwise it is a playback message
            if(pointArray != null){
                //set playback mode
                //clear the old bluetooth connecting listener
                bluetoothFrame.setOnClickListener(null);
                //turn off blue filter that was associated with this frame for bt
                if(bluetoothOn){
                    bluetoothIcon.setColorFilter(null);
                    bluetoothImage.setImageDrawable(bluetoothIcon);
                    Log.d(MainActivity.class.getSimpleName(), "bluetooth off");
                    bluetoothOn = false;
                }
                bluetoothFrame.setOnClickListener(playPauseListener);
                bluetoothImage.setImageDrawable(pauseIcon);
                playbackMode = true;
                enableSeek();

                seekBar.setProgress(0);
                pointList = new ArrayList<HeatPoint>();
                for (int i = 0; i < pointArray.length; i++) {
                    pointList.add(new HeatPoint(0, 0, 200, pointArray[i] / 1023.0f));
                }
                seekBar.setMax(pointList.size()/8);
                Log.d(MainActivity.class.getSimpleName(), "handling playback mglviewref: " + mGLView);
                mGLView.startPlaybackThread(pointList);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //check if onCreate is being called several times when returning to activity
        Log.d(TAG, "heat map main activity CREATED");

        //setup tab host
        /*
        TabHost myTabs = (TabHost)findViewById(R.id.tabHost);
        myTabs.setup();
        myTabs.setup(this.getLocalActivityManager());
        */

        //add first tab
        //TabHost.TabSpec tabSpec = myTabs.newTabSpec("heatmap");

        //heatmap
        LinearLayout tab1LinearLayout = (LinearLayout) findViewById(R.id.heatmap_layout);
        mGLView = new MyGLSurfaceView(this);
        tab1LinearLayout.addView(mGLView);

        //icons
        recordButton = (ToggleButton) findViewById(R.id.record_button);
        pauseIcon = getResources().getDrawable(R.drawable.pause);
        playIcon = getResources().getDrawable(R.drawable.play);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setVisibility(LinearLayout.GONE);
        //dont need this anymore
        seekBar.setMax(1024);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                seekTouching = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                seekTouching = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
                if(fromUser)
                    mGLView.setPlaybackIndex(progress);
            }
        });
        navigationImage = (ImageView) findViewById(R.id.navigation_icon);
        bluetoothImage = (ImageView) findViewById(R.id.bluetooth_icon);
        backFrame = (FrameLayout) findViewById(R.id.navigation_icon_frame);
        backFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        bluetoothFrame = (FrameLayout) findViewById(R.id.bluetooth_icon_frame);
        bluetoothFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("onclick bt","clicked bt icon");
                MotionEvent motionEvent = MotionEvent.obtain(
                        0,
                        0,
                        MotionEvent.ACTION_DOWN,
                        0,
                        0,
                        0
                );
                mGLView.onTouchEvent(motionEvent);
            }
        });
        bluetoothIcon = getResources().getDrawable(R.drawable.bluetooth);

        //record button ontouch listener for navigation back home
        //set button click attributes
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((ToggleButton) v).isChecked()){
                    random = new SecureRandom();
                    tempFileName = new BigInteger(130, random).toString(32);
                    mGLView.setSave(true, tempFileName);
                    Log.d("mainactivity", "ischecked, starting save");
                }
                if(!((ToggleButton) v).isChecked()){
                    mGLView.setSave(false, tempFileName);
                    Log.d("mainactivity", "not checked, stopping save");
                }
                //flag is to not destroy itself
            }
        });
        startActivity(new Intent(MainActivity.this, MainMenu.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

    }

    public static void enableSeek(){
        MainActivity.recordButton.setVisibility(LinearLayout.GONE);
        MainActivity.seekBar.setVisibility(LinearLayout.VISIBLE);
    }

    public static void disableSeek(){
        MainActivity.recordButton.setVisibility(LinearLayout.VISIBLE);
        MainActivity.seekBar.setVisibility(LinearLayout.GONE);
    }

    private AnimationSet createFadeAnimation() {
        //animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setStartOffset(500);
        fadeIn.setDuration(1000);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(2000);
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(0);

        return animation;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "main activity STARTED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "main activity RESUMED");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "main activity PAUSED");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "main activity STOPPED");
    }

    @Override
    protected void onDestroy() {
        //I do not want to be destroyed
        super.onDestroy();
        Log.d(TAG, "main activity DESTROYED");
    }
    public static View.OnClickListener playPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //if it is paused set icon to play
            if(!paused){
                bluetoothImage.setImageDrawable(playIcon);
                mGLView.pausePlayback = true;
                paused = true;
                Log.d("playpauselistener", "unpaused");
            }
            //if it is playing set icon to pause
            else{
                bluetoothImage.setImageDrawable(pauseIcon);
                mGLView.pausePlayback = false;
                paused = false;
                Log.d("playpauselistener", "paused");
            }
        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); //do not call constructor! (causes self to be destroyed)
        //moveTaskToBack(true);
        if(mGLView.playbackData)
            mGLView.playbackData = false;
        //turn off the playback boolean &
        if(playbackMode) {
            startActivity(new Intent(MainActivity.this, SessionsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            //turn off pause variables
            paused = false;
            mGLView.playbackData = false;
            mGLView.pausePlayback = false;
            playbackMode = false;
            bluetoothFrame.setOnClickListener(null);
            bluetoothImage.setImageDrawable(bluetoothIcon);
            //if bluetooth is on put back on the blue filter
            if(bluetoothOn) {
                bluetoothIcon.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
                bluetoothImage.setImageDrawable(bluetoothIcon);
                Log.d(MainActivity.class.getSimpleName(), "bluetooth on");
                bluetoothOn = true;
            }
            //clear the pause/play clicklistener from the bluetooth frame
            bluetoothFrame.setOnClickListener(null);
            //restore onclick function for normal bluetooth connecting
            bluetoothFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("onclick bt","clicked bt icon");
                    MotionEvent motionEvent = MotionEvent.obtain(
                            0,
                            0,
                            MotionEvent.ACTION_DOWN,
                            0,
                            0,
                            0
                    );
                    mGLView.onTouchEvent(motionEvent);
                }
            });
        }
        else{
            startActivity(new Intent(MainActivity.this, MainMenu.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        }
        //turn off seekbar
        disableSeek();
        //clear the heatmap
        mGLView.clearScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
