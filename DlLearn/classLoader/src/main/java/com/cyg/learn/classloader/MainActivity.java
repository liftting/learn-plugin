package com.cyg.learn.classloader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("DEMO", "Context的类加载加载器:" + Context.class.getClassLoader());
        Log.i("DEMO", "ListView的类加载器:" + ListView.class.getClassLoader());
        Log.i("DEMO", "应用程序默认加载器:" + getClassLoader());
        Log.i("DEMO", "系统类加载器:" + ClassLoader.getSystemClassLoader());
        Log.i("DEMO", "系统类加载器和Context的类加载器是否相等:" + (Context.class.getClassLoader() == ClassLoader.getSystemClassLoader()));
        Log.i("DEMO", "系统类加载器和应用程序默认加载器是否相等:" + (getClassLoader() == ClassLoader.getSystemClassLoader()));

        Log.i("DEMO", "打印应用程序默认加载器的委派机制:");
        ClassLoader classLoader = getClassLoader();
        while (classLoader != null) {
            Log.i("DEMO", "类加载器:" + classLoader);
            classLoader = classLoader.getParent();
        }

        Log.i("DEMO", "打印系统加载器的委派机制:");
        classLoader = ClassLoader.getSystemClassLoader();
        while (classLoader != null) {
            Log.i("DEMO", "类加载器:" + classLoader);
            classLoader = classLoader.getParent();
        }

    }
}
