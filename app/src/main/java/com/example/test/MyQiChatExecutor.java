package com.example.test;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.BaseQiChatExecutor;

import java.util.List;

class MyQiChatExecutor extends BaseQiChatExecutor {
    private final QiContext qiContext;
    private Future<Void> animationFuture;

    MyQiChatExecutor(QiContext context) {
        super(context);
        this.qiContext = context;
    }

    @Override
    public void runWith(List<String> params) { //wird ausgeführt wenn ^execute in topic
        // This is called when execute is reached in the topic
        if (params == null || params.isEmpty()) {
            return;
        }
        String param = params.get(0);
        boolean async = true;
        if (params.size() >= 2 && params.get(1).equals("sync")) {
            async = false;
        }

        animate(qiContext, qiContext.getResources().getIdentifier(param, "raw", qiContext.getPackageName()), async);
    }

    @Override
    public void stop() {
        if (animationFuture != null) {
            animationFuture.cancel(true);
        }
    }

    private void animate(QiContext qiContext, int resource, boolean async) {
        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(resource) // Set the animation resource.
                .build(); // Build the animation.

        // Create an animate action.
        Animate animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build(); // Build the animate action.
        if (async) {
            animationFuture = animate.async().run();
            //try {
            //    animationFuture.get();
            //} catch (ExecutionException e) {     //macht async kaputt??
            //    Log.e("tag", e.getMessage());
            //}
        } else {
            animate.run();
        }
    }
}
