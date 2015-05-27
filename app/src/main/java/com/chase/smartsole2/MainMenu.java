package com.chase.smartsole2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


public class MainMenu extends Activity {

    private Button profileButton;
    private Button heatmapButton;
    private Button sessionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //test
        Log.d("main menu","CREATED");

        //setup with no action bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_menu);

        //get buttons
        profileButton = (Button) findViewById(R.id.profile_button);
        heatmapButton = (Button) findViewById(R.id.heatmap_button);
        sessionsButton = (Button) findViewById(R.id.sessions_button);

        //start the heatmap activity (it will go in the bg because the on create puts this back... very jank)
        startActivity( new Intent(MainMenu.this, MainActivity.class) );

        //set button click attributes
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //flag is to not destroy itself
                startActivity( new Intent(MainMenu.this, MainActivity.class).addFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT ) );
            }
        });

        heatmapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(MainMenu.this, MainActivity.class).addFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT )  );
            }
        });

        sessionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this should eventually go to the playback list
                startActivity( new Intent(MainMenu.this, MainActivity.class).addFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT )  );
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("main menu","DESTROYED");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
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
