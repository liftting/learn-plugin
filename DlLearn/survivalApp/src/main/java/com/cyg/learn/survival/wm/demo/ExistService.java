package com.cyg.learn.survival.wm.demo;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.cyg.learn.survival.R;

/**
 * Created by wm on 16/7/22.
 */
public class ExistService extends Service {

    private static final String TAG = "cyg-service";

    private final int PID = android.os.Process.myPid();

    private DemonServiceConnection mConnection;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ExistService: onCreate()");

        setForeground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "ExistService: onDestroy()");
    }

    private Notification getNotification() {
        Notification noti;
        //定义一个notification
        Notification.Builder builder = new Notification.Builder(this).setContentText("前台服务")
                .setContentTitle("service").setSmallIcon(R.drawable.emoji_21);

        if (Build.VERSION.SDK_INT < 16) {
            noti = builder.getNotification();
        } else {
            noti = builder.build();
        }
        return noti;
    }

    public void setForeground() {

        if (null == mConnection) {
            mConnection = new DemonServiceConnection();
        }

        this.bindService(new Intent(this, DemonService.class), mConnection,
                Service.BIND_AUTO_CREATE);
    }


    private class DemonServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                Service demonSer = ((DemonService.LocalBinder) service).getService();
                ExistService.this.startForeground(PID, getNotification());
                demonSer.startForeground(PID, getNotification());

                demonSer.stopForeground(true);

                ExistService.this.unbindService(mConnection);
                mConnection = null;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
