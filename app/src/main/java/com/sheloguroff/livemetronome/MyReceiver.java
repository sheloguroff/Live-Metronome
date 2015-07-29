package com.sheloguroff.livemetronome;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = MyReceiver.class.getSimpleName();
    // Same as constants in android.bluetooth.BluetoothProfile, which is API level 11.
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    Metronome rMetronome = new Metronome();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "GOT INTENT " + intent);
        if (isConnected(intent)) {
            Log.i(TAG, "Connected to Bluetooth A2DP.");
                MainActivity.setBluetoothDelay(300);
                MainActivity.StateChanged();
        } else if (isDisconnected(intent)) {
            Log.i(TAG, "Disconnected from Bluetooth A2DP.");
                MainActivity.setBluetoothDelay(0);
                MainActivity.StateChanged();
        }
    }

    private boolean isConnected(Intent intent) {
        if ("android.bluetooth.a2dp.action.SINK_STATE_CHANGED".equals(intent.getAction()) &&
                intent.getIntExtra("android.bluetooth.a2dp.extra.SINK_STATE", -1) == STATE_CONNECTED) {
            return true;
        }
        else if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(intent.getAction()) &&
                intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == STATE_CONNECTED) {
            return true;
        }
        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
            return true;
        }
        return false;
    }

    private boolean isDisconnected(Intent intent) {
        if ("android.bluetooth.a2dp.action.SINK_STATE_CHANGED".equals(intent.getAction()) &&
                intent.getIntExtra("android.bluetooth.a2dp.extra.SINK_STATE", -1) == STATE_DISCONNECTED) {
            return true;
        }
        else if ("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED".equals(intent.getAction()) &&
                intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1) == STATE_DISCONNECTED) {
            return true;
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            return true;
        }
        return false;
    }
}

//public class MyReceiver extends BroadcastReceiver {
//    public MyReceiver() {
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//        Log.i("MyReceiver", action);
//        Bundle extras = intent.getExtras();
//        if (extras == null){
//            Log.i("MyReceiver", "No Extras");
//        } else {
//            String extra = intent.getExtras().keySet().toString();
//            Log.i("MyReceiver", "Extras: " + extra);
//        }
//
//    }
//}
