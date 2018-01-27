package com.lindov.android.rpican;

import android.bluetooth.BluetoothSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by ilia on 15.01.18.
 */

public class CommunicatonThread extends Thread {
    private static final String LOGTAG = RpiCanService.LOGTAG;
    private RpiCanService service;
    private BluetoothSocket btSocket;
    private AudioController audioController;

    public CommunicatonThread(RpiCanService service, BluetoothSocket btSocket) {
        this.service = service;
        this.btSocket = btSocket;
        audioController = new AudioController(service);
    }

    @Override
    public void run() {
        LogDual.v(LOGTAG, "Communication thread started...");

        BufferedReader inReader = null;
        OutputStream outStream = null;

        try {
            inReader = new BufferedReader(new InputStreamReader(btSocket.getInputStream()));
            outStream = btSocket.getOutputStream();
        }
        catch (IOException e) {
            LogDual.e(LOGTAG,  "Cannot get input/output streams. Exiting");
            service.stopSelf();
        }

        while (true) {
            if (isInterrupted()) {
                LogDual.v(LOGTAG, "CommunicationThread interrupted. Exiting...");
                return;
            }

            String request = "";
            try {
                request = inReader.readLine();
            }
            catch (IOException e) {
                LogDual.e(LOGTAG, "Error reading from socket. Trying to close it. Exiting of communication thread.");
                try {
                    btSocket.close();
                }
                catch (IOException ex) {}
                return;
            }

            String response = "";
            if (request == null || request.length() == 0) {
                response = "-1";
            }
            else if (request.length() > 0) {
                LogDual.v(LOGTAG, "Read: '" + request + "'");

                if (request.equals("0")) {
                    response = "0";
                }
                else {
                    response = "-1";
                }
            }
//                audioController.play();

            try {
                outStream.write((response + "\n").getBytes());
            }
            catch (IOException e) {
                LogDual.e(LOGTAG, "IO error in communication loop (write). Exiting...", e);
            }
        }
    }
}
