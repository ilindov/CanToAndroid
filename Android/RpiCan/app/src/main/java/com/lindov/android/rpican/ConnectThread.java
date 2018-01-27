package com.lindov.android.rpican;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ilia on 15.01.18.
 */

public class ConnectThread extends Thread {
    private static final String LOGTAG = RpiCanService.LOGTAG;
    private static final String BT_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothDevice btDevice;
    private RpiCanService service;
    private BluetoothSocket btSocket = null;
    private CommunicatonThread communicatonThread = null;

    public ConnectThread(RpiCanService service, BluetoothDevice btDevice) {
        this.service = service;
        this.btDevice = btDevice;
    }

    @Override
    public void run() {
        while (true) {
            if (isInterrupted()) {
                LogDual.i(LOGTAG, "Connect thread is interrupted. Exiting...");
                try {
                    if (communicatonThread != null) {
                        communicatonThread.interrupt();
                    }
                    if (btSocket != null) {
                        btSocket.close();
                    }
                }
                catch (IOException closeException) {
                    LogDual.e(LOGTAG, "Unable to close socket");
                }
                return;
            }

            if (btSocket == null || !btSocket.isConnected()) {
                if (communicatonThread != null)
                    communicatonThread.interrupt();
                try {
                    if (btSocket != null)
                        btSocket.close();
                }
                catch (IOException e) {
                    LogDual.v(LOGTAG, "Socket cannot be closed.");
                }

                createSocketAndConnect();
            }

            try {
                sleep(5000);
            }
            catch (InterruptedException e) {
                interrupt();
                continue;
            }
        }
    }

    private boolean createSocketAndConnect() {
        LogDual.v(LOGTAG, "Trying to connect socket...");
        while (!connect()) {
            if (isInterrupted()) {
                LogDual.i(LOGTAG, "CSC...Connect thread is interrupted. Exiting...");
                try {
                    if (communicatonThread != null) {
                        communicatonThread.interrupt();
                    }
                    if (btSocket != null) {
                        btSocket.close();
                    }
                }
                catch (IOException closeException) {
                    LogDual.e(LOGTAG, "Unable to close socket");
                }
                return false;
            }

            LogDual.v(LOGTAG, "... not connected.");
            try {
                sleep(5000);
            }
            catch (InterruptedException e) {
                interrupt();
                continue;
            }
            LogDual.v(LOGTAG, "Trying to connect socket...");
        }

        return true;
    }

    private boolean connect() {
        LogDual.v(LOGTAG, "connect() called...");

//
//        Map<Thread, StackTraceElement[]> myMap = Thread.getAllStackTraces();
//        Iterator<Thread> iter = myMap.keySet().iterator();
//        int count = 1;
//        while (iter.hasNext()) {
//            Thread t = iter.next();
//            LogDual.v(LOGTAG,"Thread " + count++ +":" + t.getId());
//        }
//

        if (communicatonThread != null) {
            LogDual.v(LOGTAG, "CommunicatonThread exists. Interrupting it...");
            if(btSocket != null) {
                try {
                    btSocket.close();
                }
                catch (IOException e) {
                    LogDual.e(LOGTAG, "Cannot close socket. Leaking a thread.", e);
                }
                finally {
                    btSocket = null;
                }
            }
            communicatonThread.interrupt();
            communicatonThread = null;
        }

        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(BT_UUID));
        }
        catch (IOException e) {
            LogDual.e(LOGTAG, "Cannot create rfcomm socket. Exiting...");
            service.stopSelf();
        }

        try {
            btSocket.connect();
            communicatonThread = new CommunicatonThread(service, btSocket);
            communicatonThread.start();
        }
        catch (IOException e) {
            return false;
        }

        if (btSocket.isConnected())
            return true;
        else
            return false;
    }

    public void close() {
        LogDual.v(LOGTAG, "Closing connection thread...");

        try {
            if (communicatonThread != null) {
                communicatonThread.interrupt();
            }
            if (btSocket != null) {
                btSocket.close();
            }
        }
        catch (IOException e) {
            LogDual.e(LOGTAG, "Could not close the client socket", e);
        }
        finally {
            interrupt();
        }
    }
}
