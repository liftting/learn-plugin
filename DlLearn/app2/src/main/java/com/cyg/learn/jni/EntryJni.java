package com.cyg.learn.jni;

import android.content.Context;

/**
 * Created by wm on 16/6/24.
 */
public class EntryJni {

    public native void entryRunDex(Context context);

    public native Object entryProtectExec(Context context);


}
