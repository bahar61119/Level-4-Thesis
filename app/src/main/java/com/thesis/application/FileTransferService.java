package com.thesis.application;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.thesis.application.fragments.DeviceDetailFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
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

            Socket clientSocket = new Socket();


            try {
                Log.d(ThesisActivity.TAG, "Opening Client Socket... ");
                clientSocket.bind(null);
                clientSocket.connect((new InetSocketAddress(host,port)), SOCKET_TIMEOUT);
                Log.d(ThesisActivity.TAG, "Client socket: " + clientSocket.isConnected());

                OutputStream outputStream = clientSocket.getOutputStream();
                ContentResolver contentResolver = context.getContentResolver();
                InputStream inputStream = null;

                try{
                    inputStream = contentResolver.openInputStream(Uri.parse(fileUri));
                }catch (FileNotFoundException e){
                    Log.e(ThesisActivity.TAG, e.getMessage());
                }

                DeviceDetailFragment.copyFile(inputStream,outputStream);
                Log.d(ThesisActivity.TAG, "Client: Data Written");

            } catch (IOException e) {
               Log.e(ThesisActivity.TAG, e.getMessage());
            } finally {
                if(clientSocket != null){
                    if(clientSocket.isConnected()){
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


        }
    }
}
