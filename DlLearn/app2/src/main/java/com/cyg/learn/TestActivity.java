package com.cyg.learn;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class TestActivity extends ActionBarActivity {

    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvName = (TextView) findViewById(R.id.tv_name);

        HelloJni helloJni = new HelloJni();
        String name = helloJni.getName("name");

        tvName.setText(name);


    }

}