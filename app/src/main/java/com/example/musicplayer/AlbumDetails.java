package com.example.musicplayer;

import static com.example.musicplayer.MainActivity.musicFiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {
RecyclerView recyclerView;
ImageView albumPhoto;
String albumName;
ArrayList<MusicFiles>albumsongs = new ArrayList<>();
AlbumDetailsAdapter albumDetailsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.album_activity_recycler_view);
        albumPhoto = findViewById(R.id.album_photo);
        albumName = getIntent().getStringExtra("albumName");
        for(int i = 0 ; i< musicFiles.size() ; i++){
            if(albumName.equals(musicFiles.get(i).getAlbum())){
                albumsongs.add(musicFiles.get(i));
            }
        }
        byte [] image = getAlbumArt(albumsongs.get(0).getPath());
        if(image != null){
            Glide.with(this).load(image).into(albumPhoto);
        }else{
            Glide.with(this).load(R.drawable.itunes).into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!(albumsongs.size()<1)){
            albumDetailsAdapter = new AlbumDetailsAdapter(albumsongs,this);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new CustomLayoutManager(this,RecyclerView.VERTICAL,false));
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