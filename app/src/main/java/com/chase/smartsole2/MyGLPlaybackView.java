package com.chase.smartsole2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class MyGLPlaybackView extends GLSurfaceView{

    /**
    * A view container where OpenGL ES graphics can be drawn on screen.
    * This view can also be used to capture touch events, such as a user
    * interacting with drawn objects.
    */
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

    //saving sensor data field variables
    int[] saveBuffer;
    int saveIndex;
    boolean saveData = false;
    String saveFileName;
    int bufsSaved = 0;
    ArrayList<HeatPoint> playbackPoints;

    public MyGLPlaybackView(Context context) {
        super(context);
        mainContext = context;
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        height = this.getHeight();
        width = this.getWidth();

        heatmapOn = false;

        pointAverages = new double[8];
        sampleSize = 1;

        //startRandomThread(height, width);

    }

    public void setSave(boolean save, String filename) {
        saveData = save;
        saveFileName = filename;
        Log.d(MainActivity.class.getSimpleName(), "saveset() finished, saveset val: " + saveData);
    }

    public void startRandomThread(int height, int width) {
        final int viewHeight = height;
        final int viewWidth = width;
        Thread t = new Thread(new Runnable() {
            public void run() {
                HeatPoint[] myHeatpoints = new HeatPoint[8];
                for (int i = 0; i < 8; i++) {
                    myHeatpoints[i] = new HeatPoint(0, 0, 100, .75f);
                }
                Random myRandom = new Random();
                while (true) {
                    //Log.d(MainActivity.class.getSimpleName(), "loop begin");
                    //Random myRandom = new Random();
                    //mRenderer.addPoints(myHeatpoints);
                    //requestRender();

                    for (int i = 0; i < 8; i++) {
                        //myHeatpoints[i].intensity = .75f;
                        //int randY = myRandom.nextInt(viewHeight + 1);
                        //int randX = myRandom.nextInt(viewWidth + 1);
                        //random float between .5-1.0
                        float randIntensity = (float) ((myRandom.nextFloat() * .5) + .5);
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

    public synchronized void addPoints(HeatPoint points[]) {
        mRenderer.addPoints(points);
        requestRender();
    }

    public void clear() {
        mRenderer.clear();
        requestRender();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //startRandomThread(height, width);
        //startRandomThread(height, width);

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!heatmapOn) {
                    //startRandomThread(height, width);

                    try {

                        heatmapOn = true;
                    } catch (Exception ex) {
                        Log.d(TAG, "couldn't start playback");
                    }

                }
        }
        return true;
    }

}
