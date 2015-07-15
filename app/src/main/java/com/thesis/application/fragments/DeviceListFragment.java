package com.thesis.application.fragments;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thesis.application.R;
import com.thesis.application.interfaces.DeviceActionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bahar61119 on 7/14/2015.
 */
public class DeviceListFragment extends ListFragment implements WifiP2pManager.PeerListListener {

    View contentView = null;
    ProgressDialog progressDialog = null;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pDevice device;

    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WifiP2pListAdapter(getActivity(), R.layout.row_devices, peers));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.device_list, null);
        return contentView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).showDetails(device);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        this.peers.clear();
        this.peers.addAll(peers.getDeviceList());
        ((WifiP2pListAdapter) getListAdapter()).notifyDataSetChanged();
        if (this.peers.size() == 0) {
            Toast.makeText(getActivity().getApplicationContext(), "No devices found", Toast.LENGTH_LONG);
            return;
        }
    }

    public WifiP2pDevice getDevice(){
        return device;
    }

    public void clearPeers() {
        peers.clear();
        ((WifiP2pListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;

        TextView deviceName = (TextView) contentView.findViewById(R.id.tvMyDeviceName);
        TextView deviceStatus = (TextView) contentView.findViewById(R.id.tvMyDeviceStatus);
        deviceName.setText(device.deviceName);
        deviceStatus.setText(getDeviceStatus(device.status));
    }

    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "Finding Peers", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
    }

    // Array Adapter for List Fragment that maintains wifipP2pDevice List

    private class WifiP2pListAdapter extends ArrayAdapter<WifiP2pDevice> {

        List<WifiP2pDevice> items;


        public WifiP2pListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.row_devices, null);
            }

            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) view.findViewById(R.id.tvDeviceName);
                TextView bottom = (TextView) view.findViewById(R.id.tvDeviceDetails);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(device.status));
                }
            }


            return view;

        }
    }
}
