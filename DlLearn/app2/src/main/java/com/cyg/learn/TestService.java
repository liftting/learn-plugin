package com.cyg.learn;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by wm on 16/6/6.
 */
public class TestService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
