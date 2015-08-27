package com.thesis.application;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import java.net.Socket;

/**
 * Created by bahar61119 on 8/28/2015.
 */
public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.thesis.application.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name){
        super(name);
    }

    public FileTransferService(){
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        if(intent.getAction().equals(ACTION_SEND_FILE)){
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            Socket clintSocket = new Socket();

        }
    }
}
