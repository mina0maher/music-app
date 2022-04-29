package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class SongsFragment extends Fragment {

RecyclerView recyclerView;
static MusicAdapter musicAdapter;
    public SongsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.songs_fragment, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        if(!(musicFiles.size() < 1)){
            musicAdapter = new MusicAdapter();
            musicAdapter.setmFiles(musicFiles);
            recyclerView.setAdapter(musicAdapter);

          //  recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setLayoutManager(new CustomLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        }

        return view;
    }
}