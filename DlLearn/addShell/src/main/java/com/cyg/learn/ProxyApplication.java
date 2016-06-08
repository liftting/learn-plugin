package com.cyg.learn;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dalvik.system.DexClassLoader;

/**
 * Created by wm on 16/6/7.
 */
public class ProxyApplication extends Application {
    private static final String appkey = "APPLICATION_CLASS_NAME";
    private static final String TAG = "ProxyApplication";
    private String apkFileName;
    private String odexPath;
    private String libPath;


    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            File odex = this.getDir("payload_odex", MODE_PRIVATE);
            File libs = this.getDir("payload_lib", MODE_PRIVATE);
            odexPath = odex.getAbsolutePath();
            libPath = libs.getAbsolutePath();

            apkFileName = odex.getAbsolutePath() + "/payload.apk";
            File dexFile = new File(apkFileName);
            if (!dexFile.exists())
                dexFile.createNewFile();


            // 读取程序classes.dex文件
            byte[] dexdata = this.readDexFileFromApk();


            // 分离出解壳后的apk文件已用于动态加载
            this.splitPayLoadFromDex(dexdata);


            // 配置动态加载环境
            Object currentActivityThread = RefInvoke.invokeStaticMethod(
                    "android.app.ActivityThread", "currentActivityThread",
                    new Class[]{}, new Object[]{});


