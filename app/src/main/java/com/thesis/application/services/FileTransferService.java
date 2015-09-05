package com.thesis.application.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.asynctask.FirstConnectionMessageAsyncTask;
import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.handler.FileInformation;
import com.thesis.application.handler.MethodHandler;
import com.thesis.application.handler.SharedPreferencesHandler;
import com.thesis.application.serializable.WiFiTransferModal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FileTransferService extends IntentService {

    public static Handler handler;

    public static final int SOCKET_TIMEOUT = 10000;
    public static final String ACTION_SEND_FILE = "com.thesis.application.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static int PORT = 8988;
    public static String InetAddress = "inetaddress";
    public static final String RequestInformationFile = "requestinformationfile";
    public static final int ByteSize = 128;
    public static final String DATAINFORMATION = "data";
    public static final String IsDataTranser = "isdatatransfer";
    public static final String Extension = "extension";
    public static final String FileLength = "filelength";
    public static final String ChunkSend = "chunksend";



    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        SharedPreferencesHandler.setStringValues(context,MethodHandler.IsFileSend, "false");

        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            boolean isDataTransfer = intent.getExtras().getBoolean(IsDataTranser);
            String extension = intent.getExtras().getString(Extension);
            String fileLengthExtra = intent.getExtras().getString(FileLength);
            String jsonString = null;
            FileInformation info = null;
            ArrayList<FileInformation> fileInformation = new ArrayList<>();

            if(isDataTransfer){
                jsonString = intent.getExtras().getString(DATAINFORMATION);

                try {
                    Log.d(ThesisActivity.TAG, "Opening client socket - ");

                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                    Log.d(ThesisActivity.TAG, "Client socket - " + socket.isConnected());

                    OutputStream stream = socket.getOutputStream();
                    //ContentResolver cr = context.getContentResolver();
                    InputStream is = null;
                    Long fileLength = Long.parseLong(fileLengthExtra);
                    WiFiTransferModal transferObject = null;
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);

                    if(transferObject == null) transferObject = new WiFiTransferModal();
                    transferObject = new WiFiTransferModal(extension,fileLength,isDataTransfer, jsonString);
                    objectOutputStream.reset();
                    objectOutputStream.writeObject(transferObject);

                    try {
                        Log.d("Chunk File Path: ", fileUri);
                        File f = new File(fileUri);
                        if(f.exists()) Log.d("File found: ",f.getName());
                        FileInputStream fis = new FileInputStream(f);
                        byte[] fileBytes = new byte[(int) f.length()];
                        fis.read(fileBytes, 0,(int)  f.length());
                        is = new ByteArrayInputStream(fileBytes);
                        //is = cr.openInputStream(Uri.parse(fileUri));
                    } catch (FileNotFoundException e) {
                        Log.e("Why man why? ", e.toString());
                    }

                    DeviceDetailFragment.copyFile(is, stream,fileLength);
                    Log.d(ThesisActivity.TAG, "SerializationFile Name: " + fileLength);
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
                                Log.e("File Transfer Service", "Connection service socket closed");
                                socket.close();
                            } catch (Exception e) {
                                // Give up
                                e.printStackTrace();
                            }
                        }
                    }
                }


            }else{
                jsonString = intent.getExtras().getString(DATAINFORMATION);
                fileInformation = (ArrayList<FileInformation>)MethodHandler.convertJsonStringToInfoObjectArray(jsonString);

                try {
                    Log.d(ThesisActivity.TAG, "Opening client socket - ");

                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                    Log.d(ThesisActivity.TAG, "Client socket - " + socket.isConnected());

                    InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

                    OutputStream stream = socket.getOutputStream();
                    WiFiTransferModal transferObject = null;
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);

                    if(transferObject == null) transferObject = new WiFiTransferModal();

                    transferObject = new WiFiTransferModal(isDataTransfer);
                    transferObject.setFileLength((long) jsonString.length());
                    objectOutputStream.reset();
                    Log.d("File Info Tran Object: ", "Init");
                    objectOutputStream.writeObject(transferObject);
                    Log.d("File Info TranObject: ", "Written");

                    DeviceDetailFragment.copyFile(inputStream, stream, (long) jsonString.length());

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
        SharedPreferencesHandler.setStringValues(context, MethodHandler.IsFileSend, "true");

    }
}
