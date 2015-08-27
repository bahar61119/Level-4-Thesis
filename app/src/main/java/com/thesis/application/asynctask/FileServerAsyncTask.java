package com.thesis.application.asynctask;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.thesis.application.ThesisActivity;
import com.thesis.application.fragments.DeviceDetailFragment;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Created by bahar61119 on 8/28/2015.
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private TextView statusText;

    public FileServerAsyncTask(Context context, View statusText){
        this.context = context;
        this.statusText = (TextView)statusText;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        statusText.setText("Opening a server socket");
    }

    @Override
    protected String doInBackground(Void... params) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8988);
            Log.d(ThesisActivity.TAG, "Server: Socket opened");
            Socket clientSocket = serverSocket.accept();
            Log.d(ThesisActivity.TAG, "Server: Connection Done");
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/thesisWork-" + System.currentTimeMillis() + ".jpg");

            File dirs = new File(f.getParent());
            if(!dirs.exists()) dirs.mkdirs();
            f.createNewFile();
            Log.d(ThesisActivity.TAG, "Server : Copying file "+f.toString());

            InputStream inputStream = clientSocket.getInputStream();
            DeviceDetailFragment.copyFile(inputStream, new FileOutputStream(f));

            Log.d(ThesisActivity.TAG, "Server : Copying done ");
            serverSocket.close();
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
            statusText.setText("File copied: "+ result);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://"+result), "image/*");
            context.startActivity(intent);
        }
    }
}
