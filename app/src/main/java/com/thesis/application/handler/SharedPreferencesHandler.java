package com.thesis.application.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by bahar61119 on 8/28/2015.
 */
public class SharedPreferencesHandler {

    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setStringValues(Context ctx, String key,
                                       String DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(key, DataToSave);
        editor.commit();
    }

    public static String getStringValue(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, null);
    }

    public static void setIntValues(Context ctx, String key, int DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(key, DataToSave);
        editor.commit();
    }

    public static int getIntValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getInt(key, 0);
    }

    public static void setBooleanValues(Context ctx, String key, Boolean DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(key, DataToSave);
        editor.commit();
    }

    public static boolean getBooleanValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getBoolean(key, false);
    }

    public static long getLongValues(Context ctx, String key) {
        return getSharedPreferences(ctx).getLong(key, 0L);
    }

    public static void setLongValues(Context ctx, String key, Long DataToSave) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putLong(key, DataToSave);
        editor.commit();
    }
}
