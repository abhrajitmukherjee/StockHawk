package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {
    Handler mMainThreadHandler = null;

  public StockIntentService(){
    super(StockIntentService.class.getName());
      mMainThreadHandler = new Handler();
  }

  public StockIntentService(String name) {
    super(name);
  }

  @Override protected void onHandleIntent(Intent intent) {
    Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
    StockTaskService stockTaskService = new StockTaskService(this);
    Bundle args = new Bundle();
    if (intent.getStringExtra("tag").equals("add")){
      args.putString("symbol", intent.getStringExtra("symbol"));
    }
    // We can call OnRunTask from the intent service to force it to run immediately instead of
    // scheduling a task.
    int res=stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    if (res==-1){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                CharSequence text = "Invalid Symbol";
                int duration = Toast.LENGTH_SHORT;
                Log.v("test me", "yes symbol is wrong");
                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }
  }
}
