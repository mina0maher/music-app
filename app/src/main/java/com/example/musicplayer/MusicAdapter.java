package com.example.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {
    private ArrayList<MusicFiles> List = new ArrayList<>();
Context context;
    @NonNull
    @Override
    public MusicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new MusicHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicHolder holder, int position) {
        holder.fileName.setText(List.get(position).getTitle());
        byte[] image = getAlbumArt(List.get(position).getPath());
        if (image != null) {
            Glide.with(context).asBitmap().load(image).into(holder.albumArt);
        } else {
            Glide.with(context).load(R.drawable.itunes).into(holder.albumArt);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,PlayerActivity.class);
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(context,view);
                popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item)-> {
                    switch(item.getItemId()){
                        case R.id.delete:
                            Toast.makeText(context,"deleted !",Toast.LENGTH_SHORT).show();
                            deleteFile(position,view);
                            break;
                    }
                    return true;
                });
            }
        });
    }

    private void deleteFile(int position, View view) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        , Long.parseLong(List.get(position).getId()));
        File file = new File(List.get(position).getPath());
        boolean deleted = file.delete();
        if(deleted) {
            context.getContentResolver().delete(contentUri,null,null);
            List.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, List.size());
            Snackbar.make(view, "file deleted : ", Snackbar.LENGTH_SHORT).show();
        }else{
            Snackbar.make(view, "file can't be deleted : ", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return List.size();
    }

    public void setList(ArrayList<MusicFiles> List) {
        this.List = List;
        notifyDataSetChanged();
    }

    public class MusicHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        ImageView albumArt,menuMore;
        public MusicHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.music_file_name);
            albumArt = itemView.findViewById(R.id.music_image);
            menuMore = itemView.findViewById(R.id.menu_more);
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
