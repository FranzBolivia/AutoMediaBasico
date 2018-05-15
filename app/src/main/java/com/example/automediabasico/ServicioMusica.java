package com.example.automediabasico;

import android.app.Service;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ServicioMusica extends MediaBrowserService {
    private MediaSession mSession;
    private List<MediaMetadata> mMusic;
    private MediaPlayer mPlayer;
    private MediaMetadata mCurrentTrack;
    private int temp;

    @Override
    public void onCreate() {
        super.onCreate();
        mMusic = new ArrayList<MediaMetadata>();
//Añadimos 3 canciones desde la librería de audio de youtube
        mMusic.add(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "https://www.youtube.com/audiolibrary_download?vid=f5cfb6bd8c048b98").putString(MediaMetadata.METADATA_KEY_TITLE, "Primera canción")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artista 1").putLong(MediaMetadata.METADATA_KEY_DURATION, 9).build());
        mMusic.add(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID,
                "https://www.youtube.com/audiolibrary_download?vid=ac7a38f4a568229c").putString(MediaMetadata.METADATA_KEY_TITLE, "Segunda canción")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artista 2").putLong(MediaMetadata.METADATA_KEY_DURATION, 65000).build());
        mMusic.add(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID,
                "https://www.youtube.com/audiolibrary_download?vid=456229530454affd").putString(MediaMetadata.METADATA_KEY_TITLE, "Tercera canción")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artista 3").putLong(MediaMetadata.METADATA_KEY_DURATION, 121000).build());
        mPlayer = new MediaPlayer();
        mSession = new MediaSession(this, "MiServicioMusical");
        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlayFromMediaId(String mediaId, Bundle extras) {
                temp=0;
                for (MediaMetadata item : mMusic) {

                    if (item.getDescription().getMediaId().equals(mediaId)) {
                        mCurrentTrack = item;
                        break;
                    }
                    temp=temp+1;

                }
                handlePlay();
            }

            @Override
            public void onPlay() {
                if (mCurrentTrack == null) {
                    mCurrentTrack = mMusic.get(0);
                    handlePlay();


                } else {
                    mPlayer.start();
                    mSession.setPlaybackState(buildState(PlaybackState.STATE_PLAYING));
                    Log.i("Duracion Tamaño","Termino el primero ");
                }
            }

            @Override
            public void onPause() {
                mPlayer.pause();
                mSession.setPlaybackState(
                        buildState(PlaybackState.STATE_PAUSED));
            }

            @Override
            public void onStop() {
                Log.i("Duracion Tamaño","Llamooooo STOP ");

            }


            @Override
            public void 	onSkipToNext(){
              /* if (temp<(mMusic.size()-1)){
                temp=temp+1;
                mCurrentTrack = mMusic.get(temp);
                handlePlay();}
*/
              siguientePista();

            }

            @Override
            public void onSkipToPrevious(){

                if (temp>0){
                    temp=temp-1;
                    mCurrentTrack = mMusic.get(temp);
                    handlePlay();}


            }





        });



        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);
        setSessionToken(mSession.getSessionToken());
    }

    public void siguientePista ()
    {
        if (temp<(mMusic.size()-1)){
            temp=temp+1;
            mCurrentTrack = mMusic.get(temp);
            handlePlay();}else {
            if (temp==(mMusic.size()-1)){
            temp=0;
                mCurrentTrack = mMusic.get(temp);
                handlePlay();
            }
        }


    }

    private PlaybackState buildState(int state) {
        return new PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY
                | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_PLAY_FROM_MEDIA_ID | PlaybackState.ACTION_PLAY_PAUSE
                | PlaybackState.ACTION_PAUSE).setState(state, mPlayer.getCurrentPosition(), 1, SystemClock.elapsedRealtime()).build();
    }

    private void handlePlay() {

        Log.i("Duracion", String.valueOf(mPlayer.getDuration()));


        mSession.setPlaybackState(buildState(PlaybackState.STATE_PLAYING));


        mSession.setMetadata(mCurrentTrack);
        try {
            mPlayer.reset();
            mPlayer.setDataSource(ServicioMusica.this,
                    Uri.parse(mCurrentTrack.getDescription().getMediaId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();


            }
        });
        mPlayer.seekTo(0);
        mPlayer.prepareAsync();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer){
         Log.i("Duracion","Se completo el audio");
         siguientePista();

            }
        } );
    }




    @Override
    public MediaBrowserService.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new MediaBrowserService.BrowserRoot("ROOT", null);
    }

    @Override
    public void onLoadChildren(String s,
                               Result<List<MediaBrowser.MediaItem>> result) {
        List<MediaBrowser.MediaItem> list = new
                ArrayList<MediaBrowser.MediaItem>();
        for (MediaMetadata m : mMusic) {
            list.add(new MediaBrowser.MediaItem(m.getDescription(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }
        result.sendResult(list);
    }

    @Override
    public void onDestroy() {
        mSession.release();
    }
}