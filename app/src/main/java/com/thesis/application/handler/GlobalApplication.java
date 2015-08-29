package com.thesis.application.handler;

import android.content.Context;

/**
 * Created by bahar61119 on 8/30/2015.
 */
public class GlobalApplication extends android.app.Application{
    private static Context GlobalContext;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        if(GlobalApplication.GlobalContext == null){
            GlobalApplication.GlobalContext = getApplicationContext();
        }
    }

    public static Context getGlobalAppContext() {
        return GlobalApplication.GlobalContext;
    }
}
