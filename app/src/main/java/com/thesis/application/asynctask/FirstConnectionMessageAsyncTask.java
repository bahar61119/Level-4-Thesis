package com.thesis.application.asynctask;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.handler.SharedPreferencesHandler;
import com.thesis.application.services.FileTransferService;
import com.thesis.application.services.WifiDirectClientIPTransferService;

import java.io.File;

/**
 * Created by bahar61119 on 8/29/2015.
 */
public class FirstConnectionMessageAsyncTask extends AsyncTask<String, Void, String> {

    Context context;

    public FirstConnectionMessageAsyncTask(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        Intent serviceIntent = new Intent(context, WifiDirectClientIPTransferService.class);

        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        String groupOwnerAddres = SharedPreferencesHandler.getStringValue(context, "GroupOwnerAddress");

        if(groupOwnerAddres != null){
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, groupOwnerAddres);
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
            serviceIntent.putExtra(FileTransferService.InetAddress, FileTransferService.InetAddress);
            context.startService(serviceIntent);
            return "success";
        }


        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null){
            if(result.equalsIgnoreCase("success")){
                DeviceDetailFragment.ClientCheck = true;
            }
        }
    }
}
