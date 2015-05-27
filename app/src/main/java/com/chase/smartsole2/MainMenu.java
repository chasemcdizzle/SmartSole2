package com.chase.smartsole2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

        //setup with no action bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_menu);

        //get buttons
        profileButton = (Button) findViewById(R.id.profile_button);
        heatmapButton = (Button) findViewById(R.id.heatmap_button);
        sessionsButton = (Button) findViewById(R.id.sessions_button);

        //set button click attributes
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, MainActivity.class));
            }
        });

        heatmapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, MainActivity.class));
            }
        });

        sessionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //this should eventually go to the playback list
                Intent intent = new Intent(MainMenu.this, Tab2Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                //FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent);
            }
        });

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
