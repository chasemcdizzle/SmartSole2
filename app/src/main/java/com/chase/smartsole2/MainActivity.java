package com.chase.smartsole2;

import android.app.ActivityGroup;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


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

    public static boolean saveData = false;
    public static String saveFileName;

    public static MyGLSurfaceView mGLView;
    //Random myRandom = new Random();

    ImageView navigationImage;
    ImageView bluetoothImage;
    ToggleButton recordButton;

    public static Handler mainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            //Log.d(TAG, "received a message");
            //float intensity = msg.getData().getFloat("intensity");
            Log.d(MainActivity.class.getSimpleName(), "handling save message");
            saveData = msg.getData().getBoolean("save");
            saveFileName = msg.getData().getString("filename");
            mGLView.setSave(saveData, saveFileName);
            //mGLView.addPoint(0, 0, 150, intensity);
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
        navigationImage = (ImageView) findViewById(R.id.navigation_icon);
        bluetoothImage = (ImageView) findViewById(R.id.bluetooth_icon);

        //record button ontouch listener for navigation back home


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

        //when this class is created it will automatically open the main menu (puts this is the bg)
        startActivity(new Intent(MainActivity.this, MainMenu.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

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

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); //do not call constructor! (causes self to be destroyed)
        //moveTaskToBack(true);
        startActivity(new Intent(MainActivity.this, MainMenu.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }

    public class MyView extends View {
        Random myRandom;
        public MyView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            tab1Canvas = new Canvas();
            imageColors = new int[200*200];
            myRandom = new Random();
            //imageColors[i] = 715827882;
            //Bitmap image = Bitmap.createBitmap(imageColors, 100, 100, Bitmap.Config.ARGB_8888);
            myImage = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            //Log.d(TAG, Integer.toString(myImage.getHeight()));
            //HeatmapThread heatmapThread = new HeatmapThread();
            //heatmapThread.start();
            //startHeatmapThread();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            int randInt = myRandom.nextInt(16777216);
            for (int i = 0; i < imageColors.length; i++) {
                //imageColors[i] = (0x0F << 24) | (0x21 << 16) | (0x21 << 8) | 0x00;
                imageColors[i] = (0xFF << 24) | randInt;
            }
            myImage.setPixels(imageColors, 0, myImage.getWidth(), 0, 0, myImage.getWidth(), myImage.getHeight());
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(myImage, 0, 0, null);
            Log.d(TAG, "ondraw");
            //this.invalidate();
            /*
            int x = getWidth();
            int y = getHeight();
            int radius;
            radius = 100;
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPaint(paint);
            // Use Color.parseColor to define HTML colors
            paint.setColor(Color.parseColor("#CD5C5C"));
            canvas.drawCircle(x / 2, y / 2, radius, paint);
            */

            //initialize heatmap points
        }

        protected void startHeatmapThread(){
            Thread t = new Thread(new Runnable(){
                public void run() {
                    Random myRandom = new Random();
                    final Handler guiHandler = new Handler();
                    while (true) {
                        int randInt = myRandom.nextInt(16777216);
                        for (int i = 0; i < imageColors.length; i++) {
                            //imageColors[i] = (0x0F << 24) | (0x21 << 16) | (0x21 << 8) | 0x00;
                            imageColors[i] = (0xFF << 24) | randInt;
                        }
                        guiHandler.post(new Runnable() {
                            public void run() {
                                myImage.setPixels(imageColors, 0, myImage.getWidth(), 0, 0, myImage.getWidth(), myImage.getHeight());
                                tab1Canvas.drawBitmap(myImage, 0, 0, null);
                                Log.d(TAG, "threading");
                            }
                        });
                        /*
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        */
                    }
                }
            });
            t.start();
        }

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

    void findBT()
    {
        Log.d(TAG, "starting findBT()");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Log.d(TAG, "No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-06"))
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.d(TAG, "Bluetooth Device Found");
    }

    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmInputStream = mmSocket.getInputStream();

        listenForData();
    }

    public void startRandomHeatmapThread(){
        final Handler handler = new Handler(Looper.getMainLooper());
        final int width = mGLView.getWidth();
        final int height = mGLView.getHeight();
        Thread t = new Thread(new Runnable() {
            Random myRand = new Random();
            public void run() {
                int z = 0;
                while (true) {
                    try {
                        handler.post(new Runnable() {
                            public void run() {
                                try {
                                    HeatPoint points[] = new HeatPoint[8];
                                    for(int i = 0; i < 8; i++){
                                        int x = myRand.nextInt(width);
                                        int y = myRand.nextInt(height);
                                        float intensity = (float)Math.random();
                                        points[i] = new HeatPoint(x, y, 200, intensity);
                                    }
                                    mGLView.addPoints(points);
                                    /*
                                    //old code for adding single point
                                    int x = myRand.nextInt(1000);
                                    int y = myRand.nextInt(1500);
                                    //int intensity = myRand.nextInt(560);
                                    mGLView.addPoint(x, y, 150, (float) Math.random());
                                    */

                                    /*
                                    //old code for message sending for adding points
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("intensity", intensity);
                                    //mainHandler.sendEmptyMessage(0);
                                    Message message = handler.obtainMessage();
                                    message.setData(bundle);
                                    mainHandler.sendMessage(message);
                                    */


                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }


                                } catch (Exception e) {
                                    //nopes
                                }
                            }
                        });
                    }
                    catch(Exception e){

                    }
                }
            }
        });
        t.start();
    }

    //unused
    public void listenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        readBufferPosition = 0;
        readBuffer = new byte[1024];

        Thread t = new Thread(new Runnable() {
            Random myRand = new Random();
            public void run(){

                /*
                try{Thread.sleep(10000);}
                catch (Exception e){

                }
                */

                int z = 0;

                //initialize heatpoint buffer / index
                myPoints = new HeatPoint[9];
                pointIndex = 0;
                Random rand = new Random();
                Log.d(TAG, "pre-random");
                for(int i = 0; i < myPoints.length; i++){
                    //Log.d(TAG, "x:" + x + " and y:" + y);
                    myPoints[i] = new HeatPoint(0, 0, 150, 0);
                }
                //myPoints[4].x = 100;
                //myPoints[5].x = 500;
                Log.d(TAG, "post-random");

                int runNum = 0;
                while(true) {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                //83 == S, 68 == D
                                if(b == delimiter || b == 83 | b == 68)
                                {
                                    //Log.d(TAG, pointIndex +": " + b);
                                    //need to reset index because a full frame has been transmitted
                                    if(b == 83){
                                        pointIndex = 0;
                                        continue;
                                    }
                                    //need to display data b/c a full frame has been transmitted
                                    else if(b == 68){
                                        //Log.d(TAG, "posting");
                                        //mGLView.addPoint(x, y, 50, (float)(intensity/560.0));
                                        //Random myRand = new Random();
                                        //int x = myRand.nextInt(1200);
                                        //int y = myRand.nextInt(1500);
                                        //new post
                                        //handler.post(new HeatmapRunnable(myPoints, runNum++, 0));

                                        handler.post(new Runnable()
                                        {
                                            public void run() {
                                                try {
                                                    HeatPoint points[] = new HeatPoint[8];
                                                    for(int i = 0; i < 8; i++){
                                                        int x = myRand.nextInt(1000);
                                                        int y = myRand.nextInt(1500);
                                                        float intensity = (float)Math.random();
                                                        points[i] = new HeatPoint(x, y, 200, intensity);
                                                    }
                                                    //mGLView.addPoints(points);
                                    /*
                                    //old code for adding single point
                                    int x = myRand.nextInt(1000);
                                    int y = myRand.nextInt(1500);
                                    //int intensity = myRand.nextInt(560);
                                    mGLView.addPoint(x, y, 150, (float) Math.random());
                                    */

                                                    for(int q = 0; q < myPoints.length-1; q++) {
                                                        //old code for message sending for adding points
                                                        Bundle bundle = new Bundle();
                                                        bundle.putFloat("intensity", myPoints[q].intensity);
                                                        //mainHandler.sendEmptyMessage(0);
                                                        Message message = handler.obtainMessage();
                                                        message.setData(bundle);
                                                        mainHandler.sendMessage(message);
                                                    }



                                                    /*
                                                    try {
                                                        Thread.sleep(2000);
                                                    } catch (InterruptedException e) {
                                                        Thread.currentThread().interrupt();
                                                    }
                                                    */


                                                } catch (Exception e) {
                                                    //nopes
                                                }
                                            }
                                        });

                                    }
                                    final byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    /*
                                    //back when we had "chase is the legend 9999"
                                    if(data.startsWith("Chase"))
                                        continue;
                                        */
                                    String myData = data.replaceAll("\\D+","");
                                    if(myData.equals(""))
                                        continue;
                                    //Log.d(TAG, myData);
                                    /*
                                    for(int w = 0; w < myData.length; w++) {
                                        if(myData[w] == 10)
                                            Log.d(TAG, "found newline, below");
                                        Log.d(TAG, "[" + w + "]: " + myData[w]);
                                    }
                                    */
                                    /*
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    */
                                    final int intensity = Integer.parseInt(myData);
                                    //if(pointIndex >= 8)
                                    //pointIndex = 0;
                                    myPoints[pointIndex].intensity = (float)(intensity/561.0);
                                    pointIndex++;
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        Log.d(TAG, "broken thread?");
                    }
                }
            }
        });
        t.start();
    }

    public class HeatmapRunnable implements Runnable {
        private HeatPoint[] myPoints;
        int x;
        int y;
        Random rand;
        public HeatmapRunnable(HeatPoint[] _points, int x, int y) {
            this.myPoints = _points;
            this.x = x;
            this.y = y;
            rand = new Random();
            Log.d(TAG, "Starting runnable " + x);
        }

        public void run() {
            try {
                //int intensity = Integer.parseInt(data);
                //Log.d(TAG, "Intensity: " + String.valueOf(intensity));
                //Log.d(TAG, "adding points");
                //mGLView.addPoints(myPoints);

                //(we have an extra point from echo code so myPoints.length-1 to avoid.
                for(int q = 0; q < myPoints.length-1; q++){
                    //Random rand = new Random();
                    //int x = rand.nextInt(1200);
                    //int y = rand.nextInt(1500);
                    //Log.d(TAG, myPoints[q].x + " " + (int)myPoints[q].y);

                    //single point method
                    mGLView.addPoint(x,y,(int)myPoints[q].size,myPoints[q].intensity);

                    //multippoint method
                    //myPoints[q].x = x;
                    //myPoints[q].y = y;
                    //these points will always be 0
                    //Log.d(TAG, myPoints[q].x + " " + (int)myPoints[q].y);
                }
                Log.d(TAG, "ending runnable " + x);
                //mGLView.addPoints(myPoints);
                //Thread.sleep(2000);

                //mGLView.addPoint(0,0,150,(float)Math.random());
                //mGLView.addPoint(500,500,200,(float)Math.random());
                //mGLView.addPoints(myPoints);

                //mGLView.addPoints(myPoints);
                //message passing way
                                                /*
                                                Bundle bundle = new Bundle();
                                                bundle.putInt("intensity", intensity);
                                                //mainHandler.sendEmptyMessage(0);
                                                Message message = handler.obtainMessage();
                                                message.setData(bundle);
                                                mainHandler.sendMessage(message);
                                                */
                //mGLView.addPoint(x, y, 50, (float)(intensity/561.0));
            }
            catch(Exception e){
                Log.d(TAG, "Couldn't plot");
                Log.d(TAG, e.getMessage());
            }
        }
    }

}
