package com.cyg.learn.memoryload;

import android.app.Application;
import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wm on 16/6/29.
 */
public class ProxyApplication extends Application {


    static {
        System.loadLibrary("helloJni");
    }

    private Context mContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mContext = base;
    }

    private static Class clazz;

    public static Class getEntryActivityClass() {
        return clazz;
    }

    private void startLoadDynDex() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = getExternalCacheDir();
                String apkPath = file.getAbsolutePath() + File.separator + "AndroidDemo.apk";
                Log.i("jw", "apkpath:" + apkPath);
                Log.i("jw", "file exist:" + new File(apkPath).exists());
                ByteArrayOutputStream bos = null;
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(apkPath);
                    bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                    bos.flush();
                    loadDynDex(bos.toByteArray());
                } catch (Exception e) {
                    Log.i("jw", "error:" + Log.getStackTraceString(e));
                } finally {
                    try {
                        bos.close();
                        fis.close();
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    /**
     * 下面是通过反射的 native的方式来进行加载的，
     * @param dexContent
     */
    private void injectDexClassLoader(byte[] dexContent) {

        Object currentActivityThread = RefInvoke.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
        String packageName = getPackageName();
        ArrayMap mPackages = (ArrayMap) RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mPackages");
        WeakReference refToLoadedApk = (WeakReference) mPackages.get(packageName);
        ClassLoader clzLoader = (ClassLoader) RefInvoke.getFieldOjbect("android.app.LoadedApk", refToLoadedApk.get(), "mClassLoader");
        DynamicDexClassLoader dLoader = new DynamicDexClassLoader(
                mContext,
                dexContent,
                null,
                clzLoader,
                getPackageResourcePath(), getDir(".dex", MODE_PRIVATE).getAbsolutePath()
        );

        Object pthList = RefInvoke.getFieldOjbect(
                "dalvik.system.BaseDexClassLoader", clzLoader, "pathList");
        Class pathListClz = pthList.getClass();
        Method[] methods = pathListClz.getMethods();
        for (Method method : methods) {
            Log.i("jw", "method name:" + method.getName());
        }
        Log.i("jw", "pathList :" + pthList);
        Object dexElements = RefInvoke.getFieldOjbect(
                "dalvik.system.DexPathList", pthList, "dexElements");

        try {
            clazz = dLoader.findClass("cn.wjdiankong.demo.MainActivity");
            Log.i("jw", "class name:" + clazz.getName());
        } catch (Exception e) {
            Log.i("jw", "error:" + Log.getStackTraceString(e));
        }

        RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader", refToLoadedApk.get(), dLoader);
    }


    /**
     * //这种方式，是在DexFile的 openDexFile([])没有被废掉时，利用反射来进行的，
     *
     * @throws InvocationTargetException
     */
    public void loadDynDex(byte[] dexContent) throws NoSuchMethodException,
            ClassNotFoundException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        Log.i("jw", "load dex len:" + dexContent.length);

        /**
         * Class obj_class = Class.forName(class_name); Method method =
         * obj_class.getMethod(method_name,pareTyple); return
         * method.invoke(null, pareVaules);
         * */
        Class DexFileClz = Class.forName("dalvik.system.DexFile");

        Class[] paratype = new Class[1];
        paratype[0] = byte[].class;

        Object[] paraobj = new Object[1];
        paraobj[0] = dexContent;

        Method[] methods = DexFileClz.getMethods();
        for (Method method : methods) {
            Log.i("jw", "name:" + method.getName());
        }

        Method openDexFilemtd = DexFileClz.getDeclaredMethod("openDexFile",
                paratype);
        openDexFilemtd.setAccessible(true);
        int retv = (Integer) openDexFilemtd.invoke(null, paraobj);
        Log.i("jw", "return value : " + retv);

        int cookie = retv;

        paratype = new Class[1];
        paratype[0] = int.class;

        Method getClassNameListMtd = DexFileClz.getDeclaredMethod(
                "getClassNameList", paratype);
        getClassNameListMtd.setAccessible(true);
        paraobj[0] = cookie;
        String[] clznms = (String[]) getClassNameListMtd.invoke(null, paraobj);

        paratype = new Class[3];
        paratype[0] = String.class;
        paratype[1] = ClassLoader.class;
        paratype[2] = int.class;

        paraobj = new Object[3];
        paraobj[0] = "";
        paraobj[1] = getClassLoader();
        paraobj[2] = cookie;

        Method defineClassMtd = DexFileClz.getDeclaredMethod("defineClass",
                paratype);
        defineClassMtd.setAccessible(true);

        for (int i = 0; i < clznms.length; i++) {
            paraobj[0] = clznms[i];
            Class loadedclz = (Class) defineClassMtd.invoke(null, paraobj);
            Log.i("jw", "classname:" + loadedclz.getName());
        }
        updateClassLoader(cookie);
    }

    /**
     * 替换原始的DexClassLoader的 cookie变成当前我们加载的dexFile的cookie，就可以进行替换了
     *
     * @param newCookie
     */
    public void updateClassLoader(int newCookie) {
        Object currentActivityThread = null;

        Class paraTypes = null;
        Object paraObjs = null;

        currentActivityThread = RefInvoke.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
        String packageName = this.getPackageName();

        ArrayMap mPackages = (ArrayMap) RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mPackages");

        WeakReference refToLoadedApk = (WeakReference) mPackages
                .get(packageName);

        Object loadedApk = refToLoadedApk.get();
        Object clzLoader = RefInvoke.getFieldOjbect("android.app.LoadedApk",
                loadedApk, "mClassLoader");
        Log.i("SN", "mClassLoader :" + clzLoader);
        Object pthList = RefInvoke.getFieldOjbect(
                "dalvik.system.BaseDexClassLoader", clzLoader, "pathList");
        Log.i("SN", "pathList :" + pthList);
        Object dexElements = RefInvoke.getFieldOjbect(
                "dalvik.system.DexPathList", pthList, "dexElements");

        int length = Array.getLength(dexElements);

        for (int i = 0; i < length; i++) {
            Object ele = Array.get(dexElements, i);
            try {
                Object dexFile = RefInvoke.getFieldOjbect(
                        "dalvik.system.DexPathList$Element", ele, "dexFile");
                // 如果没有抛出异常,说明成功获取到了dex文件
                RefInvoke.setFieldOjbect("dalvik.system.DexFile", "mCookie",
                        dexFile, newCookie);
            } catch (Exception e) {
                continue;
            }
            break;// 只处理遇到的第一个dex文件
        }
    }
}
