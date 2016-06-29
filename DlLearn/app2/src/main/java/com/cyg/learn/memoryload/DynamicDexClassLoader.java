package com.cyg.learn.memoryload;

import android.content.Context;
import android.util.Log;

import com.cyg.learn.jni.NativeTool;

import java.net.URL;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * Created by wm on 16/6/29.
 */
public class DynamicDexClassLoader extends DexClassLoader {

    private static final String TAG = DynamicDexClassLoader.class.getName();

    private Context mContext;

    private int cookie;

    public DynamicDexClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    public DynamicDexClassLoader(Context context, byte[] dexBytes,
                                 String libraryPath, ClassLoader parent, String oriPath,
                                 String fakePath) {
        super(oriPath, fakePath, libraryPath, parent);
        setContext(context);
        setCookie(NativeTool.loadDex(dexBytes, dexBytes.length));
    }

    private void setCookie(int kie) {
        cookie = kie;
    }

    private void setContext(Context context) {
        mContext = context;
    }


    private String[] getClassNameList(int cookie) {
        return (String[]) RefInvoke.invokeDeclaredStaticMethod(DexFile.class.getName(),
                "getClassNameList", new Class[]{int.class},
                new Object[]{cookie});
    }

    private Class defineClass(String name, ClassLoader loader, int cookie) {
        Log.i(TAG, "define class:" + name);
        return (Class) RefInvoke.invokeDeclaredStaticMethod(DexFile.class.getName(),
                "defineClassNative", new Class[]{String.class, ClassLoader.class,
                        int.class}, new Object[]{name, loader, cookie}
        );
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Log.d(TAG, "findClass-" + name);
        Class<?> cls = null;

        String as[] = getClassNameList(cookie);
        for (int z = 0; z < as.length; z++) {
            if (as[z].equals(name)) {
                cls = defineClass(as[z].replace('.', '/'),
                        mContext.getClassLoader(), cookie);
            } else {
                defineClass(as[z].replace('.', '/'), mContext.getClassLoader(),
                        cookie);
            }
        }

        if (null == cls) {
            cls = super.findClass(name);
        }

        return cls;
    }

    @Override
    protected URL findResource(String name) {
        Log.d(TAG, "findResource-" + name);
        return super.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) {
        Log.d(TAG, "findResources ssss-" + name);
        return super.findResources(name);
    }

    @Override
    protected synchronized Package getPackage(String name) {
        Log.d(TAG, "getPackage-" + name);
        return super.getPackage(name);
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        Log.d(TAG, "loadClass-" + className + " resolve " + resolve);
        Class<?> clazz = super.loadClass(className, resolve);
        if (null == clazz) {
            Log.e(TAG, "loadClass fail,maybe get a null-point exception.");
        }
        return clazz;
    }

    @Override
    protected Package[] getPackages() {
        Log.d(TAG, "getPackages sss-");
        return super.getPackages();
    }

    @Override
    protected Package definePackage(String name, String specTitle,
                                    String specVersion, String specVendor, String implTitle,
                                    String implVersion, String implVendor, URL sealBase)
            throws IllegalArgumentException {
        Log.d(TAG, "definePackage" + name);
        return super.definePackage(name, specTitle, specVersion, specVendor,
                implTitle, implVersion, implVendor, sealBase);
    }


}
