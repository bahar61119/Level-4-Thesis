package com.thesis.application.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.thesis.application.asynctask.FileCopyAsyncTask;
import com.thesis.application.asynctask.FileSendAsyncTask;
import com.thesis.application.asynctask.FirstConnectionMessageAsyncTask;
import com.thesis.application.handler.FileInformation;
import com.thesis.application.handler.GlobalApplication;
import com.thesis.application.handler.MethodHandler;
import com.thesis.application.handler.SharedPreferencesHandler;
import com.thesis.application.services.FileTransferService;
import com.thesis.application.R;
import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.asynctask.FileServerAsyncTask;
import com.thesis.application.interfaces.DeviceActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by bahar61119 on 7/15/2015.
 */
public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    ProgressDialog progressDialog = null;
    public static ProgressDialog staticProgressDialog;
    private static View contentView = null;
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
                contentView.findViewById(R.id.btnDisconnect).setVisibility(View.GONE);
            }
        });

        contentView.findViewById(R.id.btnGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(),"Not Implemented",Toast.LENGTH_LONG).show();

                ///Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                ///intent.setType("image/*");
                ///startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE );

                FirstConnectionMessageAsyncTask firstObj = new FirstConnectionMessageAsyncTask(getActivity(), FileTransferService.RequestInformationFile);
                if (firstObj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        firstObj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[] { null });
                    } else
                        firstObj.execute();
                }

            }
        });

        contentView.findViewById(R.id.btnSelectSeedFile).setOnClickListener(new View.OnClickListener() {
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

    public static void sendchunkFilesSequencially(Context context){

        String jsonString = SharedPreferencesHandler.getStringValue(context, MethodHandler.CHUNKFILETOSEND);

        if(jsonString == null){
            Toast.makeText(GlobalApplication.getGlobalAppContext(),"No New Files To Send",Toast.LENGTH_LONG).show();
        }
        else{
            ArrayList<FileInformation> fileInformation = new ArrayList<>();

            fileInformation = (ArrayList<FileInformation>)MethodHandler.convertJsonStringToInfoObjectArray(jsonString);


            ArrayList<FileInformation> infoList = new ArrayList<>();
            int k=0;
            for(int i=0;i<fileInformation.size();i++){
                for(int j=0; j< fileInformation.get(i).getChunkListSize();j++){
                    FileInformation info = new FileInformation();
                    info = fileInformation.get(i);
                    ArrayList<Integer> chunkList = new ArrayList<>();
                    chunkList.add(info.getChunkList().get(j));
                    info.setChunkList(chunkList);
                    infoList.add(info);
                }
            }


            if(infoList!= null && infoList.size()>0){
                Log.d("File Chunk 0 : ", infoList.get(0).toString());
                FileInformation info = infoList.get(0);

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
                jsonString = MethodHandler.convertObjectToJsonString(info);
                DeviceDetailFragment.sendData(context, filePath, fileName, fileLength, isDataTransfer, jsonString);
                Log.d("Send Data:", "Done");
            }
            else{
                Toast.makeText(context,"No New Files To Send",Toast.LENGTH_LONG).show();
            }


        }
    }

    public static void sendData(Context context, String filePath, String fileName, Long fileLength, boolean isDataTransfer, String jsonString){

        Log.d(ThesisActivity.TAG, "Intent--------" +filePath);

        Intent serviceIntent = new Intent(context, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, filePath);

        //////////////////////////////////////////////////////////////////////////////////////////////

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
                Log.d("CLient","Sender");
                host = ownerIP;
                subPort = FileTransferService.PORT;
                serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, ownerIP);
            }

            serviceIntent.putExtra(FileTransferService.Extension, fileName);
            serviceIntent.putExtra(FileTransferService.IsDataTranser, isDataTransfer);
            serviceIntent.putExtra(FileTransferService.DATAINFORMATION, jsonString);
            serviceIntent.putExtra(FileTransferService.FileLength, fileLength+"");
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
            if(host != null && subPort != -1){
                String send = "Sending: Information File";
                if(!isDataTransfer) DeviceDetailFragment.showProgress(context,send);
                Log.d("Sending :","From Send Function");
                context.startService(serviceIntent);
            }else{
                dismissProgressDialog();
                Toast.makeText(context,"Host address not found. Please Reeee-connect.", Toast.LENGTH_LONG).show();
            }


        }else{
            dismissProgressDialog();
            Toast.makeText(context,"Host address not found. Please connect again", Toast.LENGTH_LONG).show();
        }

        /*String isSend = SharedPreferencesHandler.getStringValue(context,MethodHandler.IsFileSend);

        if(isSend.equalsIgnoreCase("true")){

            SharedPreferencesHandler.setStringValues(context,MethodHandler.IsFileSend,"false");

            FirstConnectionMessageAsyncTask firstObj = new FirstConnectionMessageAsyncTask(context, FileTransferService.RequestInformationFile);
            if (firstObj != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    firstObj.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            new String[] { null });
                } else
                    firstObj.execute();
            }

        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        //dismissProgressDialog();

        if(resultCode == getActivity().RESULT_OK){
            Uri uri = data.getData();
            String selectedFilePath = uri.getPath();
            String extension = "";
            try {
                //selectedFilePath = MethodHandler.getPath(uri,getActivity());
                selectedFilePath = MethodHandler.getRealPathFromURI(getActivity().getContentResolver(), uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(selectedFilePath != null){
                FileCopyAsyncTask firstObj = new FileCopyAsyncTask(getActivity(), selectedFilePath);
                if (firstObj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        firstObj.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[] { null });
                        } else
                            firstObj.execute();
                    }
                Log.d("File Copied","Location: thesis/files");

                ArrayList<FileInformation> informationFile = new ArrayList<>();
                ArrayList<FileInformation> informationFileList = new ArrayList<>();

                informationFile = MethodHandler.readInformationFile();
                informationFileList = MethodHandler.readFileList();


                if(informationFileList != null){
                    if(informationFile == null ) MethodHandler.writeInformationFile(informationFileList);
                    else{
                        informationFile = MethodHandler.updateInformationFile(informationFile,informationFileList);
                        if(informationFile!=null) MethodHandler.writeInformationFile(informationFile);
                        else{
                            Toast.makeText(getActivity(),"No File to send",Toast.LENGTH_LONG);
                        }
                    }
                }


            }
            else{
                Log.e("", "path is null");
                return;
            }

        }
        else{
            Toast.makeText(getActivity(), "Cancelled Request",Toast.LENGTH_LONG);
        }


        //////////////////////////////////////////////////////////////////////////////////////////////
    }

    public static void runServer(Context context){
        FileServerAsyncTask fileServerAsyncTask = new FileServerAsyncTask(context,FileTransferService.PORT);
        if(fileServerAsyncTask!= null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                fileServerAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{ null});
            }else{
                fileServerAsyncTask.execute();
            }
        }
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        //DeviceListFragment deviceListFragment = (DeviceListFragment)getFragmentManager().findFragmentById(R.id.fragmentDeviceList);
        //ThesisActivity.showHideFragment(deviceListFragment, "hide");

        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        TextView textView = (TextView) contentView.findViewById(R.id.tvGroupOwner);
        textView.setText(getResources().getString(R.string.group_owner_text)+ ((info.isGroupOwner==true)? getResources().getString(R.string.yes):getResources().getString(R.string.no)));

        textView = (TextView) contentView.findViewById(R.id.tvGroupIp);
        textView.setText("Group Owner IP: "+ info.groupOwnerAddress.getHostAddress());

        try {
            String GroupOwner = info.groupOwnerAddress.getHostAddress();
            if(GroupOwner!=null && !GroupOwner.equals("")) SharedPreferencesHandler.setStringValues(getActivity(),
                    "GroupOwnerAddress", GroupOwner);
            contentView.findViewById(R.id.btnGallery).setVisibility(View.VISIBLE);
            if (info.groupFormed && info.isGroupOwner) {

                SharedPreferencesHandler.setStringValues(getActivity(),
                        "ServerBoolean", "true");

                FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
                        getActivity(), FileTransferService.PORT);
                if (FileServerobj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerobj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[] { null });
                    }
                    else
                        FileServerobj.execute();
                }

            }
            else  {

                    FirstConnectionMessageAsyncTask firstObj = new FirstConnectionMessageAsyncTask(getActivity(), FileTransferService.InetAddress);
                    if (firstObj != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            firstObj.executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR,
                                    new String[] { null });
                        } else
                            firstObj.execute();
                    }

                FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
                        getActivity(), FileTransferService.PORT);
                if (FileServerobj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        FileServerobj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[] { null });
                    }
                    else
                        FileServerobj.execute();

                }


            }

            //ArrayList<FileInformation> fileInformation = MethodHandler.readInformationFile();
            //String filePath = MethodHandler.InformationFilePath;
            //int i = filePath.lastIndexOf("/");
            //String fileName = filePath.substring(0, i);
            //String jsonString = MethodHandler.convertObjectToJsonString(fileInformation);
            //Long fileLength = Long.valueOf(jsonString.length());
            //sendData(getActivity().getApplicationContext(),filePath,fileName,fileLength,false,jsonString);

        }
        catch(Exception e){

        }

        contentView.findViewById(R.id.btnConnect).setVisibility(View.GONE);
        contentView.findViewById(R.id.btnDisconnect).setVisibility(View.VISIBLE);
    }

    public void showDetails(WifiP2pDevice device){
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        //TextView textView = (TextView) contentView.findViewById(R.id.tvPeerDeviceAddress);
        //textView.setText(device.deviceAddress);
        //TextView  textView = (TextView) contentView.findViewById(R.id.tvPeerDeviceInfo);
        //textView.setText(device.toString());

        TextView  textView = (TextView) contentView.findViewById(R.id.tvDeviceName);
        textView.setText(device.deviceName);

        textView = (TextView) contentView.findViewById(R.id.tvDeviceDetails);
        textView.setText(DeviceListFragment.getDeviceStatus(device.status));
        if(DeviceListFragment.getDeviceStatus(device.status).equalsIgnoreCase("available")){
            textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        else if(DeviceListFragment.getDeviceStatus(device.status).equalsIgnoreCase("connected")){
            textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }else textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

    }

    public void resetViews(){
        contentView.findViewById(R.id.btnConnect).setVisibility(View.VISIBLE);
        //TextView view = (TextView)contentView.findViewById(R.id.tvPeerDeviceAddress);
        //view.setText("");
        //TextView  view = (TextView) contentView.findViewById(R.id.tvPeerDeviceInfo);
        //view.setText("");
        TextView  view = (TextView) contentView.findViewById(R.id.tvGroupOwner);
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

            }

            dismissProgressDialog();

            out.close();
            inputStream.close();
            long endTime=System.currentTimeMillis()-startTime;
            Log.v("","Time taken to transfer all bytes is : "+endTime);

            if(staticProgressDialog != null){
                if(staticProgressDialog.isShowing()){
                    staticProgressDialog.dismiss();
                }
            }

        } catch (IOException e) {
            Log.d(ThesisActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    public static void showProgress(Context context, final String task) {
        dismissProgressDialog();
        if (staticProgressDialog == null) {
            staticProgressDialog = new ProgressDialog(context,
                    ProgressDialog.THEME_HOLO_LIGHT);
        }
        Handler handle = new Handler();
        final Runnable s = new Runnable() {
            public void run() {
                DeviceDetailFragment.staticProgressDialog.setMessage(task);
                DeviceDetailFragment.staticProgressDialog.setIndeterminate(false);
                DeviceDetailFragment.staticProgressDialog.setMax(100);
                DeviceDetailFragment.staticProgressDialog.setProgress(0);
                DeviceDetailFragment.staticProgressDialog.setProgressNumberFormat(null);
                DeviceDetailFragment.staticProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                DeviceDetailFragment.staticProgressDialog.show();
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
