package com.lindov.android.rpican;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;
import java.util.Set;

public class RpiCanService extends Service {

    public static final String LOGTAG = "LINDOV_LOG";
    private static final String BT_DEVICE = "rpi-can";
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice btDevice = null;
    private ConnectThread connectThread = null;
    private boolean chargingState = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Bind not allowed (yet, or forever)
        LogDual.v(LOGTAG, "onBind() invoked");

        LogDual.v(LOGTAG, "Exitig onBind()");
        return null;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        LogDual.v(LOGTAG, "onStartCommand() invoked");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            LogDual.e(LOGTAG, "No Bluetooth adapter found. Exiting...");
            stopSelf();
        }
        if (!bluetoothAdapter.isEnabled()) {
            LogDual.e(LOGTAG, "Bluetooth not enabled. Exiting...");
            stopSelf();
        }


        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(BT_DEVICE)) {
                    btDevice = device;
                    break;
                }
            }
        }

        if (btDevice == null) {
            LogDual.e(LOGTAG, "'rpi-can' bluetooth device not paired.");
            stopSelf();
        }

        bluetoothAdapter.cancelDiscovery();

        PowerConnectionReceiver powerConnectionReceiver = new PowerConnectionReceiver();
        powerConnectionReceiver.service = this;

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(powerConnectionReceiver, intentFilter);

        LogDual.v(LOGTAG, "Exiting onStartCommand()");
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        LogDual.v(LOGTAG, "onCreate() invoked");
        LogDual.v(LOGTAG, "Exiting onCreate()");
    }

    @Override
    public void onDestroy() {
        LogDual.v(LOGTAG, "onDestroy() invoked");
        if (connectThread != null) {
            connectThread.close();
        }
        LogDual.v(LOGTAG, "Exiting onDestroy()");
    }

    public AudioManager getAudioManager() {
        return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    public void chargingStateChanged(boolean isCharging) {
        if (isCharging == chargingState)
            return;

        if (isCharging) {
            LogDual.i(LOGTAG, "Charging state changed: CHARGING");
            if (connectThread == null) {
                connectThread = new ConnectThread(this, btDevice);
                connectThread.start();
            }
            chargingState = true;
        }
        else {
            LogDual.i(LOGTAG, "Charging state changed: NOT CHARGING");
            if (connectThread != null) {
                connectThread.close();
                connectThread = null;
            }
            chargingState = false;
        }
    }
}
