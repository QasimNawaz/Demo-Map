package com.example.alisons.demo_application.utils;

import android.util.Log;

import com.example.alisons.demo_application.BuildConfig;

public class AppLogs {


    public static void loge(String msg, String tag) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg + " -> " + Thread.currentThread().getStackTrace()[3].getFileName());
        } else {
            Log.e(tag, msg + " ");
        }
    }

    public static void logd(String msg, String tag) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg + " -> " + Thread.currentThread().getStackTrace()[3].getFileName());
        } else {
            Log.e(tag, msg + " ");
        }
    }

    public static void logw(String msg, String tag) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg + " -> " + Thread.currentThread().getStackTrace()[3].getFileName());
        } else {
            Log.e(tag, msg + " ");
        }
    }

    public static void logi(String msg, String tag) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg + " -> " + Thread.currentThread().getStackTrace()[3].getFileName());
        } else {
            Log.e(tag, msg + " ");
        }
    }

    /*

    public String lineOut() {
        int level = 3;
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        return (" at " + traces[level] + " ");
    }

    */
}
