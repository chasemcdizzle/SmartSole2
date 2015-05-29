package com.chase.smartsole2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
    MyAdapter customAdapter;
    List<String> items;

    private class MyAdapter extends ArrayAdapter<String> {

        int resource;
        Context context;

        public MyAdapter(Context _context, int _resource, List<String> items) {
            super(_context, _resource, items);
            resource = _resource;
            context = _context;
            this.context = _context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout newView;

            final String message = getItem(position);

            // Inflate a new view if necessary.
            if (convertView == null) {
                newView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource,  newView, true);
            } else {
                newView = (LinearLayout) convertView;
            }

            // Fills in the view.
            TextView tv = (TextView) newView.findViewById(R.id.itemText);
            Button b = (Button) newView.findViewById(R.id.itemButton);
            Log.d("adapter:",message);
            tv.setText(message);
            b.setText("Playback");

            // Sets a listener for the button, and a tag for the button as well.
            b.setTag(new Integer(position));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textFileHandler = new TextFileHandler("", activityContext);
                    int[] points = textFileHandler.getIntArrayFromFile(message);
                    Bundle bundle = new Bundle();
                    bundle.putIntArray("playback", points);
                    Message msg = MainActivity.mainHandler.obtainMessage();
                    msg.setData(bundle);
                    startActivity(new Intent(SessionsActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    MainActivity.mainHandler.sendMessage(msg);
                }
            });


            // Set a listener for the whole list item.
            newView.setTag(message);
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(SessionsActivity.this, DatabaseActivity.class);
                    intent.putExtra("tableName", message);
                    context.startActivity(intent);
                }
            });

            return newView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);
        textFileHandler = new TextFileHandler("", this);
        //find saved session files to use for list
        File[] file = this.getFilesDir().listFiles();
        Log.d("Files", "Size: " + file.length);
        //initialize the items in the list, one for each file
        items = new ArrayList<String>();
        for (int i=0; i < file.length; i++)
        {
            int extensionIndex = file[i].getName().indexOf(".txt");
            //if the file is a .txt file, add it to the list
            if(extensionIndex != -1){
                //delete files if you want
                /*
                if(file[i].getName().equals("sessiontest9.txt"))
                    Log.d("sessions", "found sessiontest9.txt");
                else{
                    file[i].delete();
                }
                */
                //Log.d(MainActivity.class.getSimpleName(), ".txt @: " + file[i].getName().indexOf(".txt") + " length: " + file[i].getName().length());
                items.add(file[i].getName().substring(0, extensionIndex));
            }
        }

        //get listView object
        listView = (ListView)findViewById(R.id.listView);
        //create MyAdapter instance
        //Put them into a list so they will be mutable later
        //List<String> items = new ArrayList<String>();
        //create adapter for listview, tells listview what to do for each item/ where each item is
        //adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //assign adapter to listview
        customAdapter = new MyAdapter(this, R.layout.session_item, items);
        listView.setAdapter(customAdapter);

        //set click listener for items
        activityContext = this;
        //set up click handler for each item

        /*
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
        */
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
