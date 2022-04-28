package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.albums;
import static com.example.musicplayer.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AlbumsFragment extends Fragment {
    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;

    public AlbumsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.albums_fragment, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_for_album);
        recyclerView.setHasFixedSize(true);
        if(!(albums.size() < 1)){
            albumAdapter = new AlbumAdapter(albums,getContext());
            recyclerView.setAdapter(albumAdapter);
          //  recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        }

        return view;
    }
}