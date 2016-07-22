package com.cyg.learn.survival.wm.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cyg.learn.survival.R;

/**
 * Created by wm on 16/7/21.
 */
public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startOne(View view) {

        Intent intent = new Intent(this, DemonService.class);
        startService(intent);

    }

    public void startTwo(View view) {

        Intent intent = new Intent(this, ExistService.class);
        startService(intent);

    }

    public void startNormal(View view) {

        Intent intent = new Intent(this, CygService.class);
        startService(intent);

    }

}
