package com.chase.smartsole2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Tab2Activity extends FragmentActivity {

    ListView listView;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    ArrayAdapter<String> adapter;
    Context activityContext;
    TextFileHandler textFileHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_tab2);
        //setup saved prefs
        prefs = this.getSharedPreferences(
                "com.chase.smartsole2", Context.MODE_PRIVATE);
        //initialize the preference editor for saving later
        editor = prefs.edit();
        //must clear it for some strange reason
        editor.clear();
        //get listView object
        listView = (ListView)findViewById(R.id.listView2);

        //string values for each item
        String[] itemStrings = {
                "Name: " + prefs.getString("name", ""),
                "Total Steps: ",
                "Steps Today: ",
                "Daily Step Goal: " + prefs.getInt("goal", 0)
                //"Record",
                //"Sessions",
                //"Playback"
        };
        //Put them into a list so they will be mutable later
        List<String> items = new ArrayList<String>(Arrays.asList(itemStrings));
        //create adapter for listview
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //assign adapter to listview
        listView.setAdapter(adapter);

        //set click listener for items
        activityContext = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // ListView Clicked item index
                final int itemPosition = position;

                //name is being set
                if(itemPosition == 0){
                    setName(itemPosition);
                }
                //step goal is being set
                else if(itemPosition == 3){
                    // Set an EditText view to get user input
                    setStepGoal(itemPosition);
                }
                //saves data
                /*
                else if(itemPosition == 4){
                    //saveData = msg.getData().getBoolean("save");
                    Log.d(MainActivity.class.getSimpleName(), "record clicked");
                    //saveFileName = msg.getData().getString("filename");
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("save", true);
                    bundle.putString("filename", "sessiontest5");
                    //mainHandler.sendEmptyMessage(0);
                    Message message = MainActivity.mainHandler.obtainMessage();
                    message.setData(bundle);

                    startActivity( new Intent(Tab2Activity.this, MainActivity.class).addFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT )  );
                    MainActivity.mainHandler.sendMessage(message);
                }
                //read file
                else if(itemPosition == 5){
                    Intent msgIntent = new Intent(activityContext, SaveService.class);
                    msgIntent.putExtra(SaveService.EXTRA_PARAM2, "sessiontest7");
                    msgIntent.setAction("com.db.chase.dbtest.action.read");
                    startService(msgIntent);
                }
                //get points from file, playback
                else if(itemPosition == 6){
                    textFileHandler = new TextFileHandler("", activityContext);
                    int[] points = textFileHandler.getIntArrayFromFile("sessiontest9");
                    Bundle bundle = new Bundle();
                    bundle.putIntArray("playback", points);
                    Message message = MainActivity.mainHandler.obtainMessage();
                    message.setData(bundle);
                    startActivity(new Intent(Tab2Activity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                    MainActivity.mainHandler.sendMessage(message);
                }
                */
                else {
                    // ListView Clicked item value
                    String itemValue = (String) listView.getItemAtPosition(position);

                    // Show Alert
                    Toast.makeText(getApplicationContext(),
                            "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                            .show();
                }

            }
        });
    }

    //overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);

    public void alert(String msg){
        new AlertDialog.Builder(this)
                .setTitle(msg)
                .setMessage("You must enter an integer value.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .show();
    }

    public void setName(final int itemPosition){
        final EditText input = new EditText(activityContext);
        new AlertDialog.Builder(activityContext)
                .setTitle("Set Name")
                .setMessage("Enter your name")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String in = input.getText().toString();
                        //if they entered a valid integer
                            String currString = adapter.getItem(itemPosition);
                            //save the value they just entered
                            editor.putString("name", in);
                            editor.apply();
                            //remove old string item and place a new one
                            adapter.remove(currString);
                            adapter.insert("Name: " + in, itemPosition);
                            adapter.notifyDataSetChanged();
                            //Log.d(MainActivity.class.getSimpleName(), currGoal);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }

    public void setStepGoal(final int itemPosition){
        final EditText input = new EditText(activityContext);
        new AlertDialog.Builder(activityContext)
                .setTitle("Set New Goal")
                .setMessage("Enter Step Goal")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String in = input.getText().toString();
                        //if they entered a valid integer
                        if(in.matches("\\d+")) {
                            String currGoalString = adapter.getItem(itemPosition);
                            //int currGoal = Integer.parseInt(currGoalString.substring(currGoalString.indexOf(":") + 2));
                            //save the value they just entered
                            editor.putInt("goal", Integer.parseInt(in));
                            editor.apply();
                            //remove old string item and place a new one
                            adapter.remove(currGoalString);
                            adapter.insert("Daily Step Goal: " + in, itemPosition);
                            adapter.notifyDataSetChanged();
                            //Log.d(MainActivity.class.getSimpleName(), currGoal);
                        }
                        else{
                            alert("Please enter an integer value.");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tab2, menu);
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
