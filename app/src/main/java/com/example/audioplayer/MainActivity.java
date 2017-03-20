package com.example.audioplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;



public class MainActivity extends Activity implements View.OnTouchListener, AdapterView.OnItemClickListener {


    SeekBar seekBar;
    ListView listViewmusic;
    TextView textViewlastplay;
    Uri dbURI = Uri.parse("content://musicdbauth/" + musicdb.DB_TABLE_NAME);

    private ServiceConnection Conn;
    private service mediaservice;
    private boolean bound;
    private boolean hasplay = true;
    private boolean stopservice;
    private SharedPreferences settings;
    final String PREFS_NAME = "settings";
    private static String lastplay = "";
    private boolean dbupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, service.class));

        settings = getSharedPreferences(PREFS_NAME, 0);
        lastplay = settings.getString("last_play", " ");
        textViewlastplay = (TextView) findViewById(R.id.nowPlay);
        textViewlastplay.setText(lastplay);


        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnTouchListener(this);
        listViewmusic = (ListView) findViewById(R.id.musiclist);
        listViewmusic.setOnItemClickListener(this);

        if (settings.getBoolean("db_update", true))
            searchmusic();
        setListview();

        Conn = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {

                mediaservice = ((service.LocalBinder) binder).getService();
                bound = true;
                if (mediaservice.isplaying()) {
                    stopservice = false;
                    seekBar.setMax(mediaservice.duration());
                } else
                    stopservice = true;
                progress();

            }

            public void onServiceDisconnected(ComponentName name) {

                bound = false;
            }
        };
        bindService(new Intent(this, service.class), Conn, 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        hasplay = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasplay)
            progress();
        hasplay = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(Conn);
        if (stopservice) {
            hasplay = false;
            stopService(new Intent(this, service.class));
        }
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("db_update", false);
        editor.apply();
    }

    public void progress() {
        new Thread(new Runnable() {
            @Override
            public void run() {


                while (hasplay) {
                    seekBar.setProgress(mediaservice.playerprogress());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }

    public void onClickStart(View v) {

        //onItemClick(null, null, 0, 0);
        mediaservice.playerresume();
        stopservice = false;
    }

    public void onClickStop(View v) {

        mediaservice.playerstop();
        stopservice = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mediaservice.seekto(seekBar.getProgress());

        return false;
    }

    private void searchmusic() {
        File filepath = Environment.getExternalStorageDirectory();
        search(filepath.listFiles());
        setListview();

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        stopservice = false;
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