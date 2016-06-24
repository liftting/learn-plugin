package com.cyg.learn;

/**
 * Created by wm on 16/6/24.
 */
public class ScanResult {

    public String ssid;

    public String mac;

    public int level;

    public String toString() {
        return "ssid:" + ssid + ",mac:" + mac + ",level:" + level;
    }

}
