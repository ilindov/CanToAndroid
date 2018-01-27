package com.lindov.android.rpican;

import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import android.os.Handler;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ilia on 22.01.18.
 */

public class LogDual {
    public static EditText logArea;
    public static Handler handler;

    public static void v(String tag, String msg) {
        String msg_ui = Thread.currentThread().getId() + " " + "V/" + tag + ": " + msg + "\n";
        msg = Thread.currentThread().getId() + " " + msg + "\n";
        updateUILog(msg_ui);
        Log.v(tag, msg);
    }

    public static void e(String tag, String msg) {
        String msg_ui = Thread.currentThread().getId() + " " + "E/" + tag + ": " + msg + "\n";
        msg = Thread.currentThread().getId() + " " + msg + "\n";
        updateUILog(msg_ui);
        Log.e(tag, msg);
    }

    public static void i(String tag, String msg) {
        String msg_ui = Thread.currentThread().getId() + " " + "I/" + tag + ": " + msg + "\n";
        msg = Thread.currentThread().getId() + " " + msg + "\n";
        updateUILog(msg_ui);
        Log.i(tag, msg);
    }

//    Throwables

    public static void v(String tag, String msg, Throwable tr) {
        String msg_ui = Thread.currentThread().getId() + " " + "V/" + tag + ": " + msg + "\n";
        msg = Thread.currentThread().getId() + " " + "V/" + ": " + msg + "\n";
        updateUILog(msg_ui);
        Log.v(tag, msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        String msg_ui = Thread.currentThread().getId() + " " + "E/" + tag + ": " + msg + "\n";
        msg = Thread.currentThread().getId() + " " + msg + "\n";
        updateUILog(msg_ui);
        Log.e(tag, msg, tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        String msg_ui = Thread.currentThread().getId() + " " + "I/" + tag + ": " + msg + "\n";
        msg = Thread.currentThread().getId() + " " + msg + "\n";
        updateUILog(msg_ui);
        Log.i(tag, msg, tr);
    }

    private static void updateUILog(String text) {
        Message msg = Message.obtain();
        text = DateFormat.getDateTimeInstance().format(new Date()) + " " + text;
        msg.obj = text;
        handler.sendMessage(msg);
    }
}
