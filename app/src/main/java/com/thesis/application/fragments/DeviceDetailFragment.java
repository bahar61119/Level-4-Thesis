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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.thesis.application.FileTransferService;
import com.thesis.application.R;
import com.thesis.application.ThesisActivity;
import com.thesis.application.asynctask.FileServerAsyncTask;
import com.thesis.application.interfaces.DeviceActionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by bahar61119 on 7/15/2015.
 */
public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    ProgressDialog progressDialog = null;
    private View contentView = null;
    private WifiP2pDevice device = null;
    private WifiP2pInfo info;

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
        TextView statusText = (TextView) contentView.findViewById(R.id.tvStatusText);
        statusText.setText("Sending: "+ uri);
        Log.d(ThesisActivity.TAG, "Intent--------" +uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, "8988");
        getActivity().startActivity(serviceIntent);
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

        if(info.groupFormed && info.isGroupOwner){
            new FileServerAsyncTask(getActivity(), contentView.findViewById(R.id.tvStatusText)).execute();

        }else if(info.groupFormed){
            contentView.findViewById(R.id.btnGallery).setVisibility(View.VISIBLE);
            ((TextView)contentView.findViewById(R.id.tvStatusText)).setText(getResources().getString(R.string.client_text));
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
    }

    public static boolean copyFile(InputStream inputStream, OutputStream outputStream){
        byte buf[] = new byte[1024];
        int len;
        long startTime = System.currentTimeMillis();

        try{
            while ((len = inputStream.read()) != -1){
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            long endTime = System.currentTimeMillis() - startTime;
            Log.v(ThesisActivity.TAG, "Time taken to transfer: "+ endTime);

        }catch (IOException e){
            Log.d(ThesisActivity.TAG, e.toString());
            return false;
        }
        return true;
    }


}
