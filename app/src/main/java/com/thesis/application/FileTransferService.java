package com.thesis.application;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by bahar61119 on 8/28/2015.
 */
public class FileTransferService extends IntentService {

    public FileTransferService(String name){
        super(name);
    }

    public FileTransferService(){
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
