package com.thesis.application.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.thesis.application.R;
import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.handler.SharedPreferencesHandler;
import com.thesis.application.serializable.WiFiTransferModal;
import com.thesis.application.services.FileTransferService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Created by bahar61119 on 8/28/2015.
 */
public class FileServerAsyncTask extends AsyncTask<String, String, String> {

    private Context context;
    private TextView statusText;
    private String extension;
    private String key;
    private File encryptedFile;
    private Long receivedFileLength;
    private int port;
    public static Handler handler;


    public FileServerAsyncTask(Context context, View statusText, int port){
        this.context = context;
        this.statusText = (TextView)statusText;
        this.port = port;
        this.handler = new Handler();
        if(DeviceDetailFragment.staticProgressDialog == null){
            DeviceDetailFragment.staticProgressDialog = new ProgressDialog(this.context, ProgressDialog.THEME_HOLO_LIGHT);
        }

    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        statusText.setText("Opening a server socket");
        if(DeviceDetailFragment.staticProgressDialog == null){
            DeviceDetailFragment.staticProgressDialog = new ProgressDialog(this.context, ProgressDialog.THEME_HOLO_LIGHT);
        }
    }

    @Override
    protected String doInBackground(String... params) {


        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Log.d(ThesisActivity.TAG, "Server: Socket opened");

            Socket clientSocket = serverSocket.accept();
            Log.d(ThesisActivity.TAG, "Server: Connection Done");

            DeviceDetailFragment.ClientIP = clientSocket.getInetAddress().getHostAddress();


            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            WiFiTransferModal transferObject  = null;
            String inetAddrss;

            try {
                transferObject = (WiFiTransferModal) objectInputStream.readObject();

                Log.d(ThesisActivity.TAG, "Received File Len: "+ transferObject.getFileName());
                inetAddrss = transferObject.getInetAddress();
                Log.v(ThesisActivity.TAG, "File Transfer InetAddress: "+ inetAddrss);

                if(inetAddrss != null && inetAddrss.equalsIgnoreCase(FileTransferService.InetAddress)){
                    SharedPreferencesHandler.setStringValues(context, "WifiClientIp", DeviceDetailFragment.ClientIP);
                    Log.d(ThesisActivity.TAG, "FileServerAsyncTask Client Ip: "+  DeviceDetailFragment.ClientIP);
                    Log.d(ThesisActivity.TAG, "SharedPer Client Ip: "+ SharedPreferencesHandler.getStringValue(context, "WifiClientIp"));
                    SharedPreferencesHandler.setStringValues(context, "ServerBoolean", "true");
                    objectInputStream.close();
                    serverSocket.close();
                    return "clientIPDetection";
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    DeviceDetailFragment.staticProgressDialog.setMessage("Reveiving...");
                    DeviceDetailFragment.staticProgressDialog.setIndeterminate(false);
                    DeviceDetailFragment.staticProgressDialog.setMax(100);
                    DeviceDetailFragment.staticProgressDialog.setProgress(0);
                    DeviceDetailFragment.staticProgressDialog.setProgressNumberFormat(null);
                    DeviceDetailFragment.staticProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    DeviceDetailFragment.staticProgressDialog.show();
                }
            };
            handler.post(r);


            ////////////////////////////////////////////////////////////////////////////////////////
            final File f = new File(Environment.getExternalStorageDirectory() + "/thesis/"
                    + context.getPackageName() + "-thesisWork:"+System.currentTimeMillis()+"-" + transferObject.getFileName());

            File dirs = new File(f.getParent());
            if(!dirs.exists()) dirs.mkdirs();
            f.createNewFile();

            Log.d(ThesisActivity.TAG, "Server : Copying file "+f.toString());

            this.receivedFileLength = transferObject.getFileLength();
            InputStream inputStream = clientSocket.getInputStream();
            DeviceDetailFragment.copyFile(inputStream, new FileOutputStream(f), receivedFileLength);

            Log.d(ThesisActivity.TAG, "Server : Copying done ");

            objectInputStream.close();
            serverSocket.close();

            this.extension = transferObject.getFileName();
            this.encryptedFile = f;

            return f.getAbsolutePath();

        } catch (IOException e) {
            Log.e(ThesisActivity.TAG, e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(s);

        if( result != null){
            if(!result.equalsIgnoreCase("clientIPDetection")){
                statusText.setText("File copied: "+ result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://"+result), "image/*");
                context.startActivity(intent);
            }
            else{
                FileServerAsyncTask fileServerAsyncTask = new FileServerAsyncTask(context, statusText, FileTransferService.PORT);
                if(fileServerAsyncTask!= null){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                        fileServerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{ null});
                    }else{
                        fileServerAsyncTask.execute();
                    }
                }
            }
        }
    }
}
