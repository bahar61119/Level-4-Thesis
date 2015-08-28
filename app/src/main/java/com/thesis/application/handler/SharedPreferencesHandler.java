package com.thesis.application.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by bahar61119 on 8/28/2015.
 */
public class SharedPreferencesHandler {

    public static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setStringValues(Context context, String key, String value){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key,value);
        editor.commit();
    }

    public static String getStringValue(Context context, String key){
        return getSharedPreferences(context).getString(key,null);
    }
}
