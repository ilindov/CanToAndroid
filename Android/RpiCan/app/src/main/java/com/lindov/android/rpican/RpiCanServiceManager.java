package com.lindov.android.rpican;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.text.DateFormat;
import java.util.Date;

public class RpiCanServiceManager extends AppCompatActivity implements Handler.Callback {
    private static final String LOGTAG = RpiCanService.LOGTAG;
    private Intent intent = null;
    private Button btnStart = null;
    private Button btnStop = null;
    private EditText logArea = null;
    private CheckBox showLog = null;
    private Handler handler = null;
    private boolean logAreaClean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rpi_can_service_manager);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop =  (Button) findViewById(R.id.btnStop);
        logArea =  (EditText) findViewById(R.id.logTextArea);
        showLog =  (CheckBox) findViewById(R.id.checkBox);
        logArea.setMovementMethod(new ScrollingMovementMethod());
        logArea.setFocusable(false);
        logArea.setText("Starting...\n");
        LogDual.logArea = logArea;
        handler = new Handler(this);
        LogDual.handler = handler;

        startService((View)null);
    }

    public void startService(View view) {
        LogDual.v(LOGTAG, "Service starting");
        if (intent == null) {
            intent = new Intent(this, RpiCanService.class);
            startService(intent);
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            LogDual.v(LOGTAG, "Service started.");
        }
        else {
            LogDual.v(LOGTAG, "Service not stopped.");
        }
    }

    public void stopService(View view) {
        LogDual.v(LOGTAG, "Service stopping...");
        if (intent != null) {
            stopService(intent);
            intent = null;
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
        else {
            LogDual.v(LOGTAG, "Service not started.");
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        if (showLog.isChecked()) {
            logArea.append((String) message.obj);
            logAreaClean = false;

            final int scrollAmount = logArea.getLayout().getLineTop(logArea.getLineCount()) - logArea.getHeight();
            if (scrollAmount > 0)
                logArea.scrollTo(0, scrollAmount);
            else
                logArea.scrollTo(0, 0);
        }
        else if (!logAreaClean) {
            logArea.setText("");
            logAreaClean = true;
        }
        return true;
    }
}
