package com.example.automediabasico;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ServicioMusica extends MediaBrowserService {
    private MediaSession mSession;
    private List<MediaMetadata> mMusic;
    private MediaPlayer mPlayer;
    private MediaMetadata mCurrentTrack;
    private int temp;
    private RequestQueue requestQueue;
    private Gson gson;
    public Musica musica;


    private final String TAG = ServicioMusica.this.getClass().getSimpleName();
    private final String URL = "http://storage.googleapis.com/automotive-media/music.json";


    @Override
    public void onCreate() {
        super.onCreate();

        requestQueue = Volley.newRequestQueue(this);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();


        getRepositorioMusical();

        mMusic = new ArrayList<MediaMetadata>();
//Añadimos 3 canciones desde la librería de audio de youtube
/*        mMusic.add(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "https://www.youtube.com/audiolibrary_download?vid=f5cfb6bd8c048b98").putString(MediaMetadata.METADATA_KEY_TITLE, "Primera canción")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artista 1").putLong(MediaMetadata.METADATA_KEY_DURATION, 59000).build());
       mMusic.add(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID,
                "https://www.youtube.com/audiolibrary_download?vid=ac7a38f4a568229c").putString(MediaMetadata.METADATA_KEY_TITLE, "Segunda canción")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artista 2").putLong(MediaMetadata.METADATA_KEY_DURATION, 65000).build());
        mMusic.add(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID,
                "https://www.youtube.com/audiolibrary_download?vid=456229530454affd").putString(MediaMetadata.METADATA_KEY_TITLE, "Tercera canción")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artista 3").putLong(MediaMetadata.METADATA_KEY_DURATION, 121000).build());

*/
        mPlayer = new MediaPlayer();
        mSession = new MediaSession(ServicioMusica.this, "MiServicioMusical");
        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlayFromMediaId(String mediaId, Bundle extras) {
                temp = 0;
                for (MediaMetadata item : mMusic) {

                    if (item.getDescription().getMediaId().equals(mediaId)) {
                        mCurrentTrack = item;
                        break;
                    }
                    temp = temp + 1;

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


                    Log.i("Duracion Tamaño", "Termino el primero ");
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
                mPlayer.pause();
                Log.i("Duracion Tamaño", "Llamooooo STOP ");
                mPlayer.seekTo(0);
            }

            @Override
            public void onSeekTo(long pos) {
                mPlayer.seekTo((int) pos);
            }

            @Override
            public void onSkipToNext() {
              /* if (temp<(mMusic.size()-1)){
                temp=temp+1;
                mCurrentTrack = mMusic.get(temp);
                handlePlay();}
*/
                siguientePista();

            }

            @Override
            public void onSkipToPrevious() {

                if (temp > 0) {
                    temp = temp - 1;
                    mCurrentTrack = mMusic.get(temp);
                    handlePlay();
                }


            }


        });


        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);

        setSessionToken(mSession.getSessionToken());


    }

    public void siguientePista() {
        if (temp < (mMusic.size() - 1)) {
            temp = temp + 1;
            mCurrentTrack = mMusic.get(temp);
            handlePlay();
        } else {
            if (temp == (mMusic.size() - 1)) {
                temp = 0;
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

//
        Log.i("Duracion", String.valueOf(mPlayer.getDuration()));


        mSession.setPlaybackState(buildState(PlaybackState.STATE_PLAYING));





        mSession.setMetadata(mCurrentTrack);
        try {

            mPlayer.seekTo(0);
            mPlayer.reset();
            mPlayer.setDataSource(ServicioMusica.this,
                    Uri.parse(mCurrentTrack.getDescription().getMediaId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

                Log.i("Buffered ", String.valueOf( mPlayer.isPlaying()));


                Log.i("Buffe Reprodu",String.valueOf(mPlayer.getCurrentPosition()));


            }
        });
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayer.seekTo(0);
                mediaPlayer.start();


            }
        });

        mPlayer.prepareAsync();




        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                siguientePista();

            }
        });
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

    private void getRepositorioMusical() {
        Log.i("Entro por aqui ", "LLego");
        StringRequest request = new StringRequest(Request.Method.GET, URL, onPostsLoaded, onPostsError);

        requestQueue.add(request);
    }


    private final Response.Listener<String> onPostsLoaded =
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    musica = gson.fromJson(response, Musica.class);
                    Log.d(TAG, "Numero de pistas de audio: " + musica.getMusica().size());

                    int slashPos = URL.lastIndexOf('/');
                    String path = URL.substring(0, slashPos + 1);
                    for (int i = 0; i < musica.getMusica().size(); i++) {
                        PistaAudio pista = musica.getMusica().get(i);
                        if (!pista.getSource().startsWith("http"))
                            pista.setSource(path + pista.getSource());
                        if (!pista.getImage().startsWith("http"))
                            pista.setImage(path + pista.getImage());
                        musica.getMusica().set(i, pista);
                    }


                    for (int i = 0; i < musica.getMusica().size(); i++) {
                        PistaAudio pista = musica.getMusica().get(i);

                        Log.i("Las pistas...", pista.getSource() + " " + pista.getTitle() + " " + pista.getArtist() + " " + pista.getDuration());


                        mMusic.add(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID, pista.getSource()).putString(MediaMetadata.METADATA_KEY_TITLE, pista.getTitle()).putString(MediaMetadata.METADATA_KEY_ARTIST, pista.getArtist()).putLong(MediaMetadata.METADATA_KEY_DURATION, pista.getDuration()*1000).putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, pista.getImage()).build());

                    }


                }


            };
    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
        }
    };


}