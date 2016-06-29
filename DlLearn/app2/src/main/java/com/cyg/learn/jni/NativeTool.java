package com.cyg.learn.jni;

/**
 * Created by wm on 16/6/29.
 * 这个是用来处理内存中动态加载so的，
 *
 * 1，去调用jni中的 openDexFile对象，
 *
 */
public class NativeTool {

    public static native int loadDex(byte[] dex, long dexlen);

}
