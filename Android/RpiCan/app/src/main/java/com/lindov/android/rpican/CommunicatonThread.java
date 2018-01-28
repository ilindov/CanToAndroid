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

    public CommunicatonThread(RpiCanService service, BluetoothSocket btSocket) {
        this.service = service;
        this.btSocket = btSocket;
        AudioController.audioManager = service.getAudioManager();
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

            LogDual.v(LOGTAG, "Command received: " + request);

            String response = "";
            if (request == null || request.length() == 0) {
                response = "-1";
            }
            else if (request.length() > 0) {
                if (request.equals("NEXT_DOWN")) {
                    AudioController.nextDown();
                    response = "0";
                }
                else if (request.equals("NEXT_UP")) {
                    AudioController.nextUp();
                    response = "0";
                }
                else if (request.equals("PREV_DOWN")) {
                    AudioController.prevDown();
                    response = "0";
                } else if (request.equals("PREV_UP")) {
                    AudioController.prevUp();
                    response = "0";
                }
                else if (request.equals("PLAY_PAUSE_DOWN")) {
                    AudioController.playPauseDown();
                    response = "0";
                }
                else if (request.equals("PLAY_PAUSE_UP")) {
                    AudioController.playPauseUp();
                    response = "0";
                }
                else {
//                    Unknown command
                    response = "-2";
                }
            }

            try {
                outStream.write((response + "\n").getBytes());
            }
            catch (IOException e) {
                LogDual.e(LOGTAG, "IO error in communication loop (write). Exiting...", e);
            }
        }
    }
}
