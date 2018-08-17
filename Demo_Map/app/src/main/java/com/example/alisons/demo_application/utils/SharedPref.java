package com.example.alisons.demo_application.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private static final String PACKAGE_NAME = "com.example.alisons.demo_application.utils";
    private static String MAP_TYPE_KEY = "map_type";
    private static String MAP_TRAFFIC_KEY = "traffic";


    public static void setMapTypeSatellite(Context context, boolean yes) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(MAP_TYPE_KEY, yes).apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean getMapTypeSatellite(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(MAP_TYPE_KEY, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void setTraffic(Context context, boolean yes) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(MAP_TRAFFIC_KEY, yes).apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean getTraffic(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
            return prefs.getBoolean(MAP_TRAFFIC_KEY, false);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
