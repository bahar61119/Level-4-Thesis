package com.thesis.application.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.handler.FileInformation;
import com.thesis.application.handler.MethodHandler;
import com.thesis.application.services.FileTransferService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by bahar61119 on 9/5/2015.
 */
public class FileCopyAsyncTask extends AsyncTask<String, String, String> {

    public Context context;
    public static Handler handler;
    public String filePath;

    public FileCopyAsyncTask(Context context, String filePath){
        this.context = context;
        this.handler = new Handler();
        this.filePath = filePath;

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

        File inputFile = new File(filePath);
        Log.d(ThesisActivity.TAG,"File Path: "+ filePath);
        FileInputStream inputStream;
        FileOutputStream outputStream;
        int fileLength = (int)inputFile.length();
        DeviceDetailFragment.ActualFileLength = fileLength;
        String fileName = inputFile.getName();

        //byte[] byteChunkPart;
        //byteChunkPart = new byte[fileLength];

        try {
            inputStream = new FileInputStream(inputFile);
            //inputStream.read(byteChunkPart, 0, fileLength);

            String filePath = Environment.getExternalStorageDirectory() + "/thesis/files/"+fileName;
            Log.d("File Name:",fileName);

            File f = new File(filePath);
            File dirs = new File(f.getParent());
            if(!dirs.exists()) dirs.mkdirs();
            MethodHandler.createFolder(f.getParent());
            f.createNewFile();

            outputStream = new FileOutputStream(f);
            DeviceDetailFragment.copyFile(inputStream,outputStream,DeviceDetailFragment.ActualFileLength);
            Log.d("File Copied","Location: thesis/files");


            outputStream.flush();
            outputStream.close();
            inputStream.close();
            //byteChunkPart = null;
            outputStream = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

            if(DeviceDetailFragment.staticProgressDialog != null){
                if(DeviceDetailFragment.staticProgressDialog.isShowing()){
                    DeviceDetailFragment.staticProgressDialog.dismiss();
                }
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        //super.onPostExecute(result);
        DeviceDetailFragment.dismissProgressDialog();
    }
}
