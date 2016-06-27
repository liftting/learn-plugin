package com.cyg.learn.jni;

/**
 * Created by wm on 16/6/27.
 */
public class LearnJni {

    public native void actionString(String prompt);

    public native static void actionStringSt(String name);

    public native void actionArray(int[] data);

}
