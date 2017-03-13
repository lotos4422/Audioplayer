package com.example.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import java.sql.Time;
import java.util.StringTokenizer;
import java.util.Timer;


public class MainActivity extends Activity implements View.OnTouchListener {

    String LOG_TAG = "log";
    SeekBar seekBar;

    ServiceConnection Conn;
    service myService;
    boolean bound;
    boolean hasplay = true;
    boolean stopservice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Main activity create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, service.class));

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnTouchListener(this);

        Conn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                myService = ((service.LocalBinder) binder).getService();
                bound = true;
                if (myService.isplaying() == true)
                    stopservice = false;
                else
                    stopservice = true;
                progress();
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };
        bindService(new Intent(this, service.class), Conn, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "Main activity on save instance state");
        hasplay = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Main activity on resume");
        if (!hasplay)
            progress();
        hasplay = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "Main activity destroy");
        //if (!hasplay)
        unbindService(Conn);
        if (stopservice) {
            hasplay = false;
            stopService(new Intent(this, service.class));
        }
    }

    public void progress() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d(LOG_TAG, "Main activity progress seek bar");

                seekBar.setMax(myService.duration());

                while (hasplay)
                    seekBar.setProgress(myService.progress());

                Log.d(LOG_TAG, "Main activity progress thread stopped");
            }
        }).start();

    }

    public void onClickStart(View v) {
        Log.d(LOG_TAG, "Main activity on click start");
        myService.play();
        stopservice = false;
    }

    public void onClickStop(View v) {
        Log.d(LOG_TAG, "Main activity on click stop");
        myService.stop();
        stopservice = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        myService.seekto(seekBar.getProgress());
        Log.d(LOG_TAG, "progress changed");
        return false;
    }
}