package com.chase.smartsole2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private String tableName;
    DateFormat dateFormatter;
    Date today;
    String s;

    //store database object
    SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dateFormatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
        today = new Date();
    }

    public void newTable(){
        dateFormatter.setLenient(false);
        s = dateFormatter.format(today);
        tableName = s;
        String CREATE_DATA_TABLE = "CREATE TABLE " + tableName + "(" + TIMESTAMP + " TEXT," + SENSOR
                + " INTEGER," + INTENSITY + " INTEGER)";
        db.execSQL(CREATE_DATA_TABLE);
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

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //we are reusing this class to create many tables, so
        //commented this code out
        /*
        String CREATE_DATA_TABLE = "CREATE TABLE " + tableName + "(" + TIMESTAMP + " TEXT," + SENSOR
                + " INTEGER," + INTENSITY + " INTEGER)";
                        db.execSQL(CREATE_DATA_TABLE);
        */
        this.db = db;

    }


    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + tableName);

        // Create tables again
        onCreate(db);
    }
}