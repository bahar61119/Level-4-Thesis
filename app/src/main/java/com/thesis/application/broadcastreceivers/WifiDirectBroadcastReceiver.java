package com.thesis.application.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;

import com.thesis.application.R;
import com.thesis.application.activities.ThesisActivity;
import com.thesis.application.asynctask.FirstConnectionMessageAsyncTask;
import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.fragments.DeviceListFragment;
import com.thesis.application.services.FileTransferService;

/**
 * Created by bahar61119 on 7/12/2015.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private ThesisActivity activity;
    private WifiP2pManager.PeerListListener peerListListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       ThesisActivity activity){
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;


    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                //wifi p2p is enable
                activity.setIsWifiP2pEnabled(true);
            }else{
                //wifi p2p is not enable
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();

            }

        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(manager!=null){
                manager.requestPeers(channel,(WifiP2pManager.PeerListListener) activity.getFragmentManager().findFragmentById(R.id.fragmentDeviceList));
            }

        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(manager == null){
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected()){
                DeviceDetailFragment deviceDetailFragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.fragmentDeviceDetail);
                manager.requestConnectionInfo(channel,deviceDetailFragment);
            }else{
                activity.resetData();
            }

        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            DeviceListFragment deviceListFragment =(DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.fragmentDeviceList);
            deviceListFragment.updateThisDevice((WifiP2pDevice)intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }



    }
}
