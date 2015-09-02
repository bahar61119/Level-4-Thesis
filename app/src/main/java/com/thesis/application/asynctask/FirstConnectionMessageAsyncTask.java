package com.thesis.application.asynctask;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.handler.GlobalApplication;
import com.thesis.application.handler.SharedPreferencesHandler;
import com.thesis.application.services.FileTransferService;
import com.thesis.application.services.WifiDirectClientIPTransferService;

import java.io.File;

/**
 * Created by bahar61119 on 8/29/2015.
 */
public class FirstConnectionMessageAsyncTask extends AsyncTask<String, Void, String> {

    Context context;
    String request;

    public FirstConnectionMessageAsyncTask(Context context, String request){
        this.context = context;
        this.request = request;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        // TODO Auto-generated method stub
        Log.e("On first Connect", "On first Connect");

        if(request.equalsIgnoreCase(FileTransferService.InetAddress)){
            String groupOwnerIP = SharedPreferencesHandler.getStringValue(context,"GroupOwnerAddress");
            Log.e(ThesisActivity.TAG, "On FC: GroupOWnerIP: "+groupOwnerIP);

            Intent serviceIntent = new Intent(context,
                    WifiDirectClientIPTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);


            if (groupOwnerIP != null) {

                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        groupOwnerIP);
                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                        FileTransferService.PORT);
                serviceIntent.putExtra(FileTransferService.InetAddress,
                        FileTransferService.InetAddress);

            }
            Log.d(ThesisActivity.TAG, "First connect Service Started");
            context.startService(serviceIntent);

        }else{
            String groupOwnerIP = SharedPreferencesHandler.getStringValue(context,"GroupOwnerAddress");
            Log.e(ThesisActivity.TAG, "On FC: GroupOWnerIP: "+groupOwnerIP);

            Intent serviceIntent = new Intent(context,
                    WifiDirectClientIPTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

            /////////////////////////////////////////////////////////////////////////

            String clientIP = SharedPreferencesHandler.getStringValue(context,"WifiClientIp");
            //String clientIP = ClientIP;
            Log.d(ThesisActivity.TAG, "Client IP SharedPre: "+ clientIP);

            String ownerIP = SharedPreferencesHandler.getStringValue(context, "GroupOwnerAddress");

            if(ownerIP != null && ownerIP.length()>0){

                String host = null;
                int subPort = -1;
                String serverBool = SharedPreferencesHandler.getStringValue(context, "ServerBoolean");

                if(serverBool != null && !serverBool.equals("") && serverBool.equalsIgnoreCase("true")){
                    if(clientIP != null && !clientIP.equals("")){
                        host = clientIP;
                        subPort = FileTransferService.PORT;
                        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, clientIP);
                        Log.d(ThesisActivity.TAG, "Client IP SErver: " + clientIP);
                    }
                }else{
                    host = ownerIP;
                    subPort = FileTransferService.PORT;
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, ownerIP);
                }

                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
                serviceIntent.putExtra(FileTransferService.InetAddress,request);
                if(host != null && subPort != -1){
                    Log.d(ThesisActivity.TAG, "First connect Service Started");
                    context.startService(serviceIntent);
                }else{
                    DeviceDetailFragment.dismissProgressDialog();
                    Toast.makeText(context, "Host address not found. Please Reeee-connect.", Toast.LENGTH_LONG).show();
                }


            }
        }

        /////////////////////////////////////////////////////////////////////////

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