            String packageName = this.getPackageName();
            HashMap mPackages = (HashMap) RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread", currentActivityThread,
                    "mPackages");


            WeakReference wr = (WeakReference) mPackages.get(packageName);

            // 替换DexClassLoader，动态加载原理加密的apk，将其解密出来，重新加载
            DexClassLoader dLoader = new DexClassLoader(apkFileName, odexPath,
                    libPath, (ClassLoader) RefInvoke.getFieldOjbect(
                    "android.app.LoadedApk", wr.get(), "mClassLoader")
            );

            /**
             *
             * 1，上面讲 解密后的原生apk解压到文件中apkFileName ,
             * 2，构建DexClassLoader 去加载这个apk文件，修改下父类加载器是使用这个应用的加载器，
             *
             *
             *
             */


            //将系统的classloader 进行替换，
            RefInvoke.setFieldOjbect("android.app.LoadedApk", "mClassLoader",
                    wr.get(), dLoader);


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void onCreate() {
        Log.d(TAG, "on create hello world~");
        // 如果源应用配置有Appliction对象，则替换为源应用Applicaiton，以便不影响源程序逻辑。
        String appClassName = null;
        try {
            ApplicationInfo ai = this.getPackageManager().getApplicationInfo(
                    this.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if (bundle != null && bundle.containsKey("APPLICATION_CLASS_NAME")) {
                appClassName = bundle.getString("APPLICATION_CLASS_NAME");
            } else {
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "the app aplication name is " + appClassName);
        /**
         * 调用静态方法android.app.ActivityThread.currentActivityThread
         * 获取当前activity所在的线程对象
         */
        Object currentActivityThread = RefInvoke.invokeStaticMethod(
                "android.app.ActivityThread", "currentActivityThread",
                new Class[]{}, new Object[]{});
        /**
         * 获取currentActivityThread中的mBoundApplication属性对象，该对象是一个
         *  AppBindData类对象，该类是ActivityThread的一个内部类
         */
        Object mBoundApplication = RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mBoundApplication");
        /**
         * 获取mBoundApplication中的info属性，info 是 LoadedApk类对象
         */
        Object loadedApkInfo = RefInvoke.getFieldOjbect(
                "android.app.ActivityThread$AppBindData", mBoundApplication,
                "info");
        /**
         * loadedApkInfo对象的mApplication属性置为null
         */
        RefInvoke.setFieldOjbect("android.app.LoadedApk", "mApplication",
                loadedApkInfo, null);

        /**
         * 获取currentActivityThread对象中的mInitialApplication属性
         * 这货是个正牌的 Application
         */
        Object oldApplication = RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mInitialApplication");
        /**
         * 获取currentActivityThread对象中的mAllApplications属性
         * 这货是 装Application的列表
         */
        ArrayList<Application> mAllApplications = (ArrayList<Application>) RefInvoke
                .getFieldOjbect("android.app.ActivityThread",
                        currentActivityThread, "mAllApplications");
        //列表对象终于可以直接调用了 remove调了之前获取的application 抹去记录的样子
        mAllApplications.remove(oldApplication);
        /**
         * 获取前面得到LoadedApk对象中的mApplicationInfo属性，是个ApplicationInfo对象
         */
        ApplicationInfo appinfo_In_LoadedApk = (ApplicationInfo) RefInvoke
                .getFieldOjbect("android.app.LoadedApk", loadedApkInfo,
                        "mApplicationInfo");
        /**
         * 获取前面得到AppBindData对象中的appInfo属性，也是个ApplicationInfo对象
         */
        ApplicationInfo appinfo_In_AppBindData = (ApplicationInfo) RefInvoke
                .getFieldOjbect("android.app.ActivityThread$AppBindData",
                        mBoundApplication, "appInfo");
        //把这两个对象的className属性设置为从meta-data中获取的被加密apk的application路径
        appinfo_In_LoadedApk.className = appClassName;
        appinfo_In_AppBindData.className = appClassName;
        /**
         * 调用LoadedApk中的makeApplication 方法 造一个application
         * 前面改过路径了
         */
        Application app = (Application) RefInvoke.invokeMethod(
                "android.app.LoadedApk", "makeApplication", loadedApkInfo,
                new Class[]{boolean.class, Instrumentation.class},
                new Object[]{false, null});
        RefInvoke.setFieldOjbect("android.app.ActivityThread",
                "mInitialApplication", currentActivityThread, app);

        HashMap mProviderMap = (HashMap) RefInvoke.getFieldOjbect(
                "android.app.ActivityThread", currentActivityThread,
                "mProviderMap");
        Iterator it = mProviderMap.values().iterator();
        while (it.hasNext()) {
            Object providerClientRecord = it.next();
            Object localProvider = RefInvoke.getFieldOjbect(
                    "android.app.ActivityThread$ProviderClientRecord",
                    providerClientRecord, "mLocalProvider");
            RefInvoke.setFieldOjbect("android.content.ContentProvider",
                    "mContext", localProvider, app);
        }

        if (null == app) {
            Log.e(TAG, "application get is null !");
        } else {
            app.onCreate();
        }
    }


    private void splitPayLoadFromDex(byte[] data) throws IOException {
        byte[] apkdata = decrypt(data);
        int ablen = apkdata.length;
        byte[] dexlen = new byte[4];
        System.arraycopy(apkdata, ablen - 4, dexlen, 0, 4);
        ByteArrayInputStream bais = new ByteArrayInputStream(dexlen);
        DataInputStream in = new DataInputStream(bais);
        int readInt = in.readInt();
        System.out.println(Integer.toHexString(readInt));
        byte[] newdex = new byte[readInt];
        System.arraycopy(apkdata, ablen - 4 - readInt, newdex, 0, readInt);

        // 解密后，将原来的apk文件存储到文件中

        File file = new File(apkFileName);
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream(file);
            localFileOutputStream.write(newdex);
            localFileOutputStream.close();


        } catch (IOException localIOException) {
            throw new RuntimeException(localIOException);
        }


        ZipInputStream localZipInputStream = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(file)));
        while (true) {
            ZipEntry localZipEntry = localZipInputStream.getNextEntry();
            if (localZipEntry == null) {
                localZipInputStream.close();
                break;
            }
            String name = localZipEntry.getName();
            if (name.startsWith("lib/") && name.endsWith(".so")) {
                File storeFile = new File(libPath + "/"
                        + name.substring(name.lastIndexOf('/')));
                storeFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(storeFile);
                byte[] arrayOfByte = new byte[1024];
                while (true) {
                    int i = localZipInputStream.read(arrayOfByte);
                    if (i == -1)
                        break;
                    fos.write(arrayOfByte, 0, i);
                }
                fos.flush();
                fos.close();
            }
            localZipInputStream.closeEntry();
        }
        localZipInputStream.close();


    }


    private byte[] readDexFileFromApk() throws IOException {
        ByteArrayOutputStream dexByteArrayOutputStream = new ByteArrayOutputStream();
        ZipInputStream localZipInputStream = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(
                        this.getApplicationInfo().sourceDir))
        );
        while (true) {
            ZipEntry localZipEntry = localZipInputStream.getNextEntry();
            if (localZipEntry == null) {
                localZipInputStream.close();
                break;
            }
            if (localZipEntry.getName().equals("classes.dex")) {
                byte[] arrayOfByte = new byte[1024];
                while (true) {
                    int i = localZipInputStream.read(arrayOfByte);
                    if (i == -1)
                        break;
                    dexByteArrayOutputStream.write(arrayOfByte, 0, i);
                }
            }
            localZipInputStream.closeEntry();
        }
        localZipInputStream.close();
        return dexByteArrayOutputStream.toByteArray();
    }


    // //直接返回数据，读者可以添加自己解密方法
    private byte[] decrypt(byte[] data) {
        return data;
    }
}
