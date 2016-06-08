package com.cyg.learn;

/**
 * Created by wm on 16/6/6.
 */
public class HelloJni {

    static {
        System.loadLibrary("helloJni");
    }

    public native String getName(String name);

}
