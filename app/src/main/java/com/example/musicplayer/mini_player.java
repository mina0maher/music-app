package com.example.musicplayer;


import static com.example.musicplayer.MainActivity.ARTIST_TO_FRAG;
import static com.example.musicplayer.MainActivity.PATH_TO_FRAG;
import static com.example.musicplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.example.musicplayer.MainActivity.SONG_TO_FRAG;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class mini_player extends Fragment {

    ImageView nextBtn, albumArt;
    TextView artistName ,songName;
    FloatingActionButton playPauseBtn;
    View view;
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
                songName.setText(SONG_TO_FRAG);
                artistName.setText(ARTIST_TO_FRAG);
            }
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}