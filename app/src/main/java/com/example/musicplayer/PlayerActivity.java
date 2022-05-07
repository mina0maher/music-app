package com.example.musicplayer;

import static com.example.musicplayer.AlbumDetailsAdapter.list;
import static com.example.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayer.ApplicationClass.CHANNEL_ID_2;

import static com.example.musicplayer.MainActivity.repeatBoolean;
import static com.example.musicplayer.MainActivity.shuffleBoolean;
import static com.example.musicplayer.MusicAdapter.mFiles;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.palette.graphics.Palette;

import android.app.PendingIntent;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;

    static Uri uri;
    static ArrayList<MusicFiles> listSongs= new ArrayList<>();
   // static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread,prevThread, nextThread;
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat  ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_DayNight_NoActionBar);
        setContentView(R.layout.activity_player);
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext() , "My Audio");
        initView();
        getIntentMethod();
        if(repeatBoolean){
            repeatBtn.setImageResource(R.drawable.icon_repeat_on);
        }else{
            repeatBtn.setImageResource(R.drawable.icon_repeat_off);
        }
        if(shuffleBoolean){
            shuffleBtn.setImageResource(R.drawable.icon_shuffle_on);
        }else{
            shuffleBtn.setImageResource(R.drawable.icon_shuffle_off);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService!=null&&fromUser){
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shuffleBoolean){
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.icon_shuffle_off);
                }else{
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.icon_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeatBoolean){
                    repeatBoolean=false;
                    repeatBtn.setImageResource(R.drawable.icon_repeat_off);
                }else{
                    repeatBoolean=true;
                    repeatBtn.setImageResource(R.drawable.icon_repeat_on);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if (musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean&&!repeatBoolean){
                Random random = new Random();
                position = random.nextInt(listSongs.size());
            }else if (!shuffleBoolean&&!repeatBoolean){
                position = ((position-1)<0?(listSongs.size()-1):(position-1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.icon_pause);
            playPauseBtn.setBackgroundResource(R.drawable.icon_pause);
            musicService.start();
        }else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean&&!repeatBoolean){
                Random random = new Random();
                position = random.nextInt(listSongs.size());
            }else if (!shuffleBoolean&&!repeatBoolean){
                position = ((position-1)<0?(listSongs.size()-1):(position-1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.icon_play);
            playPauseBtn.setBackgroundResource(R.drawable.icon_play);
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean&&!repeatBoolean){
                Random random = new Random();
                position = random.nextInt(listSongs.size());
            }else if (!shuffleBoolean&&!repeatBoolean){
                position = ((position+1)%listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.icon_pause);
            playPauseBtn.setBackgroundResource(R.drawable.icon_pause);
            musicService.start();
        }else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean&&!repeatBoolean){
                Random random = new Random();
                position = random.nextInt(listSongs.size());
            }else if (!shuffleBoolean&&!repeatBoolean){
                position = ((position+1)%listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.setPosition(position);
            musicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.icon_play);
            playPauseBtn.setBackgroundResource(R.drawable.icon_play);
          musicService.start();
        }
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if(musicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.icon_play);
            showNotification(R.drawable.icon_play);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }else{
            showNotification(R.drawable.icon_pause);
            playPauseBtn.setImageResource(R.drawable.icon_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }

    private String formattedTime(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition%60);
        String minutes = String.valueOf(mCurrentPosition/60);
        totalOut= minutes + " : " +seconds;
        totalNew = minutes +" : 0"+seconds;
        if (seconds.length() == 1 ){
            return totalNew;
        }else{
            return totalOut;
        }
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position",-1);
        String sender = getIntent().getStringExtra("sender");
        if(sender!= null&&sender.equals("albumDetails")){
            listSongs = list;
        }else {
            listSongs = mFiles;
        }
        if(listSongs!=null){
            playPauseBtn.setImageResource(R.drawable.icon_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        showNotification(R.drawable.icon_pause);
       Intent  intent= new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);


    }

    private void initView() {
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.song_artist);
        durationPlayed = findViewById(R.id.duration_played);
        durationTotal = findViewById(R.id.duration_total);

        coverArt = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_previous);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);

        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seek_bar);
    }

    private void metaData(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int dTotal = Integer.parseInt(listSongs.get(position).getDuration())/1000;
        durationTotal.setText(formattedTime(dTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art != null){
            bitmap = BitmapFactory.decodeByteArray(art,0,art.length);
            imageAnimation(this,coverArt,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if(swatch != null){
                        ImageView gradient = findViewById(R.id.image_view_gradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        songName.setTextColor(swatch.getTitleTextColor());
                        artistName.setTextColor(swatch.getBodyTextColor());
                    }else{
                        ImageView gradient = findViewById(R.id.image_view_gradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        songName.setTextColor(Color.WHITE);
                        artistName.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }else{
            Glide.with(getApplicationContext()).asBitmap().load(R.drawable.itunes).into(coverArt);
            ImageView gradient = findViewById(R.id.image_view_gradient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            songName.setTextColor(Color.WHITE);
            artistName.setTextColor(Color.DKGRAY);
        }
    }
    public void imageAnimation(Context context,ImageView imageView,Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(getApplicationContext()).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }



    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration()/1000);
        metaData(uri);
        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        musicService.OnCompleted();

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }
    void showNotification(int playPauseBtn){
        Intent intent =new Intent(this,PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent
                .getActivity(this,0,intent,0);

///////////////////////////////////////////////////////////////////////////
        Intent prevIntent =new Intent(this,NotifcationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent
                .getBroadcast(this,0,prevIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
///////////////////////////////////////////////////////////////////////////
        Intent pauseIntent =new Intent(this,NotifcationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent
                .getBroadcast(this,0,pauseIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
//////////////////////////////////////////////////////////////////////////
        Intent nextIntent =new Intent(this,NotifcationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent
                .getBroadcast(this,0,nextIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
/////////////////////////////////////////////////////////////////////////
        byte [] picture = null;
        picture = getAlbumArt(listSongs.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }else{
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.itunes);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,CHANNEL_ID_2).setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(listSongs.get(position).getTitle())
                .setContentText(listSongs.get(position).getArtist())
                .addAction(R.drawable.icon_skip_previous,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.icon_skip_next,"next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
               .setContentIntent(contentIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0,notification.build());
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}