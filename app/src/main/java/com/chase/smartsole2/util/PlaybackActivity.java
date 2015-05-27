package com.chase.smartsole2.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.chase.smartsole2.HeatPoint;
import com.chase.smartsole2.MainMenu;
import com.chase.smartsole2.MyGLSurfaceView;
import com.chase.smartsole2.R;

import java.io.InputStream;
import java.util.ArrayList;

public class PlaybackActivity extends ActionBarActivity {
    String TAG = PlaybackActivity.class.getSimpleName();
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

    //playback variable
    public static ArrayList<HeatPoint> pointList;

    //public static MyGLSurfaceView mGLView;
    public static MyGLSurfaceView mGLView;
    //Random myRandom = new Random();
    public static Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            int[] pointArray = msg.getData().getIntArray("playback");
            saveData = msg.getData().getBoolean("save");
            Log.d("playback activity", "playback being handled");
            //float intensity = msg.getData().getFloat("intensity");
            //otherwise it is a playback message
            pointList = new ArrayList<HeatPoint>();
            for (int i = 0; i < pointArray.length; i++) {
                pointList.add(new HeatPoint(0, 0, 200, pointArray[i] / 1023.0f));
            }
            //Log.d(MainActivity.class.getSimpleName(), "handling playback mglviewref: " + mGLView);
            mGLView.startPlaybackThread(pointList);

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

        LinearLayout tab1LinearLayout = (LinearLayout) findViewById(R.id.heatmap_layout);
        //mGLView = new MyGLSurfaceView(this);
        //Log.d(Activity);
        mGLView = new MyGLSurfaceView(this);

        //tab1LinearLayout.addView(new MyView(tab1LinearLayout.getContext()));
        tab1LinearLayout.addView(mGLView);

        /*
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Heatmap");
        myTabs.addTab(tabSpec);
        */

        /*
        //add second tab
        tabSpec = myTabs.newTabSpec("profile");
        tabSpec.setContent(new Intent(this, Tab2Activity.class));
        tabSpec.setIndicator("Profile");
        myTabs.addTab(tabSpec);
        */


        /*
        //attempt to add a third tab (success)
        tabSpec = myTabs.newTabSpec("Poot");
        tabSpec.setContent(new Intent(this, MainMenu.class));
        tabSpec.setIndicator("TEMP_BTN");
        myTabs.addTab(tabSpec);
        */

        /*
        //bluetooth stuff
        try
        {
            Log.d(TAG, "try");
            findBT();
            openBT();
        }
        catch (IOException ex) { Log.d(TAG, "couldn't find/open"); }
        */
        //startRandomHeatmapThread();
        //random heatmap points thread
        //startRandomHeatmapThread();
    }
    /*
    public class HeatmapThread extends Thread{
        Random myRandom = new Random();
        public void run() {
            while(true) {
                int randInt = myRandom.nextInt(16777216);
                for (int i = 0; i < imageColors.length; i++) {
                    //imageColors[i] = (0x0F << 24) | (0x21 << 16) | (0x21 << 8) | 0x00;
                    imageColors[i] = (0xFF << 24) | randInt;
                }
                myImage.setPixels(imageColors, 0, myImage.getWidth(), 0, 0, myImage.getWidth(), myImage.getHeight());
                tab1Canvas.drawBitmap(myImage, 0, 0, null);
                Log.d(TAG, "threading");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    */

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

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); //do not call constructor! (causes self to be destroyed)
        //moveTaskToBack(true);
        startActivity(new Intent(PlaybackActivity.this, MainMenu.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
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
