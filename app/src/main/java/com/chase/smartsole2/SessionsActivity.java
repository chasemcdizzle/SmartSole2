package com.chase.smartsole2;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SessionsActivity extends ActionBarActivity {

    TextFileHandler textFileHandler;

    //listview fields
    ListView listView;
    ArrayAdapter<String> adapter;
    Context activityContext;
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);
        textFileHandler = new TextFileHandler("", this);
        //find saved session files to use for list
        File[] file = this.getFilesDir().listFiles();
        Log.d("Files", "Size: " + file.length);
        //initialize the items in the list, one for each file
        List<String> items = new ArrayList<String>();
        for (int i=0; i < file.length; i++)
        {
            int extensionIndex = file[i].getName().indexOf(".txt");
            //if the file is a .txt file, add it to the list
            if(extensionIndex != -1){
                //Log.d(MainActivity.class.getSimpleName(), ".txt @: " + file[i].getName().indexOf(".txt") + " length: " + file[i].getName().length());
                items.add(file[i].getName().substring(0, extensionIndex));
            }
        }

        //get listView object
        listView = (ListView)findViewById(R.id.listView);
        //Put them into a list so they will be mutable later
        //List<String> items = new ArrayList<String>();
        //create adapter for listview, tells listview what to do for each item/ where each item is
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //assign adapter to listview
        listView.setAdapter(adapter);

        //set click listener for items
        activityContext = this;
        //set up click handler for each item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //should show the contents of the table
                Context context = view.getContext();
                Intent intent = new Intent(SessionsActivity.this, DatabaseActivity.class);
                intent.putExtra("tableName", adapter.getItem(position));
                context.startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sessions, menu);
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
