package com.cyg.learn;

import android.content.pm.ApplicationInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.logging.Logger;


public class TestActivity extends ActionBarActivity {

    private TextView tvName;

    private String TAG = "cyg_win";

    private HelloJni helloJni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        helloJni = new HelloJni();

        tvName = (TextView) findViewById(R.id.tv_name);

        ApplicationInfo appInfo = getApplicationInfo();

        Log.d(TAG, "appinfo dataDir:" + appInfo.dataDir);
        Log.d(TAG, "appinfo sourceDir:" + appInfo.sourceDir);
        Log.d(TAG, "appinfo publicSourceDir:" + appInfo.publicSourceDir);


    }

    public void load1(View view) {

        String name = helloJni.getName("aa");

        tvName.setText(name);

    }

    public void load2(View view) {
        String[] array = helloJni.getArray();
        StringBuilder sb = new StringBuilder();

        for (String s : array) {
            sb.append(s).append("\n");
        }

        tvName.setText(sb.toString());

    }


    public void load4(View view) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Student> stuArray = helloJni.getArrayList();
        for (Student stu : stuArray) {
            sb.append(stu.toString()).append("\n");
        }
        tvName.setText(sb.toString());
    }

    public void load3(View view) {
        ScanResult scanResult = helloJni.getResult();
        tvName.setText(scanResult.toString());
    }
}