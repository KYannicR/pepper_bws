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

public class JobsActivity extends RobotActivity implements RobotLifecycleCallbacks{
    private Button back, job1, job2, job3, job4, job5;
    private QiContext qiContext;
    private boolean jobs_active;
    private Timer timeoutTimer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_jobs);
        jobs_active = true;
        back = findViewById(R.id.back);
        job1 = findViewById(R.id.job1);
        job2 = findViewById(R.id.job2);
        job3 = findViewById(R.id.job3);
        job4 = findViewById(R.id.job4);
        job5 = findViewById(R.id.job5);
        initButtonListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_jobs);
        jobs_active = true;
        back = findViewById(R.id.back);
        job1 = findViewById(R.id.job1);
        job2 = findViewById(R.id.job2);
        job3 = findViewById(R.id.job3);
        job4 = findViewById(R.id.job4);
        job5 = findViewById(R.id.job5);
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

            if(jobs_active) {
                Intent activity2Intent = new Intent(JobsActivity.this, MainActivity.class);
                startActivity(activity2Intent);
                finishAffinity();
            } else {
                clearImage();
                showButtons();
                jobs_active = true;
            }
        });

        job1.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            jobs_active = false;
            hideButtons();
            setImage(R.drawable.java);
        });

        job2.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            jobs_active = false;
            hideButtons();
            setImage(R.drawable.juniorj);
        });

        job3.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            jobs_active = false;
            hideButtons();
            setImage(R.drawable.seniorj);
        });

        job4.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            jobs_active = false;
            hideButtons();
            setImage(R.drawable.dotnet);
        });

        job5.setOnClickListener(v -> {
            cancelTimer();
            startTimer();
            jobs_active = false;
            hideButtons();
            setImage(R.drawable.student);
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
                Intent activity2Intent = new Intent(JobsActivity.this, MainActivity.class);
                startActivity(activity2Intent);
                finishAffinity();
                clearImage();
                cancelTimer();
            }
        }, 90000, 90000);
    }

    private void hideButtons() {
        job1.setVisibility(View.GONE);
        job2.setVisibility(View.GONE);
        job3.setVisibility(View.GONE);
        job4.setVisibility(View.GONE);
        job5.setVisibility(View.GONE);
    }

    private void showButtons() {
        job1.setVisibility(View.VISIBLE);
        job2.setVisibility(View.VISIBLE);
        job3.setVisibility(View.VISIBLE);
        job4.setVisibility(View.VISIBLE);
        job5.setVisibility(View.VISIBLE);
    }

    private Say sayText(){
        return SayBuilder.with(qiContext)
                .withText("Hier siehst du unsere aktuellen Stellenangebote, klick einfach eins an, um mehr zu erfahren!")
                .build();
    }

    private void setImage(Integer resource){
        runOnUiThread(() -> {
            ImageView test = findViewById(R.id.splashImageView);
            test.setImageResource(resource);
            test.setVisibility(View.VISIBLE);
        });
    }

    private void clearImage(){
        runOnUiThread(() -> {
            ImageView test = findViewById(R.id.splashImageView);
            test.setVisibility(View.GONE);
        });
    }

}
