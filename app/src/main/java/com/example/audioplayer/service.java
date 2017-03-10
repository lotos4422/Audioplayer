package com.example.audioplayer;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**********
 *author maksim kozachenko
 */

public class service extends Service {

    String LOG_TAG = "log";
    MediaPlayer mediaPlayer;

    IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "start service");
        mediaPlayer = mediaPlayer.create(this, R.raw.okea);

        super.onCreate();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "service on bind");
        return binder;
    }

    class LocalBinder extends Binder {
        public service getService() {
            Log.d(LOG_TAG, "service local binder");
            return service.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,"service destroy");
    }

    public boolean isplaying() {
        if (mediaPlayer.isPlaying()) return true;
        return false;
    }

    public void play() {
        mediaPlayer.start();
    }

    public void stop() {
        mediaPlayer.pause();
    }

    public int progress() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekto(int position) {
        mediaPlayer.seekTo(position);
    }

    public int duration() {
        return mediaPlayer.getDuration();
    }


}
