package com.chase.smartsole2;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SaveService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SAVE = "com.db.chase.dbtest.action.save";
    private static final String ACTION_READ = "com.db.chase.dbtest.action.read";
    private static final String ACTION_RENAME = "com.db.chase.dbtest.action.rename";
    private static final String ACTION_TIME = "com.db.chase.dbtest.action.time";

    // TODO: Rename parameters
    public static final String EXTRA_PARAM1 = "com.db.chase.dbtest.extra.data";
    public static final String EXTRA_PARAM2 = "com.db.chase.dbtest.extra.filename";

    DatabaseHandler myDbHandler;

    Context context;

    String fileName;

    TextFileHandler textFileHandler;
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */



    public SaveService() {
        super("SaveIntent");
        //this.fileName = fileName;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        //myDbHandler = new DatabaseHandler(this, "testing4");
        //myDbHandler.newTable();
        fileName = "coolin.txt";
        textFileHandler = new TextFileHandler(fileName, this);
        //myDbHandler.receiveDataThread();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE.equals(action)) {
                final int[] data = intent.getIntArrayExtra(EXTRA_PARAM1);
                final String fileName = intent.getStringExtra(EXTRA_PARAM2);
                handleActionSave(data, fileName);
            }
            if (ACTION_READ.equals(action)) {
                final String fileName = intent.getStringExtra(EXTRA_PARAM2);
                handleActionRead(fileName);
            }
            if(ACTION_RENAME.equals(action)){
                final String oldName = intent.getStringExtra(EXTRA_PARAM2);
                handleActionRename(oldName);
            }
            if(ACTION_TIME.equals(action)){
                final long[] times = intent.getLongArrayExtra(EXTRA_PARAM1);
                final String fileName = intent.getStringExtra(EXTRA_PARAM2);
                handleActionTimes(times, fileName);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSave(int[] data, String fileName) {
        Log.d(MainActivity.class.getSimpleName(), "saving");
        //old db handler way
        //myDbHandler.addIntsToTable(data);
        //new textfile way
        textFileHandler.writeInts(data, fileName);
        Log.d(MainActivity.class.getSimpleName(), "saved");
    }

    private void handleActionRead(String fileName) {
        String result = textFileHandler.readFile(fileName);
        //Log.d(MainActivity.class.getSimpleName(), result);
        Log.d(MainActivity.class.getSimpleName(),  "length: " + result.length());
    }

    private void handleActionTimes(long[] times, String fileName){
        textFileHandler.writeTimes(times, fileName);
    }

    private void handleActionRename(String oldName){
        textFileHandler.renameFile(oldName);
    }

}
