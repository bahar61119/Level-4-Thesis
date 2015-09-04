package com.thesis.application.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.handler.GlobalApplication;
import com.thesis.application.serializable.WiFiTransferModal;

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

        Context context = GlobalApplication.getGlobalAppContext();
        if (intent.getAction().equals(FileTransferService.ACTION_SEND_FILE)) {
            String host = intent.getExtras().getString(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS);
            String InetAddress =  intent.getExtras().getString(FileTransferService.InetAddress);
            Log.e("LocalIp Rece First COn:","host address"+ host);

            Socket socket = new Socket();
            int port = intent.getExtras().getInt(FileTransferService.EXTRAS_GROUP_OWNER_PORT);
            try {
                Log.d(ThesisActivity.TAG, "Opening client socket for First tiime- ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), FileTransferService.SOCKET_TIMEOUT);
                Log.d(ThesisActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();


               /*
                * Object that is used to send file name with extension and recieved on other side.
                */
                ObjectOutputStream oos = new ObjectOutputStream(stream);
                WiFiTransferModal transObj = new WiFiTransferModal(InetAddress);

                oos.writeObject(transObj);
                System.out.println("Sending request to Socket Server");

                oos.close();	//close the ObjectOutputStream after sending data.
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            Log.e("WiFiClientIP Service", "First Connection service socket closed");
                            socket.close();
                        } catch (Exception e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }


}
