package com.sheloguroff.livemetronome;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//* */
public class HeadsetConnectedReceiver extends BroadcastReceiver {
    private static final String TAG = HeadsetConnectedReceiver.class.getSimpleName();
    // Same as constants in android.bluetooth.BluetoothProfile, which is API level 11.
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    //* Passes intent to isConnected and is Disconnected methods
    // to check, is A2DP connected or disconnected.
    // Sets proper delay to the flasher to match the click sound.*/
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
