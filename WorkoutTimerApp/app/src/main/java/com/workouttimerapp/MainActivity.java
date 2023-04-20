package com.workouttimerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    LinearLayout mainLayout, workoutLayout;

    EditText workoutDuration, restPeriod;

    Button startWorkoutButton, playPauseWorkoutButton, stopWorkoutButton, addItemToList;

    //Boolean value to play and pause timer and progress bar
    boolean timerAndProgressBarRunning;

    ProgressBar progressBar;

    TextView countDownTextView, workoutOrRestTextView;

    long workoutDurationValue = 0;
    long restPeriodValue = 0;

    static CountDownTimer countDownTimer;

    static Long START_TIME_IN_MILLIS = 0L;
    Long timeleftInMillis = START_TIME_IN_MILLIS;

    //A list to save workout durtion and rest period
    static List<Long> workoutDurationAndRestPeriodList;

    static int i; //Denoting the index of current workout or rest

    MediaPlayer mediaPlayerOnChange, mediaPlayerWorkoutComplete;

    static String CHANNEL_ID = "cure fit channel id";
    static String CHANNEL_NAME = "cure fit channel name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing all the buttons, layouts, progress bar, text views and edit text boxes
        mainLayout = findViewById(R.id.mainLayout);
        workoutLayout = findViewById(R.id.workoutLayout);
        workoutDuration = findViewById(R.id.workoutDuration);
        restPeriod = findViewById(R.id.restPeriod);
        startWorkoutButton = findViewById(R.id.startWorkoutButton);
        playPauseWorkoutButton = findViewById(R.id.palyPauseWorkoutButton);
        stopWorkoutButton = findViewById(R.id.stopWorkoutButton);
        addItemToList = findViewById(R.id.addItemToList);
        progressBar = findViewById(R.id.progress_bar);
        countDownTextView = findViewById(R.id.progress_text);
        workoutOrRestTextView = findViewById(R.id.workoutOrRestTextView);

        //Initializing workout duration and rest period list
        workoutDurationAndRestPeriodList = new ArrayList<>();

        //Initializing mp3 notifications
        mediaPlayerOnChange = MediaPlayer.create(this, R.raw.change_workout_notification);
        mediaPlayerWorkoutComplete = MediaPlayer.create(this, R.raw.workout_completed_notification);

        //Function to initialize layouts and set values for the required variables
        initializeOrStopWorkout(false);

        //Click action for Start Workout Button
        addItemToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateTextFieldsAndAddItemToList();
            }
        });
        //Click action for Start Workout Button
        startWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popNotificationDialog("Workout Started.", true);
                validateTextFieldsAndStartWorkout();
            }
        });

        //Click action for stop workout button
        stopWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initializeOrStopWorkout(true);
            }
        });

        //Click action for play and pause button
        playPauseWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playOrPauseTimerAndProgressBar();
            }
        });

    }

    private void validateTextFieldsAndStartWorkout() {
        mainLayout.setVisibility(View.GONE);
        workoutLayout.setVisibility(View.VISIBLE);
        //Closing keyboard if open
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(countDownTextView.getWindowToken(), 0);
        startWorkout();
    }

    private void startWorkout() {
        if (workoutDurationAndRestPeriodList.size() > 0) {
            if (i % 2 == 0) {
                workoutOrRestTextView.setText("Workout");
                START_TIME_IN_MILLIS = workoutDurationAndRestPeriodList.get(i) * 60 * 1000;
            } else {
                START_TIME_IN_MILLIS = workoutDurationAndRestPeriodList.get(i) * 1000;
                workoutOrRestTextView.setText("Rest");
            }

            timeleftInMillis = START_TIME_IN_MILLIS;
            playTimerAndProgressBar();
        }
    }

    private void playOrPauseTimerAndProgressBar() {
        if (timerAndProgressBarRunning) {
            pauseTimerAndProgressBar();
        } else {
            playTimerAndProgressBar();
        }
    }

    private void playTimerAndProgressBar() {
        countDownTimer = new CountDownTimer(timeleftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeleftInMillis = millisUntilFinished;
                updateCountDownTextAndProgressBar();
            }

            @Override
            public void onFinish() {
                timerAndProgressBarRunning = false;
                if (i < workoutDurationAndRestPeriodList.size() - 1) {
                    i += 1;
                    mediaPlayerOnChange.start();
                    popNotificationDialog("Get ready for next phase.", true);
                    startWorkout();
                } else {
                    initializeOrStopWorkout(true);
                }
            }
        }.start();

        //Setting timer and progress bar boolean to true
        timerAndProgressBarRunning = true;
        //Changing play button to pause button
        playPauseWorkoutButton.setBackgroundResource(R.drawable.pause_button);
        stopWorkoutButton.setVisibility(View.VISIBLE);
    }

    //For Notification Pop-up
    private void popNotificationDialog(String text, boolean showPauseAndStopAction) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID);
        builder.setAutoCancel(true);
        builder.setContentTitle("cure.fit");
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.cultfitlogo);
        if (showPauseAndStopAction && countDownTimer != null) {
            Intent myIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity
                        (this, 0, myIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivity
                        (this, 0, myIntent, PendingIntent.FLAG_ONE_SHOT);
            }
            builder.addAction(R.drawable.pause_button, "Stop Workout", pendingIntent);
        }
        notificationManager.notify(1, builder.build());
    }

    private void updateCountDownTextAndProgressBar() {
        int minutes = (int) (timeleftInMillis / 1000) / 60;
        int seconds = (int) (timeleftInMillis / 1000) % 60;
        String timeLeftFormat = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        countDownTextView.setText(timeLeftFormat);
        int progress = (int) Math.floor((double) (timeleftInMillis * 100) / START_TIME_IN_MILLIS);
        System.out.println("Start Time: " + START_TIME_IN_MILLIS + " Time Left: " + timeleftInMillis + " Progress: " + progress);
        progressBar.setProgress(100 - progress);
    }

    private void pauseTimerAndProgressBar() {
        countDownTimer.cancel();
        timerAndProgressBarRunning = false;
        //Changing pause button to play button
        playPauseWorkoutButton.setBackgroundResource(R.drawable.play_button);
    }

    private void initializeOrStopWorkout(boolean stopWorkout) {
        timerAndProgressBarRunning = false;
        if (countDownTimer != null)
            countDownTimer.cancel();
        if (stopWorkout) {
            mediaPlayerWorkoutComplete.start();
            popNotificationDialog("Workout Completed.", false);
        }
        i = 0;
        workoutDurationAndRestPeriodList.clear();
        timeleftInMillis = START_TIME_IN_MILLIS = 0L;
        updateCountDownTextAndProgressBar();
        workoutDuration.setText(workoutDuration.getText().toString());
        restPeriod.setText(restPeriod.getText().toString());
        //Setting visibility of the layouts
        workoutLayout.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        startWorkoutButton.setVisibility(View.GONE);
        //Setting focus to the textbox to enter worout duration value
        workoutDuration.requestFocus();
    }

    private void validateTextFieldsAndAddItemToList() {
        //Getting value from edit text boxes
        String workoutDurationStringValue = workoutDuration.getText().toString();
        String restPeriodStringValue = restPeriod.getText().toString();

        //Checking if user has entered the required value and is valid
        if (!workoutDurationStringValue.equalsIgnoreCase("") && !restPeriodStringValue.equalsIgnoreCase("")) {
            //Validating entered value
            if (isEnteredValueNumeric(workoutDurationStringValue) && isEnteredValueNumeric(restPeriodStringValue)) {
                workoutDurationValue = Long.parseLong(workoutDurationStringValue);
                restPeriodValue = Long.parseLong(restPeriodStringValue);
                workoutDurationAndRestPeriodList.add(workoutDurationValue);
                workoutDurationAndRestPeriodList.add(restPeriodValue);
                startWorkoutButton.setVisibility(View.VISIBLE);
                Toast.makeText(workoutDuration.getContext(), "Added workout with duration: " + workoutDurationStringValue + " and rest period: " + restPeriodStringValue, Toast.LENGTH_SHORT).show();
                System.out.println("-> " + workoutDurationAndRestPeriodList);
            } else {
                if (!isEnteredValueNumeric(workoutDurationStringValue)) {
                    Toast.makeText(workoutDuration.getContext(), "Please enter correct workout duration.", Toast.LENGTH_SHORT).show();
                } else if (!isEnteredValueNumeric(restPeriodStringValue)) {
                    Toast.makeText(restPeriod.getContext(), "Please enter correct rest period.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(workoutDuration.getContext(), "Please enter correct workout duration and rest period.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (workoutDurationStringValue.equalsIgnoreCase("")) {
                Toast.makeText(workoutDuration.getContext(), "Please enter workout duration.", Toast.LENGTH_SHORT).show();
            } else if (restPeriodStringValue.equalsIgnoreCase("")) {
                Toast.makeText(restPeriod.getContext(), "Please enter rest period.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(workoutDuration.getContext(), "Please enter workout duration and rest period.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Function to validate the entered value is numeric or not
    public static boolean isEnteredValueNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            long d = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}