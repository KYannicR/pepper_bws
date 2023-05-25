package com.example.pepper_bws_backup;

import android.media.MediaPlayer;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;

import java.util.List;

class MyQiChatSound extends BaseQiChatExecutor {
    private final QiContext qiContext;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    MyQiChatSound(QiContext context) {
        super(context);
        this.qiContext = context;
    }

    @Override
    public void runWith(List<String> params) { //wird ausgeführt wenn ^execute in topic
        String param = params.get(0);
        play(qiContext, qiContext.getResources().getIdentifier(param, "raw", qiContext.getPackageName()));
        //mediaPlayer.reset();
        //mediaPlayer = MediaPlayer.create(qiContext, R.raw.music1);
        //mediaPlayer.start();
    }

    @Override
    public void stop() {
        mediaPlayer.reset();
    }

    private void play(QiContext qiContext, int file) {
        // Create an animation.
        //private void playMedia(Integer mediaResource) {
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(qiContext, file);
        mediaPlayer.start();

    }
}
