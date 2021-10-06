package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;

public class EntertainActivity extends RobotActivity implements RobotLifecycleCallbacks{
    private Button back, dance, five, hug;
    private TouchSensor handTouchSensor;
    private QiContext qiContext;
    boolean touched = false;
    Future<Void> highFiveFuture;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_entertain);
        back = findViewById(R.id.back);
        dance = findViewById(R.id.job1);
        five = findViewById(R.id.job5);
        hug = findViewById(R.id.job3);
        initButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_entertain);
        back = findViewById(R.id.back);
        dance = findViewById(R.id.job1);
        five = findViewById(R.id.job5);
        hug = findViewById(R.id.job3);
        initButtonListeners();
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        Touch touch = qiContext.getTouch();
        handTouchSensor = touch.getSensor("RHand/Touch");
        
    }

    @Override
    public void onRobotFocusLost() {
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        this.qiContext = null;
    }

    private void initButtonListeners() {
        dance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Button Clicked");
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Button Clicked");
                Intent activity2Intent = new Intent(EntertainActivity.this, MainActivity.class);
                startActivity(activity2Intent);
                finishAffinity();
            }
        });
        five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_speaking);
                //sayText("High Five").run();
                Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                        .withResources(R.raw.highfive) // Set the animation resource.
                        .build(); // Build the animation.
                Animate animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                        .withAnimation(animation) // Set the animation.
                        .build();

                handTouchSensor.addOnStateChangedListener(touchState -> {
                    if (touchState.getTouched()) {
                        highFive();
                        touched = true;
                    }
                });
                highFiveFuture = animate.async().run();

                highFiveFuture.thenConsume(future -> {
                    if (future.isSuccess()) {
                        sayText("Nagut dann nicht.").run();
                    } else if (future.isCancelled()) {
                        sayText("Nice").async().run();
                    } else if (future.hasError()) {
                        Log.e("TAG", "Error", future.getError());
                    }
                });
                handTouchSensor.removeAllOnStateChangedListeners();
            }
        });
        hug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Button Clicked");
            }
        });

    }

    private Say sayText(String text){
        return SayBuilder.with(qiContext)
                .withText(text)
                .build();
    }

    private void highFive() {
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.nicereaction_a002) // Set the animation resource.
                .build(); // Build the animation.
        Animate animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build();
        highFiveFuture.requestCancellation();
        animate.async().run();
    }
}
