package com.cyg.learn.survival.wm.demo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.cyg.learn.survival.R;

/**
 * Created by wm on 16/7/21.
 */
public class CygService extends Service {

    public static final String TAG = "CygForegroundService";

    @Override
    public void onCreate() {
        super.onCreate();


        Notification noti;
        //定义一个notification
        Notification.Builder builder = new Notification.Builder(this).setContentText("前台服务")
                .setContentTitle("service").setSmallIcon(R.drawable.emoji_21);

        if (Build.VERSION.SDK_INT < 16) {
            noti = builder.getNotification();
        } else {
            noti = builder.build();
        }

        //把该service创建为前台service
        startForeground(1, noti);
    }

    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "enter on start command");
        return super.onStartCommand(intent, flags, startId);
    }
}
