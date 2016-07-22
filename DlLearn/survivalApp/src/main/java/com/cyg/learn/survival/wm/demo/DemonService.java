package com.cyg.learn.survival.wm.demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by wm on 16/7/22.
 */
public class DemonService extends Service {

    private static final String TAG = "cyg-service";

    public class LocalBinder extends Binder {
        public DemonService getService() {
            return DemonService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DemonService: onCreate()");

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "DemonService: onBind()");
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "DemonService: onDestroy()");
    }


}
