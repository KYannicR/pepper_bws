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
import com.aldebaran.qi.sdk.builder.HolderBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.camera.Camera;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.holder.AutonomousAbilitiesType;
import com.aldebaran.qi.sdk.object.holder.Holder;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;

public class EntertainActivity extends RobotActivity implements RobotLifecycleCallbacks{
    private Button back, dance, five, hug;
    private TouchSensor handTouchSensor;
    private QiContext qiContext;
    boolean touched = false;
    Future<Void> highFiveFuture;
    private Animate highfiveAnim;
    private Holder holder;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_entertain);
        back = findViewById(R.id.back6);
        dance = findViewById(R.id.dance);
        five = findViewById(R.id.five);
        hug = findViewById(R.id.hug);
        initButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_entertain);
        back = findViewById(R.id.back6);
        dance = findViewById(R.id.dance);
        five = findViewById(R.id.five);
        hug = findViewById(R.id.hug);
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
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.handshakehold) // Set the animation resource.
                .build(); // Build the animation.
        highfiveAnim = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build();
        initHandTouchedListener();
    }

    @Override
    public void onRobotFocusLost() {
        if (handTouchSensor != null) {
            handTouchSensor.removeAllOnStateChangedListeners();
        }
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
                //sayText("High Five").async().run();
                Log.i("Five", "High Five initiated");
                highFiveFuture = highfiveAnim.async().run();

                highFiveFuture.thenConsume(future -> {
                    if (future.isSuccess()) {
                        sayText("Nagut, dann nicht.").async().run();

                    } else if (future.isCancelled()) {
                        sayText("Nice").async().run();

                    } else if (future.hasError()) {
                        Log.e("TAG", "Error", future.getError());

                    }
                });

            }
        });
        hug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Button Clicked");
            }
        });

    }

    private void initHandTouchedListener() {
        Touch touch = qiContext.getTouch();

        handTouchSensor = touch.getSensor("RHand/Touch");

        handTouchSensor.addOnStateChangedListener(touchState -> {
            Log.i("TOUCH", "Touch Noticed");
            highFive();

        });
    }

    private Say sayText(String text){
        return SayBuilder.with(qiContext)
                .withText(text)
                .build();
    }

    private void highFive() {
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.handshakeactive) // Set the animation resource.
                .build(); // Build the animation.
        Animate animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build();
        highFiveFuture.requestCancellation();
        highFiveFuture.cancel(true);
        animate.run();

    }

}
