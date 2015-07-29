package com.sheloguroff.livemetronome;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class Metronome {


    int sampleRate = 44100;
    int playState = 0;
    int loopPoints;
    AudioTrack metronomeTrack;
    private Message msg;
    private int click = 1;
    int markerDelay = 3000;
    int lastBpm = 60;

    public void createPlayer(final int bpm, InputStream in, final Handler handler) {
        metronomeTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                176400, AudioTrack.MODE_STATIC);
        setLoopPoints(bpm);
        try {
            loadSound(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setListener(handler);
    }



    private void setListener(final Handler handler) {
        metronomeTrack.setNotificationMarkerPosition(loopPoints-markerDelay);
        AudioTrack.OnPlaybackPositionUpdateListener clickListener = new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                msg = new Message();
                msg.obj = click++;
                handler.handleMessage(msg);
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
            }
        };
        metronomeTrack.setPlaybackPositionUpdateListener(clickListener, handler);
    }

    public void setMarker () {
        metronomeTrack.setNotificationMarkerPosition(loopPoints - markerDelay);
    }

    public void play() {
        metronomeTrack.play();
    }

    public void stop() {
        metronomeTrack.pause();
        metronomeTrack.setPlaybackHeadPosition(loopPoints - 1500);
    }

    public int headPosition () {
        int headPosition = metronomeTrack.getPlaybackHeadPosition();
        return headPosition;
    }

    public int getLoopPoints (){
        return loopPoints;
    }

    public void setLoopPoints(int bpm) {
        loopPoints = 60 * sampleRate / bpm;
        metronomeTrack.setLoopPoints(0, loopPoints, -1);
    }

    public int getPlayState() {
        playState = metronomeTrack.getPlayState();
        return playState;
    }


    public void setTempo (int bpm){
        playState = metronomeTrack.getPlayState();
                           metronomeTrack.pause();
        loopPoints = 60 * sampleRate / bpm;
        if (metronomeTrack.getPlaybackHeadPosition()>=loopPoints){
            metronomeTrack.setPlaybackHeadPosition(1);
            metronomeTrack.reloadStaticData();
        }
        metronomeTrack.setLoopPoints(0, loopPoints, -1);
        if (playState == 3) {
            metronomeTrack.play();
        }
        metronomeTrack.setNotificationMarkerPosition(loopPoints-markerDelay);
        lastBpm = bpm;
    }


    public void compensateDelay () {
        metronomeTrack.pause();
        metronomeTrack.setPlaybackHeadPosition(metronomeTrack.getPlaybackHeadPosition()+3000);
        metronomeTrack.play();
        Log.i("CompensateDelay", "Done!");
    }

    public void setDelay (int d){
        metronomeTrack.pause();
        SystemClock.sleep(d);
        metronomeTrack.play();
    }
            

    public void loadSound(InputStream in) throws IOException
    {
        byte[] music = new byte[8544];
        try {
            in.read(music);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                in.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        metronomeTrack.write(music, 0, music.length);
    }
}
