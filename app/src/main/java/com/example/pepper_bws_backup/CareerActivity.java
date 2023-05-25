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

public class CareerActivity extends RobotActivity implements RobotLifecycleCallbacks{
    private QiContext qiContext;
    private boolean cancelled = false;
    private int image;
    Button back, left, right;
    private Timer timeoutTimer;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_career);
        startTimer();
        initListeners();
        setImage(R.drawable.benefits1);
        image = 1;

    }

    private void initListeners() {
        back = findViewById(R.id.back2);
        back.setOnClickListener(v -> {
            cancelTimer();
            cancelled = true;
            Intent activity2Intent = new Intent(CareerActivity.this, MainActivity.class);
            startActivity(activity2Intent);
            finishAffinity();
        });

        left = findViewById(R.id.left);
        left.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            switch(image){
                case 1:
                    clearImage();
                    setImage(R.drawable.benefits3);
                    image = 3;
                    break;
                case 2:
                    clearImage();
                    setImage(R.drawable.benefits1);
                    image = 1;
                    break;
                case 3:
                    clearImage();
                    setImage(R.drawable.benefits2);
                    image = 2;
                    break;
            }
        });

        right = findViewById(R.id.right);
        right.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            switch(image){
                case 1:
                    clearImage();
                    setImage(R.drawable.benefits2);
                    image = 2;
                    break;
                case 2:
                    clearImage();
                    setImage(R.drawable.benefits3);
                    image = 3;
                    break;
                case 3:
                    clearImage();
                    setImage(R.drawable.benefits1);
                    image = 1;
                    break;
            }
        });
    }
    private void cancelTimer() {
        timeoutTimer.cancel();
        timeoutTimer.purge();
    }

    private void startTimer() {
        timeoutTimer = new Timer();
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cancelled = true;
                Intent activity2Intent = new Intent(CareerActivity.this, MainActivity.class);
                startActivity(activity2Intent);
                finishAffinity();
                cancelTimer();
            }
        }, 90000, 90000);
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
        Log.d("gain", "onRobotFocusGained");
        this.qiContext = qiContext;
        sayText().run();
        /*try {
            runPresentation();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
        if(timeoutTimer != null) cancelTimer();
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        this.qiContext = null;
    }

    private Say sayText(){
        return SayBuilder.with(qiContext)
                .withText("Das hier sind unsere Benefits!")
                .build();
    }

    private void setImage(Integer resource){
        runOnUiThread(() -> {
            ImageView test = findViewById(R.id.splashImageView2);
            test.setImageResource(resource);
            test.setVisibility(View.VISIBLE);
        });
    }

    private void clearImage(){
        runOnUiThread(() -> {
            ImageView test = findViewById(R.id.splashImageView2);
            test.setVisibility(View.GONE);
        });
    }


}
