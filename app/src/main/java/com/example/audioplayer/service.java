package com.example.audioplayer;


import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import java.io.IOException;

public class service extends Service {

    private final String TAG = service.class.getSimpleName();

    MediaPlayer mediaPlayer;

    IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class LocalBinder extends Binder {

        public service getService() {
            Log.i(TAG, "getService: ");
            return service.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    public void play(String path) {
        Log.i(TAG, "play: ");

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    public void playerResume() {
        Log.i(TAG, "player resume: ");
        mediaPlayer.start();
    }

    public void playerStop() {
        Log.i(TAG, "playerstop: ");
        mediaPlayer.pause();
    }

    public int playerProgress() {
        if (mediaPlayer == null)
            return 0;
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isPlaying() {
        if (mediaPlayer.isPlaying()) return true;
        return false;
    }


    public void seekTo(int position){
        Log.i(TAG, "seekto: ");
        mediaPlayer.seekTo(position);
    }

    public int duration() {
        if (mediaPlayer == null)
            return 0;
        return mediaPlayer.getDuration();
    }


}
