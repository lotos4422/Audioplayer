package com.example.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnTouchListener, AdapterView.OnItemClickListener {


    SeekBar seekBar;
    ListView listViewmusic;
    TextView textViewlastplay;
    TextView currentprogress;
    ProgressBar progressBar;
    MusicSearch musicSearch;
    Uri dbURI = Uri.parse("content://musicdbauth/" + musicdb.DB_TABLE_NAME);
    MusicProgress update_music_progress;

    private ServiceConnection Conn;
    private service mediaservice;
    private boolean bound;
    private SharedPreferences settings;
    final String PREFS_NAME = "settings";
    private static String lastplay = "";
    private boolean dbupdate;
    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, service.class));

        settings = getSharedPreferences(PREFS_NAME, 0);
        lastplay = settings.getString("last_play", " ");
        textViewlastplay = (TextView) findViewById(R.id.nowPlay);
        textViewlastplay.setText(lastplay);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        currentprogress = (TextView) findViewById(R.id.cureenttime);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnTouchListener(this);
        listViewmusic = (ListView) findViewById(R.id.musiclist);
        listViewmusic.setOnItemClickListener(this);

        musicSearch = new MusicSearch();
        if (settings.getBoolean("db_update", true))
            musicSearch.execute();
        else {
            musicSearch.onProgressUpdate();
        }

        Conn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.i(TAG, "onServiceConnected: ");
                mediaservice = ((service.LocalBinder) binder).getService();
                bound = true;
                if (mediaservice.isPlaying()) {
                    seekBar.setMax(mediaservice.duration());

                }
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "onServiceDisconnected: ");
                bound = false;
            }
        };
        bindService(new Intent(this, service.class), Conn, 0);
        update_music_progress = (MusicProgress) getLastNonConfigurationInstance();
        if (update_music_progress == null) {
            update_music_progress = new MusicProgress();
            update_music_progress.execute();
        }
        update_music_progress.link(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        unbindService(Conn);

        if (this.isFinishing())
            stopService(new Intent(this, service.class));

        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("db_update", false);
        editor.apply();
    }

    public Object onRetainNonConfigurationInstance() {
        Log.i(TAG, "onRetainNonConfigurationInstance: ");
        update_music_progress.unLink();
        return update_music_progress;
    }


    static class MusicProgress extends AsyncTask<Void, String, Void> {
        MainActivity activity;

        void link(MainActivity act) {
            activity = act;
        }

        void unLink() {
            activity = null;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (true) {

                try {
                    Thread.sleep(1000);
                    Time time = new Time(0, 0, activity.mediaservice.playerProgress() / 1000);
                    publishProgress(time.toString());

                } catch (Exception e) {
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            try {
                activity.seekBar.setProgress(activity.mediaservice.playerProgress());
                activity.currentprogress.setText(values[0]);
            } catch (Exception e) {
            }
        }

    }


    public void onClickStart(View v) {
        Log.i(TAG, "onClickStart: ");
        mediaservice.playerResume();
    }

    public void onClickStop(View v) {
        Log.i(TAG, "onClickStop: ");
        mediaservice.playerStop();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mediaservice.seekTo(seekBar.getProgress());
        Log.i(TAG, "onTouch: ");
        return false;
    }

    class MusicSearch extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listViewmusic.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            File filepath = Environment.getExternalStorageDirectory();
            search(filepath.listFiles());
            publishProgress();


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            setListview();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
            listViewmusic.setVisibility(View.VISIBLE);
        }

        private void search(File[] file) {
            for (File f : file) {
                if (f.getAbsolutePath().contains(".mp3")) {
                    String s = f.getAbsolutePath();
                    s = s.substring(s.lastIndexOf("/") + 1);
                    ContentValues values = new ContentValues();
                    values.put(musicdb.DB_SONG_NAME, s);
                    values.put(musicdb.DB_SDPATH, f.getAbsolutePath());
                    getContentResolver().insert(dbURI, values);

                }
                if (f.listFiles() != null)
                    search(f.listFiles());
            }
        }

        private void setListview() {
            Cursor cursor = getContentResolver().query(dbURI, null, null, null, null);
            cursor.moveToFirst();
            int nameindex = cursor.getColumnIndex(musicdb.DB_SONG_NAME);
            ArrayList<String> s = new ArrayList<String>();
            while (!cursor.isAfterLast()) {
                s.add(cursor.getString(nameindex));
                cursor.moveToNext();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, s);
            listViewmusic.setAdapter(adapter);
            cursor.close();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick: ");
        Integer select = ++position;
        Cursor cursor = getContentResolver().query(dbURI, null, "_id = " + select.toString(), null, null);
        cursor.moveToFirst();

        int indexsong = cursor.getColumnIndex(musicdb.DB_SONG_NAME);
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("last_play", cursor.getString(indexsong));
        textViewlastplay.setText(cursor.getString(indexsong));
        editor.apply();

        int indexsd = cursor.getColumnIndex(musicdb.DB_SDPATH);
        mediaservice.play(cursor.getString(indexsd));
        cursor.close();
        seekBar.setMax(mediaservice.duration());
    }
}