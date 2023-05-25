package com.example.pepper_bws_backup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.util.Timer;
import java.util.TimerTask;

public class AboutUsActivity extends RobotActivity implements RobotLifecycleCallbacks{
    private Button back, backTeam, backImage, team, locations, bws;
    private QiContext qiContext;
    private boolean showingImage;
    private Timer timeoutTimer;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_aboutus);
        findViewById(R.id.buttonGrid).setVisibility(View.VISIBLE);
        findViewById(R.id.scrollTeam).setVisibility(View.GONE);
        findViewById(R.id.imageView).setVisibility(View.GONE);
        showingImage = false;
        back = findViewById(R.id.back3);
        team = findViewById(R.id.team);
        locations = findViewById(R.id.locations);
        bws = findViewById(R.id.bws);
        backTeam = findViewById(R.id.back4);
        backImage = findViewById(R.id.back5);
        initButtonListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_aboutus);
        findViewById(R.id.buttonGrid).setVisibility(View.VISIBLE);
        findViewById(R.id.scrollTeam).setVisibility(View.GONE);
        findViewById(R.id.imageView).setVisibility(View.GONE);
        showingImage = false;
        back = findViewById(R.id.back3);
        team = findViewById(R.id.team);
        locations = findViewById(R.id.locations);
        bws = findViewById(R.id.bws);
        backTeam = findViewById(R.id.back4);
        backImage = findViewById(R.id.back5);
        initButtonListeners();
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        if(timeoutTimer != null) cancelTimer();
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        startTimer();
        sayText().run();
    }

    @Override
    public void onRobotFocusLost() {
        if(timeoutTimer != null) cancelTimer();
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        this.qiContext = null;
    }

    private void initButtonListeners() {
        back.setOnClickListener(v -> {
            cancelTimer();
            Intent activity2Intent = new Intent(AboutUsActivity.this, MainActivity.class);
            startActivity(activity2Intent);
            finishAffinity();
        });

        backTeam.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            clearImage();
            findViewById(R.id.buttonGrid).setVisibility(View.VISIBLE);
            findViewById(R.id.scrollTeam).setVisibility(View.GONE);
            findViewById(R.id.imageView).setVisibility(View.GONE);
        });

        backImage.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            clearImage();
            findViewById(R.id.buttonGrid).setVisibility(View.VISIBLE);
            findViewById(R.id.scrollTeam).setVisibility(View.GONE);
            findViewById(R.id.imageView).setVisibility(View.GONE);
        });

        team.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            findViewById(R.id.buttonGrid).setVisibility(View.GONE);
            findViewById(R.id.scrollTeam).setVisibility(View.VISIBLE);
        });

        bws.setOnClickListener(v -> {
            cancelTimer();
            showingImage = true;
            startTimer();
            findViewById(R.id.buttonGrid).setVisibility(View.GONE);
            setImage(R.drawable.bws3);
        });

        locations.setOnClickListener(v -> {
            cancelTimer();
            showingImage = true;
            startTimer();
            findViewById(R.id.buttonGrid).setVisibility(View.GONE);
            setImage(R.drawable.standorte);
        });
    }

    private void cancelTimer() {
        Log.i("END", "TIMEOUT TIMER END");
        timeoutTimer.cancel();
        timeoutTimer.purge();
    }

    private void startTimer() {
        Log.i("START", "TIMEOUT TIMER START");
        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent activity2Intent = new Intent(AboutUsActivity.this, MainActivity.class);
                startActivity(activity2Intent);
                finishAffinity();
                clearImage();
                cancelTimer();
            }
        }, 90000, 90000);
    }

    private void hideButtons() {
        team.setVisibility(View.GONE);
        bws.setVisibility(View.GONE);
        locations.setVisibility(View.GONE);
    }

    private void showButtons() {
        team.setVisibility(View.VISIBLE);
        bws.setVisibility(View.VISIBLE);
        locations.setVisibility(View.VISIBLE);
    }

    private Say sayText(){
        return SayBuilder.with(qiContext)
                .withText("Was möchtest du über uns wissen?")
                .build();
    }

    private void setImage(Integer resource){
        runOnUiThread(() -> {
            ImageView test = findViewById(R.id.splashImageViewAbout);
            test.setImageResource(resource);
            findViewById(R.id.imageView).setVisibility(View.VISIBLE);
            test.setVisibility(View.VISIBLE);
        });
    }

    private void clearImage(){
        runOnUiThread(() -> {
            ImageView test = findViewById(R.id.splashImageViewAbout);
            test.setVisibility(View.GONE);
            findViewById(R.id.imageView).setVisibility(View.GONE);
        });
    }

}
