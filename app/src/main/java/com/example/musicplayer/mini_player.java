package com.example.musicplayer;


import static android.content.Context.MODE_PRIVATE;
import static com.example.musicplayer.MainActivity.ARTIST_TO_FRAG;
import static com.example.musicplayer.MainActivity.PATH_TO_FRAG;
import static com.example.musicplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.example.musicplayer.MainActivity.SONG_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class mini_player extends Fragment implements ServiceConnection {

    ImageView nextBtn, albumArt;
    TextView artistName ,songName;
    FloatingActionButton playPauseBtn;
    View view;
    MusicService musicService ;
    RelativeLayout relativeLayout;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    private static final String MUSIC_FILE = "STORED" ;
    public static final String ARTIST_NAME = "ARTIST";
    private static final String SONG_NAME = "SONG" ;
    public mini_player() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        artistName = view.findViewById(R.id.song_artist_mini);
        songName = view.findViewById(R.id.song_name_mini);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        albumArt = view.findViewById(R.id.bottom_album_art);
        playPauseBtn = view.findViewById(R.id.play_pause_mini);
        relativeLayout = view.findViewById(R.id.card_bottom_player);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(musicService!=null){
                    musicService.nextBtnClicked();
                    if(getActivity()!=null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();

                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String pathh = preferences.getString(MUSIC_FILE,null);
                        String artistt = preferences.getString(ARTIST_NAME,null);
                        String songg = preferences.getString(SONG_NAME,null);
                        if(pathh != null){
                            SHOW_MINI_PLAYER =true;
                            SONG_TO_FRAG = songg;
                            ARTIST_TO_FRAG = artistt;
                            PATH_TO_FRAG = pathh;
                        }else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            SONG_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            PATH_TO_FRAG = null;
                        }
                        if(SHOW_MINI_PLAYER){
                            if(PATH_TO_FRAG!=null) {
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if(art!=null){
                                    Glide.with(getContext()).load(art).into(albumArt);
                                }else{
                                    Glide.with(getContext()).load(R.drawable.itunes).into(albumArt);
                                }
                                songName.setText(SONG_TO_FRAG);
                                artistName.setText(ARTIST_TO_FRAG);
                            }
                        }
                    }
               }
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View view) {
                    musicService.playPauseBtnClicked();
                    if(musicService.isPlaying()){
                        playPauseBtn.setImageResource(R.drawable.icon_pause);
                    }else{
                        playPauseBtn.setImageResource(R.drawable.icon_play);
                    }
            }
        });
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),PlayerActivity.class);
                intent.putExtra("position",musicService.position);
                intent.putExtra("duration",musicService.getCurrentPosition());
                getContext().startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER){
            if(PATH_TO_FRAG!=null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art!=null){
                    Glide.with(getContext()).load(art).into(albumArt);
                }else{
                    Glide.with(getContext()).load(R.drawable.itunes).into(albumArt);
                }
                playPauseBtn.setImageResource(R.drawable.icon_pause);
                songName.setText(SONG_TO_FRAG);
                artistName.setText(ARTIST_TO_FRAG);
                Intent intent = new Intent(getContext(),MusicService.class);
                if(getContext() !=null){
                    getContext().bindService(intent,this, Context.BIND_AUTO_CREATE);
                }
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext()!=null){
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder binder = (MusicService.MyBinder)iBinder;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
         musicService = null;
    }
}