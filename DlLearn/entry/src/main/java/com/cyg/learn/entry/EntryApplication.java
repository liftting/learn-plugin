package com.cyg.learn.entry;

import android.app.Application;
import android.content.Context;

/**
 * Created by wm on 16/6/27.
 */
public class EntryApplication extends Application {

    static {
        System.loadLibrary("helloJni");
    }

    @Override
    public void onCreate() {
        super.onCreate();




    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        entryRunDex(base);
    }

    public native void entryRunDex(Context context);

    public native Object entryProtectExec(Context context);

}
