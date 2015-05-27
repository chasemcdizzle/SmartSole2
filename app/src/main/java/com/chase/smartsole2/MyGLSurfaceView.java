package com.chase.smartsole2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */

public class MyGLSurfaceView extends GLSurfaceView {
    String TAG = MainActivity.class.getSimpleName();
	private MyGLRenderer mRenderer;
    //[x*y][intensity]
    private double[] pointAverages;
    private int sampleSize;
    private int numPoints;
    private final int height;
    private final int width;
    private boolean heatmapOn;

    //bluetooth field variables
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    InputStream mmInputStream;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    //heatpoint buffer / index for bluetooth buffer
    HeatPoint[] myPoints;
    int pointIndex;

    Context mainContext;
    final long mBlueToothWatchDogCount = 1000000;
    boolean connectionTimeout = false;

    //saving sensor data field variables
    int[] saveBuffer;
    int saveIndex;
    boolean saveData = false;
    boolean trueSave = false;
    String saveFileName;
    int bufsSaved = 0;

    //playback variable
    ArrayList<HeatPoint> playbackPoints;
    boolean playbackData = false;


	public MyGLSurfaceView(Context context) {
		super(context);
        mainContext = context;
		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
		setRenderer(mRenderer);

		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        height = this.getHeight(); //TODO is the view scaling already done here?
        width = this.getWidth();

        heatmapOn = false;

        pointAverages = new double[8];
        sampleSize = 1;

        //startRandomThread(height, width);

	}

    public void setSave(boolean save, String filename){
        saveData = save;
        saveFileName = filename;
        Log.d(MainActivity.class.getSimpleName(), "saveset() finished, saveset val: " + saveData);
    }


    public void startPlaybackThread(ArrayList<HeatPoint> pts) {
        //turn off bluetooth thread because we are going to playback data now
        //set playback boolean to true so can't start bluetooth until its done
        playbackData = true;
        Log.d("playbackview", "playbackthread started");
        playbackPoints = pts;
        //grab the points array from whatever we get via getextra or whatever
        Thread t = new Thread(new Runnable() {
            public void run() {
                Log.d("playbackview", "run() started. size: " + playbackPoints.size());
                HeatPoint[] myHeatpoints = new HeatPoint[8];
                int x = 0;
                while (x < playbackPoints.size() && playbackData == true) {
                    //Log.d("playbackview", "while() running");
                    int i = 0;
                    for(; i < 8; i++) {
                        try{
                            myHeatpoints[i] = playbackPoints.get(x+i);
                            //Log.d("playbackview", "stored point");
                        }
                        catch(Exception e){

                        }
                    }
                    //if the last frame has less than 8 points, populate the rest of the 8 points with 0 intensity
                    //even if catch happens, for loop will still increment, so i represents the first point
                    //that wasn't plotted
                    /*
                    if(i < x+8){
                        for(int y = x+i; y < x+8; y++){
                            //create a new empty point to set for points that didn't get recorded
                            myHeatpoints[y] = new HeatPoint(0,0,0,0);
                        }
                    }
                    */
                    //Log.d("playbackview", "plotting frame");
                    mRenderer.addPoints(myHeatpoints);
                    requestRender();
                    try{Thread.sleep(50);}
                    catch(Exception e){

                    }

                    x += 8;
                }
                //set playback boolean false
                playbackData = false;
                //Log.d("playbackview", "finished plotting");
            }
        });
        t.start();


    }


    public void startRandomThread(int height, int width) {
        final int viewHeight = height;
        final int viewWidth = width;
        Thread t = new Thread(new Runnable() {
            public void run() {
                HeatPoint[] myHeatpoints = new HeatPoint[8];
                for(int i = 0; i < 8; i++){
                    myHeatpoints[i] = new HeatPoint(0, 0, 100, .75f);
                }
                Random myRandom = new Random();
                while (true) {
                        //Log.d(MainActivity.class.getSimpleName(), "loop begin");
                        //Random myRandom = new Random();
                        //mRenderer.addPoints(myHeatpoints);
                        //requestRender();

                        for(int i = 0; i < 8; i++) {
                            //myHeatpoints[i].intensity = .75f;
                            //int randY = myRandom.nextInt(viewHeight + 1);
                            //int randX = myRandom.nextInt(viewWidth + 1);
                            //random float between .5-1.0
                            float randIntensity = (float) ((myRandom.nextFloat()*.5)+.5);
                            myHeatpoints[i].intensity = randIntensity;
                            //mRenderer.addPoint(randX, randY, 150, .5f);
                            //requestRender();

                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            mRenderer.addPoints(myHeatpoints);
                            requestRender();

                        }

                        //Log.d(MainActivity.class.getSimpleName(), "loop end");
                }
            }
        });
        t.start();
    }

    public synchronized void addPoint(int x, int y, int size, float intensity) {
        mRenderer.addPoint(x, y, size, intensity);
        numPoints++;
        requestRender();
        /*
        if(numPoints == 8) {
            requestRender();
            numPoints = 0;
            Log.d(MainActivity.class.getSimpleName(), "rendering");
        }
        */
    }
    public synchronized void addPoints(HeatPoint points[]){
        mRenderer.addPoints(points);
        requestRender();
    }

    public void clear(){
        mRenderer.clear();
        requestRender();
    }

