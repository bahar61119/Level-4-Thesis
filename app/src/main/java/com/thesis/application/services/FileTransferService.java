package com.thesis.application.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.serializable.WifiTransferSerializable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FileTransferService extends IntentService {

    Handler handler;

    public static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.thesis.application.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static int PORT = 8988;
    public static String InetAddress = "inetaddress";
    public static final int ByteSize = 512;
    public static final String Extension = "extension";
    public static final String FileLength = "filelength";


    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            String extension = intent.getExtras().getString(Extension);
            String fileLengthExtra = intent.getExtras().getString(FileLength);

            try {
                Log.d(ThesisActivity.TAG, "Opening client socket - ");

                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(ThesisActivity.TAG, "Client socket - " + socket.isConnected());

                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                Long fileLength = Long.parseLong(fileLengthExtra);
                WifiTransferSerializable transferObject = null;
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);

                if(transferObject == null) transferObject = new WifiTransferSerializable();
                transferObject = new WifiTransferSerializable(extension,fileLength);
                objectOutputStream.writeObject(transferObject);

                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(ThesisActivity.TAG, e.toString());
                }

                DeviceDetailFragment.copyFile(is, stream,fileLength);
                Log.d(ThesisActivity.TAG, "Client: Data written");

                objectOutputStream.close();

            } catch (IOException e) {
                Log.e(ThesisActivity.TAG, e.getMessage());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FileTransferService.this, "Paired deivec is not ready to receive the file", Toast.LENGTH_LONG).show();
                    }
                });

                DeviceDetailFragment.dismissProgressDialog();

            } finally {
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
