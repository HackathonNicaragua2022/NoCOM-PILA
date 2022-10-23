package com.steward.nowpaid;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    public static String Id = "";
    public static boolean property = false;

    // account info
    public static String userID = "";
    public static String name = "";
    public static String image = "";
    public static String phone = "";
    public static int location = -1;

    public static void save(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("SessionSettings",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("SessionID",Id);
        editor.putBoolean("SessionProperty",property);
        editor.apply();
    }

    public static void load(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("SessionSettings",Context.MODE_PRIVATE);
        Id = prefs.getString("SessionID","");
        property = prefs.getBoolean("SessionProperty",false);
    }
}
