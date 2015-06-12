package com.chase.smartsole2;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DatabaseActivity extends ActionBarActivity {
    TableLayout table_layout;
    String tableName;
    String sessionName;
    TextView title;
    TextFileHandler textFileHandler;
    Button backButton;
    SimpleDateFormat dt;
    NumberFormat numFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        //initialize date format
        dt = new SimpleDateFormat("hh:mm:ss.S");
        //number format for pressure
        numFormat = new DecimalFormat("#0.00");

        //grab tablename from bundle
        Bundle crossActivityBundle = getIntent().getExtras();
        tableName = crossActivityBundle.getString("tableName");
        sessionName = crossActivityBundle.getString("sessionName");

        table_layout = (TableLayout) findViewById(R.id.tableLayout);
        title = (TextView) findViewById(R.id.textView);
        title.setText(sessionName);

        //setup textfilehandler
        textFileHandler = new TextFileHandler(tableName, this);

        //create a row / 3 collumns for the "key"
        TableRow keyrow = new TableRow(this);
        keyrow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        //create textview's for each collumn
        for (int j = 0; j < 3; j++) {

            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            //tv.setBackgroundResource(R.drawable.cell_shape);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setPadding(15, 5, 15, 5);
            if(j == 0)
                tv.setText("Sensor");
            else if(j == 1)
                tv.setText("Pressure");
            else if(j == 2)
                tv.setText("Timestamp");
            keyrow.addView(tv);
        }
        //finally add row to the table layout
        table_layout.addView(keyrow);


        int[] points = textFileHandler.getIntArrayFromFile(tableName);
        long[] times = textFileHandler.getLongArrayFromFile(tableName);

        Log.d("databaseactivity", "points.length = " + points.length);
        //create a row for each heatpoint
        for (int i = 0; i < points.length; i++) {

            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            //create textview's for each collumn
            for (int j = 0; j < 3; j++) {

                TextView tv = new TextView(this);
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                //tv.setBackgroundResource(R.drawable.cell_shape);
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(18);
                tv.setPadding(5, 5, 5, 5);
                if(j == 0)
                    tv.setText("" + i % 8);
                else if(j == 1)
                    tv.setText(numFormat.format(points[i]*.1178) + " lb");
                else if(j == 2) {
                    Date resultDate = new Date(times[i]);
                    tv.setText("" + dt.format(resultDate));
                }
                row.addView(tv);
            }

            table_layout.addView(row);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_database, menu);
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