	@Override
	public boolean onTouchEvent(MotionEvent e) {
        //startRandomThread(height, width);
        //startRandomThread(height, width);

        switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
            if(!heatmapOn) {
                //startRandomThread(height, width);

                try
                {
                    Log.d(TAG, "try");
                    findBT();
                    openBT();

                    //only go true if we successfully find and open bluetooth
                    heatmapOn = true;

                }
                catch (IOException ex) {
                    Log.d(TAG, "couldn't find/open"); //did not find bluetooth

                    //TODO added a temp heat map!
                    Log.d("Tom's String: ", "Generating a temporary heatmap");
                    myPoints = new HeatPoint[8];
                    for(int i = 0; i < myPoints.length; i++) {
                        //Log.d(TAG, "x:" + x + " and y:" + y);
                        myPoints[i] = new HeatPoint(0, 0, 300, 0.5f);
                    }
                    mRenderer.addPoints(myPoints);
                    requestRender();


                }


            }
			/*
            mRenderer.onTouchEvent(e.getX(), e.getY());
			requestRender();
            numPoints++;
            if(numPoints == 8){
                numPoints = 0;
                mRenderer.clear();
                requestRender();
            }
            */
		}
		return true;
	}


    void findBT() {
        Log.d(TAG, "starting findBT()");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Log.d(TAG, "No bluetooth adapter available");
        }

        /*
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        */

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

        //throw error to open the smartphone bluetooth settings

        //call openBT if device != null


        Log.d(TAG, "Bluetooth Device Found");
    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();

        mmInputStream = mmSocket.getInputStream();

        listenForData();
    }


    public void listenForData() {

        final byte delimiter = 10; //This is the ASCII code for a newline character
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        saveBuffer = new int[1024];
        saveIndex = 0;

        Thread t = new Thread(new Runnable() {
            public void run(){
                //initialize heatpoint buffer / index
                myPoints = new HeatPoint[8];
                pointIndex = 0;
                for(int i = 0; i < myPoints.length; i++){
                    //Log.d(TAG, "x:" + x + " and y:" + y);
                    myPoints[i] = new HeatPoint(0, 0, 200, 0);
                }

                //watchdog timer/counter
                long mBlueToothWatchDogCounter = 0;

                //generate heatmap!
                while( !connectionTimeout ) {
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
                                        //Log.d(MainActivity.class.getSimpleName(), "found S");
                                        pointIndex = 0;
                                        //we want to start saving data, so we wait for the beginning of a frame
                                        //then begin recording
                                        if(saveData)
                                            trueSave = true;
                                        continue;
                                    }
                                    //need to display data b/c a full frame has been transmitted
                                    else if(b == 68){
                                        //Log.d(MainActivity.class.getSimpleName(), "found D");
                                        try {
                                            mRenderer.addPoints(myPoints);
                                            requestRender();
                                            if(sampleSize < 4) sampleSize++; //filter transient

                                        } catch (Exception e) {
                                            Log.d(MainActivity.class.getSimpleName(), "couldn't plot");
                                        }

                                    }
                                    final byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    String myData = data.replaceAll("\\D+","");
                                    if(myData.equals(""))
                                        continue;
                                    //Log.d(TAG, myData);
                                    //scaling such that no pressure = green dot
                                    //int intensity = 1500-Integer.parseInt(myData);

                                    //low pass filter
                                    int preintensity = Integer.parseInt(myData);
                                    //Log.d(MainActivity.class.getSimpleName(), "savedata val: " + saveData);
                                    if(preintensity > 1000)
                                        preintensity = 1500;
                                    int intensity = 1500 - preintensity;
                                    pointAverages[pointIndex] = pointAverages[pointIndex] - (pointAverages[pointIndex]/sampleSize) + (intensity/(double)sampleSize);
                                    //Log.d(TAG, "PointIndex " + pointIndex + ": " + pointAverages[pointIndex]);
                                    myPoints[pointIndex].intensity = (float)(pointAverages[pointIndex]/1023.0);
                                    if(saveData){
                                        if(saveIndex == 1024) {
                                            saveIndex = 0;
                                            Intent msgIntent = new Intent(mainContext, SaveService.class);
                                            msgIntent.putExtra(SaveService.EXTRA_PARAM1, saveBuffer);
                                            msgIntent.putExtra(SaveService.EXTRA_PARAM2, saveFileName);
                                            //probably dont need to make a new array for next buf b/c putExtra already copies the array
                                            //so we don't have to worry about it being modified before we're done saving it
                                            //saveBuffer = new int[1024];
                                            msgIntent.setAction("com.db.chase.dbtest.action.save");
                                            mainContext.startService(msgIntent);
                                            Log.d(MainActivity.class.getSimpleName(), "saveservice started");
                                            bufsSaved++;
                                            if(bufsSaved == 2){
                                                saveData = false;
                                                trueSave = false;
                                                Log.d(MainActivity.class.getSimpleName(), "finished saving");
                                            }
                                        }
                                        saveBuffer[saveIndex] = (int)pointAverages[pointIndex];
                                        saveIndex++;
                                    }
                                    //myPoints[pointIndex].intensity = (float)(intensity/1023.0);
                                    //Log.d(TAG, String.valueOf(myPoints[pointIndex].intensity));
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
                        //break to disconnect bluetooth
                        break;
                    }
                }//end while loop

                //disconnect from bluetooth
                Log.d(MainActivity.class.getSimpleName(), "Bluetooth Disconnected");
                heatmapOn = false;

            }
        });
        t.start(); //start running the heatmap!


    }//end function

}
