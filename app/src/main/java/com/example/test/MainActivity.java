package com.example.test;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    public Button kar_button, ent_button, jobs_button;
    public Chat chat;
    private QiContext qiContext;
    private QiChatbot qiChatbot;
    private TouchSensor headTouchSensor;
    private final List<Double> ranges = new ArrayList<>();
    private HumanAwareness humanAwareness;
    private int humans;
    private boolean attractionZone = false, proactiveZone = false, solitaryZone = false;
    public Timer solitaryTimer, attractionTimer, proactiveTimer;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Animate tickleAnim1, tickleAnim2;
    public final Lock animLock = new ReentrantLock();
    //private boolean timersActive = false;
    private Map<String, Bookmark> proactiveBookmarks, attractionBookmarks, solitaryBookmarks;
    private Topic proTop, attTop, solTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //robot = new Robot();
        super.onCreate(savedInstanceState);
        //QiSDK.register(this, robot);
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_main);
        kar_button = findViewById(R.id.kar_button);
        ent_button = findViewById(R.id.ent_button);
        jobs_button = findViewById(R.id.jobs_button);
        //TODO
        ent_button.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        QiSDK.register(this, this);
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.ALWAYS);
        setContentView(R.layout.activity_main);
        kar_button = findViewById(R.id.kar_button);
        ent_button = findViewById(R.id.ent_button);
        jobs_button = findViewById(R.id.jobs_button);
        //TODO
        ent_button.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext theContext) {
        Log.d("gain", "onRobotFocusGained");
        this.qiContext = theContext;
        runChat();
        proTop = TopicBuilder.with(qiContext).withResource(R.raw.proactive).build();
        attTop = TopicBuilder.with(qiContext).withResource(R.raw.attraction).build();
        solTop = TopicBuilder.with(qiContext).withResource(R.raw.solitary).build();
        proactiveBookmarks = proTop.getBookmarks();
        attractionBookmarks = attTop.getBookmarks();
        solitaryBookmarks = solTop.getBookmarks();
        humanAwareness = qiContext.getHumanAwareness();
        initHeadTouchedListener();
        initButtonListeners();
        initListeningListener();
        startTimers();
        startSolitary();
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
            humanAwareness.removeAllOnEngagedHumanChangedListeners();
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
        //stopTimers();
        // The robot focus is refused.
    }

    private void setExecutor() {
        Map<String, QiChatExecutor> executors = new HashMap<>();
        // Map the executor name from the topic to our qiChatbotExecutor
        executors.put("launchAnimation", new MyQiChatExecutor(qiContext));
        executors.put("playSound", new MyQiChatSound(qiContext));
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
                Log.i("TAG", Objects.requireNonNull(e.getMessage()));
            }
        }
        return topics;
    }

    private void speak(String topicName) {
        Object rndKey;
        Bookmark proposalBookmark;
        if(topicName.equals("proactive")) {
            rndKey = getRndKey(proactiveBookmarks);
            proposalBookmark = proactiveBookmarks.get(rndKey);
            proactiveBookmarks.remove(rndKey);
            if(proactiveBookmarks.isEmpty()){
                proactiveBookmarks = proTop.getBookmarks();
            }
        } else if (topicName.equals("attraction")) {
            rndKey = getRndKey(attractionBookmarks);
            proposalBookmark = attractionBookmarks.get(rndKey);
            attractionBookmarks.remove(rndKey);
            if(attractionBookmarks.isEmpty()){
                attractionBookmarks = attTop.getBookmarks();
            }
        } else {
            rndKey = getRndKey(solitaryBookmarks);
            proposalBookmark = solitaryBookmarks.get(rndKey);
            solitaryBookmarks.remove(rndKey);
            if(solitaryBookmarks.isEmpty()){
                solitaryBookmarks = solTop.getBookmarks();
            }
        }
        //Map<String, Bookmark> bookmarks = topic.getBookmarks();
        //rndKey = getRndKey(bookmarks);
        //proposalBookmark = bookmarks.get(rndKey);
        qiChatbot.goToBookmark(proposalBookmark, AutonomousReactionImportance.LOW, AutonomousReactionValidity.DELAYABLE);
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
            initHeadTouchedListener();
        }
    }

    private int findHumansAround() throws ExecutionException {
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
            Human human = humans.get(i);
            Frame humanFrame = human.getHeadFrame();
            double distance = computeDistance(humanFrame, robotFrame);
            ranges.add(distance);
        }
    }

    private double computeDistance(Frame humanFrame, Frame robotFrame) {
        TransformTime transformTime = humanFrame.computeTransform(robotFrame);
        Transform transform = transformTime.getTransform();
        Vector3 translation = transform.getTranslation();
        double x = translation.getX();
        double y = translation.getY();
        return Math.sqrt(x * x + y * y);
    }

    private boolean testProactive(List<Double> ranges){
        for (Double curr : ranges) {
            if (curr <= 1.8) return true;
        }
        return false;
    }

    private boolean testAttraction(List<Double> ranges){
        for (Double curr : ranges) {
            if (curr > 1.8) return true; //curr < 2.5 &&
        }
        return false;
    }

    private boolean testSolitary(List<Double> ranges){
        for (Double curr : ranges) {
            if (curr >= 2.5) return true;
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

    public void stopTimers() {
        solitaryTimer.cancel();
        attractionTimer.cancel();
        proactiveTimer.cancel();
        //timersActive = false;
    }

    public void startTimers() {
        proactiveTimer = new Timer();
        proactiveTimer.schedule(new TimerTask() {
            public void run() {
                tryProactive();
            }
        }, 19000, 19000);

        attractionTimer = new Timer();
        attractionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                tryAttraction();
            }
        }, 21000, 21000);

        /*solitaryTimer = new Timer();
        solitaryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    humans = findHumansAround();
                    attractionZone = testAttraction(ranges);
                    proactiveZone = testProactive(ranges);
                    solitaryZone = testSolitary(ranges);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if ((solitaryZone || humans == 0) && !attractionZone && !proactiveZone) {
                    animLock.lock();
                    try {
                        runOnUiThread(() -> {
                            setImage();
                            //TODO ent_button.setVisibility(View.GONE);
                            kar_button.setVisibility(View.GONE);
                            jobs_button.setVisibility(View.GONE);
                        });
                            speak("solitary");
                        runOnUiThread(() -> {
                            clearImage();
                            //ent_button.setVisibility(View.VISIBLE);
                            kar_button.setVisibility(View.VISIBLE);
                            jobs_button.setVisibility(View.VISIBLE);
                        });
                    } finally {
                        animLock.unlock();
                    }
                }
                ranges.clear();
            }
        }, 125000, 125000);*/
        //timersActive = true;
    }

    private void startSolitary() {
        solitaryTimer = new Timer();
        solitaryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                animLock.lock();
                try {
                    hideUI();
                    speak("solitary");
                } finally {
                    animLock.unlock();
                    showUI();
                }
            }
        }, 15000, 60000);
    }

    private void tryProactive(){
        try {
            humans = findHumansAround();
            proactiveZone = testProactive(ranges);
            ranges.clear();
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

    private void tryAttraction(){
        try {
            humans = findHumansAround();
            attractionZone = testAttraction(ranges);
            proactiveZone = testProactive(ranges);
            ranges.clear();
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
    }

    private void initHeadTouchedListener() {
        Touch touch = qiContext.getTouch();
        headTouchSensor = touch.getSensor("Head/Touch");
        headTouchSensor.addOnStateChangedListener(touchState -> {
            if (touchState.getTouched()) {
                tickle();
            }
        });
        Animation tickle1 = AnimationBuilder.with(qiContext).withResources(R.raw.tickling_a001).build();
        Animation tickle2 = AnimationBuilder.with(qiContext).withResources(R.raw.tickling_a002).build();
        tickleAnim1 = AnimateBuilder.with(qiContext).withAnimation(tickle1).build();
        tickleAnim2 = AnimateBuilder.with(qiContext).withAnimation(tickle2).build();
    }

    private void initButtonListeners() {

        kar_button.setOnClickListener(v -> {
            stopTimers();
            Intent activity2Intent = new Intent(MainActivity.this, CareerActivity.class);
            startActivity(activity2Intent);
            finishAffinity();
        });
        ent_button.setOnClickListener(v -> {
            stopTimers();
            Intent activity2Intent = new Intent(MainActivity.this, EntertainActivity.class);
            startActivity(activity2Intent);
            finishAffinity();
        });
        jobs_button.setOnClickListener(v -> {
            stopTimers();
            Intent activity2Intent = new Intent(MainActivity.this, JobsActivity.class);
            startActivity(activity2Intent);
            finishAffinity();
        });

        humanAwareness.addOnHumansAroundChangedListener(changedHumans -> {
            if(changedHumans.size() != 0){
                stopTimers();
                animLock.lock();
                try {
                    hideUI();
                    speak("proactive");
                } finally {
                    animLock.unlock();
                    showUI();
                }
                startTimers();
            } else {
                startSolitary();
                Log.i("Humans", "Found no humans after change.");
            }
        });

        humanAwareness.addOnEngagedHumanChangedListener(engagedHuman -> {
            if(engagedHuman != null){
                stopTimers();
                animLock.lock();
                try {
                    hideUI();
                    speak("proactive");
                } finally {
                    animLock.unlock();
                    showUI();
                }
                startTimers();
            }
            if(engagedHuman == null){
                startSolitary();
                Log.i("Engaged", "Engaged human(s) = null.");
            }
        });
    }

    private void initListeningListener() {
        chat.addOnListeningChangedListener(listening -> {
            //show Buttons while listening
            if(listening.equals(true)) {
                showUI();
            } else {
                hideUI();
            }
        });
    }

    private void hideUI(){
        runOnUiThread(() -> {
            //setImage
            ImageView test = findViewById(R.id.splashImageView);
            test.setImageResource(R.drawable.speaking);
            test.setVisibility(View.VISIBLE);

            //TODO ent_button.setVisibility(View.GONE);
            kar_button.setVisibility(View.GONE);
            jobs_button.setVisibility(View.GONE);
        });
    }

    private void showUI(){
        runOnUiThread(() -> {
            //clearImage
            ImageView test = findViewById(R.id.splashImageView);
            test.setVisibility(View.GONE);

            //TODO ent_button.setVisibility(View.VISIBLE);
            kar_button.setVisibility(View.VISIBLE);
            jobs_button.setVisibility(View.VISIBLE);
        });
    }
}