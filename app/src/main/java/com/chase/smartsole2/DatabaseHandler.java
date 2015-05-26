package com.chase.smartsole2;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "soleData";

    // Contacts Table Columns names
    private static final String INTENSITY = "intensity";
    private static final String SENSOR = "sensor";
    private static final String TIMESTAMP = "timestamp";

    private static final String TABLE_NAME = "testing1";

    private static String tableName;
    DateFormat dateFormatter;
    Date today;
    String s;

    //store database object
    //SQLiteDatabase db;

    public DatabaseHandler(Context context, String tableName) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dateFormatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
        today = new Date();
        this.tableName = tableName;
    }

    public void newTable(){
        dateFormatter.setLenient(false);
        s = dateFormatter.format(today);
        //tableName = s;
        String CREATE_DATA_TABLE = "CREATE TABLE IF NOT EXISTS " + tableName + "(" + TIMESTAMP + " TEXT," + SENSOR
                + " INTEGER," + INTENSITY + " INTEGER)";
        this.getWritableDatabase().execSQL(CREATE_DATA_TABLE);
    }

    public void printTableNames(){

        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            while ( !cursor.isAfterLast() ) {
                Log.d(MainActivity.class.getSimpleName(),cursor.getString(0));
                cursor.moveToNext();
            }
        }
    }

    public ArrayList<String> getTableNames(){
        ArrayList<String> tableNames = new ArrayList<String>();
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (cursor.moveToFirst()) {
            while ( !cursor.isAfterLast() ) {
                String tableName = cursor.getString(0);
                if(!tableName.equals("android_metadata"))
                    tableNames.add(tableName);
                Log.d(MainActivity.class.getSimpleName(),tableName);
                cursor.moveToNext();
            }
        }
        return tableNames;
    }

    public List<HeatPoint> getHeatPointsFromTable(){
        List<HeatPoint> pointList = new ArrayList<HeatPoint>();

        //select all from table
        String selectTableItems = "SELECT  * FROM " + tableName;
        Cursor cursor = this.getReadableDatabase().rawQuery(selectTableItems, null);

        //loop through all of the rows of the table
        int sensorNum = 0;
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                float intensity = cursor.getInt(2);
                HeatPoint myPoint = new HeatPoint(0,0, 200, intensity);
                pointList.add(myPoint);
                Log.d(MainActivity.class.getSimpleName(), "found point intensity: " + intensity);
                cursor.moveToNext();
            }
        }

        return pointList;
    }

    public void addFrameToTable(HeatPoint myPoints[]){
        //access writable database
        SQLiteDatabase db = this.getWritableDatabase();
        //Create key/value pairs
        String timestamp = dateFormatter.format(today);
        for(int i = 0; i < myPoints.length; i++) {
            ContentValues values = new ContentValues();
            values.put(TIMESTAMP, timestamp);
            values.put(SENSOR, i);
            values.put(INTENSITY, myPoints[i].intensity);
            db.insert(tableName, null, values);
        }
        db.close();
    }

    public void addIntsToTable(int[] saveData){
        //access writable database
        SQLiteDatabase db = this.getWritableDatabase();
        //Create key/value pairs
        String timestamp = dateFormatter.format(today);
        for(int i = 0; i < saveData.length; i++) {
            ContentValues values = new ContentValues();
            values.put(TIMESTAMP, timestamp);
            values.put(SENSOR, i%8);
            values.put(INTENSITY, saveData[i]);
            db.insert(tableName, null, values);
        }
        db.close();
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        dateFormatter.setLenient(false);
        s = dateFormatter.format(today);
        //tableName = s;
        String CREATE_DATA_TABLE = "CREATE TABLE " + tableName + "(" + TIMESTAMP + " TEXT," + SENSOR
                + " INTEGER," + INTENSITY + " INTEGER)";
        db.execSQL(CREATE_DATA_TABLE);
        //this.db = db;
        */

    }


    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        // Create tables again
        onCreate(db);
    }
    //USE SERVICE TO ACCOMPLISH SAVING!
    //
    public void receiveDataThread() {
        Thread t = new Thread(new Runnable() {
            boolean saveData = false;
            public android.os.Handler _handler = new android.os.Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.d(MainActivity.class.getSimpleName(), "received data in database handler handler");
                    int[] sensorData = msg.getData().getIntArray("sensorData");
                    addIntsToTable(sensorData);
                }
            };
            public void run() {
                //do nothing, maybe infinite loop if this doesn't work
            }
        }){

        };
        t.start();
    }
}