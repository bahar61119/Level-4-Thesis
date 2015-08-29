package com.thesis.application.asynctask;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.thesis.application.activities.ThesisActivity;
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
        // TODO Auto-generated method stub
        Log.e("On first Connect", "On first Connect");

        String groupOwnerIP = SharedPreferencesHandler.getStringValue(context,"GroupOwnerAddress");
        Log.e(ThesisActivity.TAG, "On FC: GroupOWnerIP: "+groupOwnerIP);

        Intent serviceIntent = new Intent(context,
                WifiDirectClientIPTransferService.class);
        Log.d(ThesisActivity.TAG, "service configure 1");
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

        Log.d(ThesisActivity.TAG, "service configure 2");

        if (groupOwnerIP != null) {

            Log.d(ThesisActivity.TAG, "service configure 3");
            serviceIntent.putExtra(
                    FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    groupOwnerIP);
            Log.d(ThesisActivity.TAG, "service configure 4");
            serviceIntent.putExtra(
                    FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                    FileTransferService.PORT);
            Log.d(ThesisActivity.TAG, "service configure 5");
            serviceIntent.putExtra(FileTransferService.InetAddress,
                    FileTransferService.InetAddress);
            Log.d(ThesisActivity.TAG, "service configure 6");

        }
        Log.d(ThesisActivity.TAG, "First connect Service Started");
        context.startService(serviceIntent);

        return "success";
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        if(result!=null){
            if(result.equalsIgnoreCase("success")){
                Log.e("On first Connect",
                        "On first Connect sent to asynctask");
                DeviceDetailFragment.ClientCheck = true;
            }
        }

    }
}
