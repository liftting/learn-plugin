package com.cyg.learn.jni;

/**
 * Created by wm on 16/6/27.
 */
public class LearnJni {

    static {
        System.loadLibrary("helloJni");
    }

    public native void actionString(String prompt);

    public native static void actionStringSt(String name);

    public native int actionArray(int[] data);

}
