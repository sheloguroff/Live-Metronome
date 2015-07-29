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

    /** Creates AudioTrack with one click and sets loop to match given bpm */
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



    /** Sets marker close the end of the track (when setting it right at the end the listener
     * doesn't work). Passes message to the handler in main activity when the marker is reached.
     * */
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


    /** Sets marker on AudioTrack. When marker is reached it sends message once and
     * doesn't do the job in the next loop. So it should be set again.*/
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

    /** Receives tempo in bpm and converts it to a length of a loop in samples.
     * Then sets the loop length. */
    public void setLoopPoints(int bpm) {
        loopPoints = 60 * sampleRate / bpm;
        metronomeTrack.setLoopPoints(0, loopPoints, -1);
    }

    public int getPlayState() {
        playState = metronomeTrack.getPlayState();
        return playState;
    }


    /** Receives tempo in bpm and converts it to a length of a loop in samples.
     * Then sets the loop length and marker position. If head position is beyond the new loop, sets it
     * to the first sample of the track. If metronome was playing, goes on playing. */
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


    /** Compensates the computing delay to let the click start exactly in time. */
    public void compensateDelay () {
        metronomeTrack.pause();
        metronomeTrack.setPlaybackHeadPosition(metronomeTrack.getPlaybackHeadPosition()+3000);
        metronomeTrack.play();
        Log.i("CompensateDelay", "Done!");
    }

   /** Pauses click for d millis */
    public void setDelay (int d){
        metronomeTrack.pause();
        SystemClock.sleep(d);
        metronomeTrack.play();
    }
            

    /** Loads the sound of a click in array of bytes to the Audiotrack */
    public void loadSound(InputStream in) throws IOException
    {
        byte[] click = new byte[8544];
        try {
            in.read(click);
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                in.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        metronomeTrack.write(click, 0, click.length);
    }
}
