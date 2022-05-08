package com.example.musicplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
}