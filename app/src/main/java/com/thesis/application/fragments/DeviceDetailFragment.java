package com.thesis.application.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.thesis.application.asynctask.FirstConnectionMessageAsyncTask;
import com.thesis.application.handler.MethodHandler;
import com.thesis.application.handler.SharedPreferencesHandler;
import com.thesis.application.services.FileTransferService;
import com.thesis.application.R;
import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.asynctask.FileServerAsyncTask;
import com.thesis.application.interfaces.DeviceActionListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by bahar61119 on 7/15/2015.
 */
public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    ProgressDialog progressDialog = null;
    public static ProgressDialog staticProgressDialog;
    private View contentView = null;
    private WifiP2pDevice device = null;
    private WifiP2pInfo info;
    public static String ClientIP;
    public static boolean ClientCheck;
    public static String GroupOwnerAddress="";
    public static long ActualFileLength = 0;
    public static int Percentage = 0;
    public static String Foldername = "ThesisWork";




    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.device_detail, null);

        contentView.findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "Conneting to " + device.deviceAddress, true, true);
                ((DeviceActionListener)getActivity()).connect(config);
            }
        });

        contentView.findViewById(R.id.btnDisconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceActionListener)getActivity()).disconnect();
            }
        });

        contentView.findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),"Not Implemented",Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE );
            }
        });

        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        Uri uri = data.getData();
        String selectedFilePath = uri.getPath();
        String extension = "";

        if(selectedFilePath != null){
            try {
                //URI path = new URI(selectedFilePath);
                //File f = new File(path);
                //Log.d(ThesisActivity.TAG,"File Path: "+ path);

                selectedFilePath = MethodHandler.getPath(uri,getActivity());
                File f = new File(selectedFilePath);
                Log.d(ThesisActivity.TAG,"File Path: "+ selectedFilePath);

                Long fileLength = f.length();
                ActualFileLength = fileLength;
                extension = f.getName();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        TextView statusText = (TextView) contentView.findViewById(R.id.tvStatusText);
        statusText.setText("Sending: "+ uri);
        Log.d(ThesisActivity.TAG, "Intent--------" +uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());

        //////////////////////////////////////////////////////////////////////////////////////////////

        String clientIP = SharedPreferencesHandler.getStringValue(getActivity(),"WifiClientIP");
        String ownerIP = SharedPreferencesHandler.getStringValue(getActivity(), "GroupOwnerAddress");

        if(ownerIP != null && ownerIP.length()>0){

            String host = null;
            int subPort = -1;
            String serverBool = SharedPreferencesHandler.getStringValue(getActivity(), "ServerBoolean");

            if(serverBool != null && !serverBool.equals("") && serverBool.equalsIgnoreCase("true")){
                if(clientIP != null && !clientIP.equals("")){
                    host = clientIP;
                    subPort = FileTransferService.PORT;
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, clientIP);
                }
            }else{
                host = ownerIP;
                subPort = FileTransferService.PORT;
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, ownerIP);
            }

            serviceIntent.putExtra(FileTransferService.Extension, extension);
            serviceIntent.putExtra(FileTransferService.FileLength, ActualFileLength+"");
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
            if(host != null && subPort != -1){
                showProgress("Sending...");
                getActivity().startService(serviceIntent);
            }else{
                dismissProgressDialog();
                Toast.makeText(getActivity(),"Host address not found. Please Re-connect.", Toast.LENGTH_LONG).show();
            }


        }else{
            dismissProgressDialog();
            Toast.makeText(getActivity(),"Host address not found. Please Re-connect.", Toast.LENGTH_LONG).show();
        }

        //////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        TextView textView = (TextView) contentView.findViewById(R.id.tvGroupOwner);
        textView.setText(getResources().getString(R.string.group_owner_text)+ ((info.isGroupOwner==true)? getResources().getString(R.string.yes):getResources().getString(R.string.no)));

        textView = (TextView) contentView.findViewById(R.id.tvGroupIp);
        textView.setText("Group Owner IP: "+ info.groupOwnerAddress.getHostAddress());

        String groupOwner = info.groupOwnerAddress.getHostAddress();

        if(groupOwner != null && !groupOwner.equals("")) SharedPreferencesHandler.setStringValues(getActivity(), "GroupOwnerAddress", groupOwner);
        contentView.findViewById(R.id.btnGallery).setVisibility(View.VISIBLE);

        if(info.groupFormed && info.isGroupOwner){

            SharedPreferencesHandler.setStringValues(getActivity(),"ServerBoolean", "true");

            FileServerAsyncTask fileServerAsyncTask = new FileServerAsyncTask(getActivity(), contentView.findViewById(R.id.tvStatusText), FileTransferService.PORT);
            if(fileServerAsyncTask!= null){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    fileServerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{ null});
                }else{
                    fileServerAsyncTask.execute();
                }
            }

        }else if(info.groupFormed){
            ((TextView)contentView.findViewById(R.id.tvStatusText)).setText(getResources().getString(R.string.client_text));

            if(!ClientCheck){
                FirstConnectionMessageAsyncTask firstConnectionMessageAsyncTask = new FirstConnectionMessageAsyncTask(getActivity());
                if(firstConnectionMessageAsyncTask != null){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                        firstConnectionMessageAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});
                    }else{
                        firstConnectionMessageAsyncTask.execute();
                    }
                }
            }

            FileServerAsyncTask fileServerAsyncTask = new FileServerAsyncTask(getActivity(), contentView.findViewById(R.id.tvStatusText), FileTransferService.PORT);
            if(fileServerAsyncTask!= null){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    fileServerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{ null});
                }else{
                    fileServerAsyncTask.execute();
                }
            }

        }

        contentView.findViewById(R.id.btnConnect).setVisibility(View.GONE);
    }

    public void showDetails(WifiP2pDevice device){
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView textView = (TextView) contentView.findViewById(R.id.tvPeerDeviceAddress);
        textView.setText(device.deviceAddress);
        textView = (TextView) contentView.findViewById(R.id.tvPeerDeviceInfo);
        textView.setText(device.toString());
    }

    public void resetViews(){
        contentView.findViewById(R.id.btnConnect).setVisibility(View.VISIBLE);
        TextView view = (TextView)contentView.findViewById(R.id.tvPeerDeviceAddress);
        view.setText("");
        view = (TextView) contentView.findViewById(R.id.tvPeerDeviceInfo);
        view.setText("");
        view = (TextView) contentView.findViewById(R.id.tvGroupOwner);
        view.setText("");
        view = (TextView) contentView.findViewById(R.id.tvGroupIp);
        view.setText("");
        view = (TextView) contentView.findViewById(R.id.tvStatusText);
        view.setText("");
        contentView.findViewById(R.id.btnGallery).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);

        SharedPreferencesHandler.setStringValues(getActivity(),"GroupOwnerAddress", "");
        SharedPreferencesHandler.setStringValues(getActivity(),"WiFiClientIp","");
        SharedPreferencesHandler.setStringValues(getActivity(),"ServerBoolean","");
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out, Long length) {
        byte buf[] = new byte[FileTransferService.ByteSize];
        int len;
        long total = 0;
        long startTime=System.currentTimeMillis();

        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }

            try {
                total += len;

                if(length>0){
                    Percentage = (int) ((total*100)/ length);
                }

                staticProgressDialog.setProgress(Percentage);

            }catch (Exception e){
                e.printStackTrace();
                Percentage = 0;
                ActualFileLength = 0;
            }

            dismissProgressDialog();

            out.close();
            inputStream.close();
            long endTime=System.currentTimeMillis()-startTime;
            Log.v("","Time taken to transfer all bytes is : "+endTime);

        } catch (IOException e) {
            Log.d(ThesisActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    public void showProgress(final String task) {
        if (staticProgressDialog == null) {
            staticProgressDialog = new ProgressDialog(getActivity(),
                    ProgressDialog.THEME_HOLO_LIGHT);
        }
        Handler handle = new Handler();
        final Runnable s = new Runnable() {

            public void run() {
                staticProgressDialog.setMessage(task);
                staticProgressDialog.setIndeterminate(false);
                staticProgressDialog.setMax(100);
                staticProgressDialog.setProgressNumberFormat(null);
                staticProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                staticProgressDialog.show();
            }
        };
        handle.post(s);
    }



    public static void dismissProgressDialog(){
        try{
            if(staticProgressDialog != null){
                if(staticProgressDialog.isShowing()){
                    staticProgressDialog.dismiss();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
