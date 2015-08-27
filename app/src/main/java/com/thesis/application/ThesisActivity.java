package com.thesis.application;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import com.thesis.application.fragments.DeviceDetailFragment;
import com.thesis.application.fragments.DeviceListFragment;
import com.thesis.application.interfaces.DeviceActionListener;

/**
 * Created by bahar61119 on 7/14/2015.
 */
public class ThesisActivity extends Activity implements WifiP2pManager.ChannelListener, DeviceActionListener{

    public static final String TAG = "thesisWifiDirect";
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private Boolean isWifiEnabled;
    private Boolean retryChannel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thesis);


        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        receiver = new WifiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_thesis,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.miWifiDirectEnable:
                if(manager !=null && channel != null){
                    WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
                    if(wifiManager.isWifiEnabled()){
                        wifiManager.setWifiEnabled(false);
                        item.setIcon(getResources().getDrawable(android.R.drawable.button_onoff_indicator_off));
                        isWifiEnabled = false;
                    }else{
                        wifiManager.setWifiEnabled(true);
                        item.setIcon(getResources().getDrawable(android.R.drawable.button_onoff_indicator_on));
                        isWifiEnabled = true;
                    }
                }else {
                    Log.e("WifiP2p", "channel or manager is null");
                }

                return true;


            case R.id.miSearchPeers:
                if(!isWifiEnabled){
                    Toast.makeText(this,R.string.p2p_off_warning,Toast.LENGTH_LONG).show();
                    return true;
                }

                final DeviceListFragment deviceListFragment = (DeviceListFragment)getFragmentManager().findFragmentById(R.id.fragmentDeviceList);
                deviceListFragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ThesisActivity.this,"Descovery Innitated...",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(ThesisActivity.this, "Descovery Failed : " + reason ,Toast.LENGTH_LONG).show();
                    }
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onChannelDisconnected() {
        if(manager != null && !retryChannel){
            retryChannel = true;
            Toast.makeText(ThesisActivity.this,"Channel Lost. Trying again...",Toast.LENGTH_SHORT).show();
            resetData();
            manager.initialize(this, getMainLooper(), this);
        }else {
            Toast.makeText(ThesisActivity.this, "Channel is probably lost. Try to re-enable Wifi",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment deviceDetailFragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.fragmentDeviceDetail);
        deviceDetailFragment.showDetails(device);
    }

    @Override
    public void cancelDisconnect() {
        final DeviceListFragment deviceListFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.fragmentDeviceList);
        if(deviceListFragment.getDevice() == null && deviceListFragment.getDevice().status == WifiP2pDevice.CONNECTED) {
            disconnect();
        } else if(deviceListFragment.getDevice().status == WifiP2pDevice.AVAILABLE || deviceListFragment.getDevice().status == WifiP2pDevice.INVITED){
            manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ThesisActivity.this, "Aborting connection ", Toast.LENGTH_SHORT ).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(ThesisActivity.this,"Abort request failed. Reason code : "+ reason, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //WifiDirectListener will notify. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(ThesisActivity.this,"Connect Failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment)getFragmentManager().findFragmentById(R.id.fragmentDeviceDetail);

        manager.removeGroup(channel,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {
                fragment.resetViews();
            }
        });
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled)
    {
        this.isWifiEnabled = isWifiP2pEnabled;
    }

    public void resetData(){
        DeviceListFragment deviceListFragment = (DeviceListFragment) getFragmentManager().findFragmentById(R.id.fragmentDeviceList);
        DeviceDetailFragment deviceDetailFragment =  (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.fragmentDeviceDetail);
        if(deviceListFragment!=null) {
            deviceListFragment.clearPeers();
        }
        if(deviceDetailFragment!=null){
            deviceDetailFragment.resetViews();
        }
    }
}
