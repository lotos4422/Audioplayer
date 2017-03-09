package com.example.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;


public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    String LOG_TAG = "log";
    SeekBar seekBar;

    Intent intent;
    ServiceConnection Conn;
    service myService;
    boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Main activity create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, service.class);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);


        Conn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(LOG_TAG, "MainActivity onServiceConnected");
                myService = ((service.LocalBinder) binder).getService();
                bound = true;
                if (myService.isplaying())
                    progress();
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(LOG_TAG, "MainActivity onServiceDisconnected");
                bound = false;
            }
        };

        bindService(intent, Conn, BIND_AUTO_CREATE);

    }


    public void progress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Main activity progress seek bar");

                seekBar.setMax(myService.duration());
                int wait = myService.duration() / 100;

                while (true) {
                    if (!myService.isplaying()) {
                        Log.d(LOG_TAG, "Main activity thread stoped");
                        return;
                    }
                    seekBar.setProgress(myService.progress());
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public void onClickStart(View v) {
        Log.d(LOG_TAG, "Main activity on click start");

        myService.play();
        progress();
    }

    public void onClickStop(View v) {
        Log.d(LOG_TAG, "Main activity on click stop");
        myService.stop();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(LOG_TAG, "Main activity seek to");
        myService.seekto(seekBar.getProgress());
    }

}
