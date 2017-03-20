package com.example.audioplayer;


import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;


import java.io.IOException;

public class service extends Service {

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
            return service.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    public void play(String path) {
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

    public void playerresume(){
        mediaPlayer.start();
    }

    public void playerstop() {
        mediaPlayer.pause();
    }

    public int playerprogress() {
        return mediaPlayer.getCurrentPosition();
    }

    public boolean isplaying() {
        if (mediaPlayer.isPlaying()) return true;
        return false;
    }


    public void seekto(int position) {
        mediaPlayer.seekTo(position);
    }

    public int duration() {
        return mediaPlayer.getDuration();
    }


}
