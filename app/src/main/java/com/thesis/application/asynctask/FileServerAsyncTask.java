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
import android.widget.Toast;

import com.thesis.application.R;
import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.handler.FileInformation;
import com.thesis.application.handler.MethodHandler;
import com.thesis.application.handler.SharedPreferencesHandler;
import com.thesis.application.serializable.WiFiTransferModal;
import com.thesis.application.services.FileTransferService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Created by bahar61119 on 8/28/2015.
 */
public class FileServerAsyncTask extends AsyncTask<String, String, String> {

    private Context context;
    private String extension;
    private String key;
    private File encryptedFile;
    private Long receivedFileLength;
    private int port;
    public static Handler handler;
    private boolean isDataTransfer;
    private FileInformation info;


    public FileServerAsyncTask(Context context,int port){
        this.context = context;
        this.port = port;
        this.info = new FileInformation();
        this.handler = new Handler();
        if(DeviceDetailFragment.staticProgressDialog == null){
            DeviceDetailFragment.staticProgressDialog = new ProgressDialog(this.context, ProgressDialog.THEME_HOLO_LIGHT);
        }

    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
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
                isDataTransfer = transferObject.getIsDataTransfer();

                Log.v(ThesisActivity.TAG, "File Transfer InetAddress: "+ inetAddrss);

                if(inetAddrss != null && inetAddrss.equalsIgnoreCase(FileTransferService.InetAddress)){
                    SharedPreferencesHandler.setStringValues(context, "WifiClientIp", DeviceDetailFragment.ClientIP);
                    Log.d(ThesisActivity.TAG, "FileServerAsyncTask Client Ip: "+  DeviceDetailFragment.ClientIP);
                    Log.d(ThesisActivity.TAG, "SharedPer Client Ip: "+ SharedPreferencesHandler.getStringValue(context, "WifiClientIp"));
                    SharedPreferencesHandler.setStringValues(context, "ServerBoolean", "true");
                    objectInputStream.close();
                    serverSocket.close();
                    return "clientIPDetection";
                }else if(inetAddrss != null && inetAddrss.equalsIgnoreCase(FileTransferService.RequestInformationFile)){
                    objectInputStream.close();
                    serverSocket.close();
                    return FileTransferService.RequestInformationFile;
                }else if(!isDataTransfer){

                    this.receivedFileLength = transferObject.getFileLength();
                    InputStream inputStream = clientSocket.getInputStream();
                    OutputStream outputStream = new ByteArrayOutputStream();
                    DeviceDetailFragment.copyFile(inputStream, outputStream, receivedFileLength);

                    Log.d("Received:", "Information File ");

                    String jsonString = outputStream.toString();
                    Log.v("Information File: ",jsonString);
                    SharedPreferencesHandler.setStringValues(context, MethodHandler.FILEINFORMSTION, jsonString);
                    objectInputStream.close();
                    serverSocket.close();
                    return MethodHandler.FILEINFORMSTION;
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    DeviceDetailFragment.staticProgressDialog.setMessage("Receiving...");
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


            String jsonString = transferObject.getInfo();
            SharedPreferencesHandler.setStringValues(context, MethodHandler.CHUNKFILEINFORMATION, jsonString);
            Log.d("Received Chunk: ",jsonString);

            info = (FileInformation)MethodHandler.convertJsonStringToInfoObject(jsonString);

            int i = info.getFileName().lastIndexOf(".");
            String name = info.getFileName().substring(0,i);

            final File f = new File(MethodHandler.ChunkFilesDirectory+"/"+name+"/" + transferObject.getFileName());

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
            return null;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(s);

        if( result != null){
            if(!result.equalsIgnoreCase("clientIPDetection")){

                if(result.equalsIgnoreCase(MethodHandler.FILEINFORMSTION)){
                    ArrayList<FileInformation> receivedInformation = new ArrayList<>();
                    ArrayList<FileInformation> originalInformation = new ArrayList<>();

                    String jsonString = SharedPreferencesHandler.getStringValue(context,MethodHandler.FILEINFORMSTION);
                    Log.d("Received Info File: ",jsonString);
                    receivedInformation = (ArrayList<FileInformation>) MethodHandler.convertJsonStringToInfoObjectArray(jsonString);
                    originalInformation = MethodHandler.readInformationFile();
                    jsonString = MethodHandler.convertObjectToJsonString(MethodHandler.getChangeInformation(originalInformation,receivedInformation));
                    Log.d("Chunk File To Send: ",jsonString);

                    SharedPreferencesHandler.setStringValues(context, MethodHandler.CHUNKFILETOSEND, jsonString);

                    Toast.makeText(context,"Information File Received",Toast.LENGTH_LONG);

                    Log.v("Chunk File:","Send Starting...");
                    DeviceDetailFragment.sendchunkFilesSequencially(context);


                }else if(result.equalsIgnoreCase(FileTransferService.RequestInformationFile)){

                    Toast.makeText(context,"Received Information File Request",Toast.LENGTH_LONG);
                    Log.d("Received: ","Information File Request");
                    ArrayList<FileInformation> fileInformation = MethodHandler.readInformationFile();
                    String filePath = MethodHandler.InformationFilePath;
                    int i = filePath.lastIndexOf("/");
                    String fileName = filePath.substring(0, i);
                    String jsonString = MethodHandler.convertObjectToJsonString(fileInformation);
                    Long fileLength = Long.valueOf(jsonString.length());
                    Toast.makeText(context,"Information File Send",Toast.LENGTH_LONG);
                    Log.d("Information File Send: ", jsonString);
                    DeviceDetailFragment.sendData(context, filePath, fileName, fileLength, false, jsonString);

                }else{


                    ArrayList<FileInformation> fileInformation = new ArrayList<>();
                    fileInformation = MethodHandler.readInformationFile();
                    String jsonString = SharedPreferencesHandler.getStringValue(context, MethodHandler.CHUNKFILEINFORMATION);
                    FileInformation info = new FileInformation();
                    info = (FileInformation)MethodHandler.convertJsonStringToInfoObject(jsonString);

                    if(fileInformation == null){
                        fileInformation.add(info);
                    }else{
                        fileInformation = MethodHandler.updateReceivedChunk(fileInformation,info);
                    }
                    MethodHandler.writeInformationFile(fileInformation);

                    //Toast.makeText(context,"Chunk File Received",Toast.LENGTH_LONG);
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"Chunk File Received",Toast.LENGTH_LONG);
                        }
                    };
                    handler.post(r);
                    ///////////////////////////////////////////////////////////////////////////////////////////

                    fileInformation = MethodHandler.readInformationFile();
                    String filePath = MethodHandler.InformationFilePath;
                    int i = filePath.lastIndexOf("/");
                    String fileName = filePath.substring(0, i);
                    jsonString = MethodHandler.convertObjectToJsonString(fileInformation);
                    Long fileLength = Long.valueOf(jsonString.length());
                    Toast.makeText(context,"Information File Send",Toast.LENGTH_LONG);
                    Log.d("Information File Send: ", jsonString);
                    DeviceDetailFragment.sendData(context, filePath, fileName, fileLength, false, jsonString);

                    /////////////////////////////////////////////////////////////////////////////////////////////



                }

                //Intent intent = new Intent();
                //intent.setAction(android.content.Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://"+result), "image/*");
                //context.startActivity(intent);
            }

            DeviceDetailFragment.runServer(context);
        }


    }
}
