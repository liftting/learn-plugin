package com.cyg.learn;

import android.content.pm.ApplicationInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.logging.Logger;


public class TestActivity extends ActionBarActivity {

    private TextView tvName;

    private String TAG = "cyg_win";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvName = (TextView) findViewById(R.id.tv_name);

        HelloJni helloJni = new HelloJni();
        String name = helloJni.getName("name");

        tvName.setText(name);

        ApplicationInfo appInfo = getApplicationInfo();

        Log.d(TAG,"appinfo dataDir:" + appInfo.dataDir);
        Log.d(TAG,"appinfo sourceDir:" + appInfo.sourceDir);
        Log.d(TAG,"appinfo publicSourceDir:" + appInfo.publicSourceDir);


    }

}