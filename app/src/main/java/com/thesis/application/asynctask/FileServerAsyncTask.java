package com.thesis.application.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.thesis.application.ThesisActivity;

import org.w3c.dom.Text;

import java.io.IOException;
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
    protected String doInBackground(Void... params) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8988);
            Log.d(ThesisActivity.TAG, "Server: Socket opened");
            Socket clientSocket = serverSocket.accept();
            Log.d(ThesisActivity.TAG, "Server: Connection Done");

        } catch (IOException e) {
            Log.e(ThesisActivity.TAG, e.getMessage());
        }

        return null;
    }
}
