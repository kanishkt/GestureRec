package com.example.kanishk.project;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import watch.nudge.phonegesturelibrary.AbstractPhoneGestureActivity;

public class MainActivity extends AbstractPhoneGestureActivity implements
        MediaPlayer.OnPreparedListener{

    AudioManager audioManager;
    float point = 0;
    MediaPlayer mp = new MediaPlayer();
    private ArrayList<Song> songList;
    private int songPosn =0;
    boolean isPaused = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Before Manager","a");
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnPreparedListener(this);
        songList = new ArrayList<Song>();
        getSongList();
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
//        if (mp.isPlaying()) {
//            mp.pause();
//            //mp.prepareAsync();
//            //mp.seekTo(0);
//            Log.d("Here", "here");
//        } else {
//            playSong();
//            mp.start();
//
//            Log.d("There", "there");
//        }
//        Log.d("After If","b");
    }

    public void onPrepared(MediaPlayer mp) {
        mp.start();

    }


    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    public void playSong(){
        //play a song
        mp.reset();
        //get song
        Song playSong = songList.get(songPosn);
        String songTitle = playSong.getTitle();

        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            mp.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mp.prepareAsync();
    }

    @Override
    public void onSnap() {
        Toast.makeText(this, "Feeling snappy!", Toast.LENGTH_LONG).show();
        if (mp.isPlaying()) {
            mp.pause();
            isPaused = true;
            Log.d("Here", "here");
        } else {
            if (isPaused){
                mp.start();
                isPaused = false;
            }
            else {
                playSong();
            }
        }
    }

    @Override
    public void onFlick() {
        Toast.makeText(this, "Flick that thang!", Toast.LENGTH_LONG).show();
        songPosn++;
        playSong();

    }

    @Override
    public void onTwist() {
        Toast.makeText(this, "Twistin' the night away", Toast.LENGTH_LONG).show();
    }

    //These functions won't be called until you subscribe to the appropriate gestures
    //in a class that extends AbstractGestureClientActivity in a wear app.

    @Override
    public void onTiltX(float x) {
        //throw new IllegalStateException("This function should not be called unless subscribed to TILT_X.");
        if (x < point) {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            point = x;
        } else {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            point = x;
        }
    }

    @Override
    public void onTilt(float x, float y, float z) {
        //Toast.makeText(this, "We Tilting", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWindowClosed() {
        Log.e("MainWatchActivity", "This function should not be called unless windowed gesture detection is enabled.");
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.release();
    }
}
