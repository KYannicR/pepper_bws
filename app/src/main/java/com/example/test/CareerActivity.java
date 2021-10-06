package com.example.test;


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


public class CareerActivity extends RobotActivity implements RobotLifecycleCallbacks{
    private QiContext qiContext;
    private boolean cancelled = false;
    private int image;
    Button back, left, right;

    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_career);
        initListeners();
        setImage(R.drawable.benefits1);
        image = 1;

    }

    private void initListeners() {
        back = findViewById(R.id.back2);
        back.setOnClickListener(v -> {
            cancelled = true;
            Intent activity2Intent = new Intent(CareerActivity.this, MainActivity.class);
            startActivity(activity2Intent);
            finishAffinity();
        });

        left = findViewById(R.id.left);
        left.setOnClickListener(v -> {
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

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
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
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        this.qiContext = null;
    }

    private void runPresentation() throws InterruptedException {
        setImage(R.drawable.benefits1);
        sayText().run();
        Thread.sleep(10000);
        clearImage();
        setImage(R.drawable.benefits2);
        Thread.sleep(10000);
        clearImage();
        setImage(R.drawable.benefits3);
        Thread.sleep(10000);
        clearImage();
        if(!cancelled) {
            Intent activity2Intent = new Intent(CareerActivity.this, MainActivity.class);
            startActivity(activity2Intent);
            finishAffinity();
        }
    }

    private Say sayText(){
        return SayBuilder.with(qiContext)
                .withText("Das hier sind unsere Benefits!")
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
