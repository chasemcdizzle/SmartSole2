package com.chase.smartsole2;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chase on 5/25/2015.
 */
public class TextFileHandler {
    String fileName;
    //OutputStreamWriter output;
    //InputStreamReader input;
    FileOutputStream output;
    FileInputStream input;
    File file;
    Context context;
    public TextFileHandler(String fileName, Context context){
        this.fileName = fileName + ".txt";
        this.context = context;
        file = context.getFileStreamPath(fileName);
        try {
            if (!file.exists())
                file.createNewFile();
        }
        catch(Exception e){
            Log.d(MainActivity.class.getSimpleName(), e.toString());
        }
    }

    public void writeInts(int[] data, String fileName){
        try {
            file = context.getFileStreamPath(fileName + ".txt");
            try {
                if (!file.exists())
                    file.createNewFile();
            }
            catch(Exception e){
                Log.d(MainActivity.class.getSimpleName(), e.toString());
            }
            output = new FileOutputStream(file, true);
        }
        catch(Exception e){
            Log.d(MainActivity.class.getSimpleName(), "Could not openfileOutput");
        }
        String datastring = "";
        try {
            for (int i = 0; i < data.length; i++) {
                //output.write(String.valueOf(data[i]));
                datastring += String.valueOf(data[i]);
                datastring += "\n";
            }
            output.write(datastring.getBytes());
        }
        catch(Exception e){
            Log.d(MainActivity.class.getSimpleName(), e.toString());
            Log.d(MainActivity.class.getSimpleName(), "couldn't write data");
        }
    }

    public List<HeatPoint> getPointsFromFile(String fileName){
        List<HeatPoint> pointList = new ArrayList<HeatPoint>();
        //loop through all of the rows of the table
        int sensorNum = 0;
        String fileData = readFile(fileName);
        String[] sensors = fileData.split("\n");
        for(int i = 0; i < sensors.length; i++){
            if(!sensors.equals("")) {
                int intensity = Integer.parseInt(sensors[i]);
                HeatPoint myPoint = new HeatPoint(0, 0, 200, intensity);
                pointList.add(myPoint);
            }
        }
        return pointList;
    }

    public int[] getIntArrayFromFile(String fileName){
        //loop through all of the rows of the table
        int sensorNum = 0;
        String fileData = readFile(fileName);
        String[] sensors = fileData.split("\n");
        //make numpoints the same size as the sensors string array
        int[] points = new int[sensors.length];
        for(int i = 0; i < sensors.length; i++){
            Log.d("textfile handler", sensors[i]);
            if(!sensors[i].equals("")) {
                int intensity = Integer.parseInt(sensors[i]);
                points[i] = intensity;
            }
        }
        return points;
    }

    public String readFile(String fileName){
        Log.d(MainActivity.class.getSimpleName(), "in readFile() ");
        file = context.getFileStreamPath(fileName + ".txt");
        try {
            if (!file.exists())
                file.createNewFile();
        }
        catch(Exception e){
            Log.d(MainActivity.class.getSimpleName(), e.toString());
        }
        try {
            input = new FileInputStream(file);
        }
        catch(Exception e){
            Log.d(MainActivity.class.getSimpleName(), "Could not openfileOutput");
        }
        //Log.d(MainActivity.class.getSimpleName(), "right before length = ");
        int length = (int) file.length();
        //Log.d(MainActivity.class.getSimpleName(), "Printing length: ");
        //Log.d(MainActivity.class.getSimpleName(), String.valueOf(length));
        byte[] bytes = new byte[length];
        try {
            try {
                input.read(bytes);
            } finally {
                input.close();
            }
        }
        catch(Exception e){
            Log.d(MainActivity.class.getSimpleName(), "couldn't read data");
        }


        String contents = new String(bytes);
        return contents;
    }

    public void renameFile(String origName){
        setFileNameDialogue(origName);
    }
    public void setFileNameDialogue(String origName){
        final EditText input = new EditText(context);
        final String oldName = origName;
        new AlertDialog.Builder(context)
                .setTitle("Enter name")
                .setMessage("Enter a name for your recording")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String in = input.getText().toString();
                        //if they entered a valid integer
                        if(!in.contains(".txt")) {
                            file = context.getFileStreamPath(oldName + ".txt");
                            File newFile = context.getFileStreamPath(in + ".txt");
                            file.renameTo(newFile);
                        }
                        else{
                            alert("Filename must not contain the term .txt");
                            setFileNameDialogue(oldName);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    public void alert(String msg){
        new AlertDialog.Builder(context)
                .setTitle(msg)
                .setMessage("You must enter an integer value.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .show();
    }

}
