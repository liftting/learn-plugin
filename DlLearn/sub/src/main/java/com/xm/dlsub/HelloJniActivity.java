package com.xm.dlsub;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.cyg.learn.jni.LearnJni;

/**
 * Created by wm on 16/6/27.
 */
public class HelloJniActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hello_jni_activity);

        LearnJni learnJni = new LearnJni();
        TextView tv = (TextView) findViewById(R.id.tv_name);
        int result = learnJni.actionArray(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        tv.setText(String.valueOf(result));

    }
}
