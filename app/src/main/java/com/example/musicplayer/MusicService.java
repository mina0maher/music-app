package com.example.musicplayer;

import static com.example.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.musicplayer.PlayerActivity.listSongs;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mBinder = new MyBinder();

    static MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying ;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    private static final String MUSIC_FILE = "STORED" ;
    public static final String ARTIST_NAME = "ARTIST";
    private static final String SONG_NAME = "SONG" ;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        if(myPosition!=-1){
            playMedia(myPosition);
        }

        if(actionName != null){
            switch (actionName){
                case "playPause":

                    if(actionPlaying != null){
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":

                    if(actionPlaying != null){
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":

                    if(actionPlaying != null){
                        actionPlaying.prevBtnClicked();
                    }
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listSongs;
        position = startPosition;
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }else{
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start(){
        mediaPlayer.start();
    }
    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    void stop(){
        mediaPlayer.stop();
    }
    void release(){
        mediaPlayer.release();
    }
    void pause(){
        mediaPlayer.pause();
    }
    int getDuration(){
        return mediaPlayer.getDuration();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int currposition){
        position = currposition;
        uri = Uri.parse( musicFiles.get(currposition).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE,uri.toString());
        editor.putString(ARTIST_NAME,musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME,musicFiles.get(position).getTitle());
        editor.apply();

        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);

    }
    void OnCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
if(actionPlaying!=null) {
    actionPlaying.nextBtnClicked();
    if(mediaPlayer == null){
       // createMediaPlayer(position);
       // mediaPlayer.start();
        OnCompleted();
    }
}



    }
    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
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
        picture = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }else{
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.itunes);
        }
        int maxProgress = Integer.parseInt(musicFiles.get(position).getDuration())/1000;
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2).setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.icon_skip_previous,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.icon_skip_next,"next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent)

               //.setProgress(maxProgress,getCurrentPosition()/1000,false)
                .build();
       // NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
       // notificationManager.notify(0,notification.build());
        startForeground(1, notification);





//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if(mediaPlayer.isPlaying()){
//            showNotification(R.drawable.icon_pause);
//        }

    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

}
