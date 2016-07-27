package com.cyg.learn.sur;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cyg.learn.sur.demo.Daemon;

public class DaemonService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
//        Daemon.run(this, DaemonService.class, Daemon.INTERVAL_ONE_MINUTE * 2);
        Daemon.run(this, DaemonService.class, Daemon.INTERVAL_ONE_MINUTE * 1);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* do something here */
        return super.onStartCommand(intent, flags, startId);
    }
}
