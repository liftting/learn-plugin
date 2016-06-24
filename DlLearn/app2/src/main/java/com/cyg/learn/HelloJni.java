package com.cyg.learn;

import java.util.ArrayList;

/**
 * Created by wm on 16/6/6.
 */
public class HelloJni {

    static {
        System.loadLibrary("helloJni");
    }

    public native String getName(String name);

    public native String[] getArray();

    public native ScanResult getResult();

    public native ArrayList<Student> getArrayList();

}
