package com.thesis.application.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.thesis.application.activities.ThesisActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WifiDirectClientIPTransferService extends IntentService {


    Handler handler;


    public WifiDirectClientIPTransferService(String name){
        super(name);
    }

    public WifiDirectClientIPTransferService() {
        super("WifiDirectClientIPTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        if(intent.getAction().equals(FileTransferService.ACTION_SEND_FILE)){
            String host = intent.getExtras().getString(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS);
            String inetAddress = intent.getExtras().getString(FileTransferService.InetAddress);
            int port = intent.getExtras().getInt(FileTransferService.EXTRAS_GROUP_OWNER_PORT);

            Socket socket = new Socket();


            try {
                Log.d(ThesisActivity.TAG, "Opening client socket first time - ");

                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), FileTransferService.SOCKET_TIMEOUT);

                Log.d(ThesisActivity.TAG, "Client socket - " + socket.isConnected());

                OutputStream outputStream = socket.getOutputStream();
                ContentResolver contentresolver = context.getContentResolver();
                InputStream inputStream = null;


                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                WifiDirectClientIPTransferService transferObject = new WifiDirectClientIPTransferService(inetAddress);

                objectOutputStream.writeObject(transferObject);

                objectOutputStream.close();

            } catch (IOException e) {
                Log.e(ThesisActivity.TAG, e.getMessage());
            }finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


}
