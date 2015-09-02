package com.thesis.application.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.handler.FileInformation;
import com.thesis.application.handler.GlobalApplication;
import com.thesis.application.handler.MethodHandler;

import java.io.File;

/**
 * Created by bahar61119 on 9/2/2015.
 */
public class FileSendAsyncTask extends AsyncTask<String, String, String> {

    public Context context;
    public static Handler handler;
    public FileInformation info;

    public FileSendAsyncTask(Context context, FileInformation info){
        this.context = context;
        this.handler = new Handler();
        this.info = new FileInformation();
        this.info = info;

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


        final Runnable r = new Runnable() {
            @Override
            public void run() {
                DeviceDetailFragment.staticProgressDialog.setMessage("Sending ...");
                DeviceDetailFragment.staticProgressDialog.setIndeterminate(false);
                DeviceDetailFragment.staticProgressDialog.setMax(100);
                DeviceDetailFragment.staticProgressDialog.setProgress(0);
                DeviceDetailFragment.staticProgressDialog.setProgressNumberFormat(null);
                DeviceDetailFragment.staticProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                DeviceDetailFragment.staticProgressDialog.show();
            }
        };
        handler.post(r);

            Log.d("File Chunk: ", info.toString());

            String filePath = MethodHandler.ChunkFilesDirectory;
            int j = info.getFileName().lastIndexOf(".");
            String name = info.getFileName().substring(0,j);
            filePath+="/"+name+"/";
            String fileName = info.getFileName()+".part"+info.getChunkList().get(0);
            filePath+= fileName;

            File f = new File(filePath);
            Long fileLength = Long.valueOf(f.length());
            boolean isDataTransfer = true;
            Log.d("Sending: ", filePath);
            String jsonString = MethodHandler.convertObjectToJsonString(info);
            DeviceDetailFragment.sendData(context, filePath, fileName, fileLength, isDataTransfer, jsonString);
            Log.d("Send Data:", "Done");

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(result);
        DeviceDetailFragment.dismissProgressDialog();
    }
}