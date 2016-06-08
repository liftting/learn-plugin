package com.cyg.learn;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by wm on 16/6/8.
 */
public class UiUtil {

    public static String getTextString(Context ctx) {
        return ctx.getResources().getString(R.string.demo_app_name);
    }

    public static Drawable getImageDrawable(Context ctx) {
        return ctx.getResources().getDrawable(R.drawable.ic_launcher);
    }

    public static View getLayout(Context ctx) {
        return LayoutInflater.from(ctx).inflate(R.layout.activity_main, null);
    }

    public static int getTextStringId() {
        return R.string.demo_app_name;
    }

    public static int getImageDrawableId() {
        return R.drawable.ic_launcher;
    }

    public static int getLayoutId() {
        return R.layout.activity_main;
    }

}
