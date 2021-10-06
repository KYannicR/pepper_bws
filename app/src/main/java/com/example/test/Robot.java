package com.example.test;

import android.media.MediaPlayer;
import android.util.Log;
import android.util.TypedValue;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionImportance;
import com.aldebaran.qi.sdk.object.conversation.AutonomousReactionValidity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatExecutor;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.TransformTime;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.object.touch.TouchSensor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Robot implements RobotLifecycleCallbacks {
    private Chat chat;
    private QiContext qiContext;
    private QiChatbot qiChatbot;
    private TouchSensor headTouchSensor;
    private Bookmark proposalBookmark;
    private List<Double> ranges = new ArrayList<>();
    private HumanAwareness humanAwareness;
    private int humans;
    private boolean attractionZone = false;
    private boolean proactiveZone = false;
    public Timer solitaryTimer;
    public Timer attractionTimer;
    public Timer proactiveTimer;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Animate tickleAnim1;
    private Animate tickleAnim2;
    public final Lock animLock = new ReentrantLock();

    public Robot() {}

    @Override
    public void onRobotFocusGained(QiContext theContext) {
        Log.d("gain", "onRobotFocusGained");
        this.qiContext = theContext;
        runChat();

        Animation tickle1 = AnimationBuilder.with(qiContext).withResources(R.raw.tickling_a001).build();
        Animation tickle2 = AnimationBuilder.with(qiContext).withResources(R.raw.tickling_a002).build();
        tickleAnim1 = AnimateBuilder.with(qiContext).withAnimation(tickle1).build();
        tickleAnim2 = AnimateBuilder.with(qiContext).withAnimation(tickle2).build();

        // Get the Touch service from the QiContext.
        Touch touch = qiContext.getTouch();
        headTouchSensor = touch.getSensor("Head/Touch");
        headTouchSensor.addOnStateChangedListener(touchState -> {
            if (touchState.getTouched()) {
                tickle();
            }
        });


        //chat.addOnSayingChangedListener(sayingPhrase -> {
            /*// Hide Buttons while speaking
            qiContext.runOnUiThread(new Runnable() {
                public void run() {
                    textViewPrograss.setText(finalI + "");
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.activity_speaking);
                }
            });*/

        //});

        //chat.addOnHearingChangedListener(hearing -> {
            /*//show Buttons while hearing
            if(hearing.equals(true)) {
                setContentView(R.layout.activity_main);
            }*/
        //});


        startTimers();
    }

    private void tickle() {
        animLock.lock();
        try {
            headTouchSensor.removeAllOnStateChangedListeners();
            double x = (int)(Math.random()*((3-1)+1))+1;
            int y = (int)x;
            switch (y) {
                case 1:
                    playMedia(R.raw.laugh);
                    tickleAnim1.run();
                    break;
                case 2:
                    playMedia(R.raw.laugh2);
                    tickleAnim1.run();
                    break;
                case 3:
                    playMedia(R.raw.laugh3);
                    tickleAnim2.run();
                    break;
                case 4:
                    playMedia(R.raw.laugh);
                    tickleAnim2.run();
                    break;
            }
        } finally {
            animLock.unlock();
            headTouchSensor.addOnStateChangedListener(touchState -> {
                if(touchState.getTouched()) {
                    tickle();
                }
            });
        }

    }

    @Override
    public void onRobotFocusLost() {
        stopTimers();

        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
        if (headTouchSensor != null) {
            headTouchSensor.removeAllOnStateChangedListeners();
        }
        if (humanAwareness != null) {
            humanAwareness.removeAllOnHumansAroundChangedListeners();
        }
        if (tickleAnim1 != null) {
            tickleAnim1.removeAllOnStartedListeners();
        }
        if (tickleAnim2 != null) {
            tickleAnim2.removeAllOnStartedListeners();
        }
        mediaPlayer.release();
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        stopTimers();
        // The robot focus is refused.
    }

    private void setExecutor() {
        Map<String, QiChatExecutor> executors = new HashMap<>();
        // Map the executor name from the topic to our qiChatbotExecutor
        executors.put("launchAnimation", new MyQiChatExecutor(qiContext));
        // Set the executors to the qiChatbot
        qiChatbot.setExecutors(executors);
    }

    private void runChat() {
        Log.d("runChat", "runChat()");
        // Create chatbots
        qiChatbot = createQiChatbot();
        setExecutor();
        // Create the chat from its chatbots
        chat = ChatBuilder.with(qiContext)
                .withChatbot(qiChatbot)
                .build();
        chat.async().run();
    }

    private QiChatbot createQiChatbot() {
        return QiChatbotBuilder.with(qiContext)
                .withTopics(getTopics())
                .build();
    }

    private List<Topic> getTopics() {
        List<Topic> topics = new ArrayList<>();
        for (Field r : R.raw.class.getFields()) {
            try {
                TypedValue value = new TypedValue();
                qiContext.getResources().getValue(r.getInt(r), value, true);
                if (value.string.toString().endsWith(".top")) {
                    topics.add(TopicBuilder.with(qiContext)
                            .withResource(r.getInt(r))
                            .build());
                }
            } catch (IllegalAccessException e) {
                Log.i("TAG", e.getMessage());
            }
        }
        return topics;
    }

    private void speak(String topicName) {
        Topic topic;
        if(topicName.equals("proactive")) {
            topic  = TopicBuilder.with(qiContext).withResource(R.raw.proactive).build();
        } else if (topicName.equals("attraction")) {
            topic  = TopicBuilder.with(qiContext).withResource(R.raw.attraction).build();
        } else {
            topic = TopicBuilder.with(qiContext).withResource(R.raw.solitary).build();
        }
        Map<String, Bookmark> bookmarks = topic.getBookmarks();
        Object rndKey;
        rndKey = getRndKey(bookmarks);
        proposalBookmark = bookmarks.get(rndKey);
        sayProposal();
    }

    private void sayProposal() {
        qiChatbot.goToBookmark(proposalBookmark, AutonomousReactionImportance.LOW, AutonomousReactionValidity.DELAYABLE);
    }

    private int findHumansAround() throws ExecutionException {
        // Here we will find the humans around the robot.
        // Get the humans around the robot.
        Future<List<Human>> humansAroundFuture = humanAwareness.async().getHumansAround();
        humansAroundFuture.andThenConsume(humansAround -> {
            Log.i("TAG", humansAround.size() + " human(s) around.");
            retrieveCharacteristics(humansAround);
        });
        List<Human> humanCount = humansAroundFuture.get();
        return humanCount.size();
    }

    private void retrieveCharacteristics(final List<Human> humans) {
        Actuation actuation = qiContext.getActuation();

        // Get the robot frame.
        Frame robotFrame = actuation.robotFrame();
        for (int i = 0; i < humans.size(); i++) {
            // Get the human.
            Human human = humans.get(i);

            // Get the characteristics.
            Integer age = human.getEstimatedAge().getYears();
            Gender gender = human.getEstimatedGender();
            ExcitementState excitementState = human.getEmotion().getExcitement();
            Frame humanFrame = human.getHeadFrame();

            // Compute the distance.
            double distance = computeDistance(humanFrame, robotFrame);
            ranges.add(distance);

            Log.i("TAG", "Age: " + age + " year(s)");
            Log.i("TAG", "Gender: " + gender);
            Log.i("TAG", "Distance: " + distance + " meter(s).");
            Log.i("TAG", "Excitement state: " + excitementState);

        }
    }

    private double computeDistance(Frame humanFrame, Frame robotFrame) {
        // Get the TransformTime between the human frame and the robot frame.
        TransformTime transformTime = humanFrame.computeTransform(robotFrame);
        // Get the transform.
        Transform transform = transformTime.getTransform();
        // Get the translation.
        Vector3 translation = transform.getTranslation();
        // Get the x and y components of the translation.
        double x = translation.getX();
        double y = translation.getY();
        // Compute the distance and return it.
        return Math.sqrt(x * x + y * y);
    }

    private boolean testProactive(List<Double> ranges){
        for (Double curr : ranges) {
            if (curr < 1.5) return true;
        }
        return false;
    }

    private boolean testAttraction(List<Double> ranges){
        for (Double curr : ranges) {
            if (curr < 2.5 && curr > 1.5) return true;
        }
        return false;
    }

    private Object getRndKey(Map<String, Bookmark> bookmarks){
        Object[] keys = bookmarks.keySet().toArray();
        Object rndKey;
        List<Object> keysList = Arrays.asList(keys);
        Collections.shuffle(keysList);
        keysList.toArray(keys);
        rndKey = keys[0];
        return rndKey;
    }

    private void playMedia(Integer mediaResource) {
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(qiContext, mediaResource);
        mediaPlayer.start();

    }

/*    public static void startCareer() {
        new Robot().pauseTimers();
        System.out.println("paused?");
        //new Robot().startTimers();
    }*/

/*    public static void startEntertainment() {
        pauseTimers();
        System.out.println("paused?");
        //new Robot().startTimers();
    }*/

    public void stopTimers() {
        solitaryTimer.cancel();
        attractionTimer.cancel();
        proactiveTimer.cancel();
    }

    public void startTimers() {
        humanAwareness = qiContext.getHumanAwareness();
        //humanAwareness.addOnHumansAroundChangedListener();

        //////////////// Timer for proactive chat
        proactiveTimer = new Timer();
        proactiveTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    humans = findHumansAround();
                    proactiveZone = testProactive(ranges);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (humans != 0 && proactiveZone) {
                    animLock.lock();
                    try {
                        speak("proactive");
                    } finally {
                        animLock.unlock();
                    }
                }
            }
        }, 0, 15000);

        ///////////////Timer for attraction phrases
        attractionTimer = new Timer();
        attractionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    humans = findHumansAround();
                    attractionZone = testAttraction(ranges);
                    proactiveZone = testProactive(ranges);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (humans != 0 && attractionZone && !proactiveZone) {
                    animLock.lock();
                    try {
                        speak("attraction");
                    } finally {
                        animLock.unlock();
                    }
                }
                ranges.clear();
            }
        }, 0, 30000);

        ////////Timer for solitary zone
        solitaryTimer = new Timer();
        solitaryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    humans = findHumansAround();
                    attractionZone = testAttraction(ranges);
                    proactiveZone = testProactive(ranges);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (!attractionZone && !proactiveZone) {
                    animLock.lock();
                    try {
                        speak("solitary");
                    } finally {
                        animLock.unlock();
                    }
                }
                ranges.clear();
            }
        }, 0, 30000);
    }

    public Chat getChat(){
        return chat;
    }
}
