package it.ilogreco.levelup.service.features;

import static android.content.Context.SENSOR_SERVICE;
import static it.ilogreco.levelup.service.BackgroundService.KEY_ACTION;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.entity.StepCounterTask;
import it.ilogreco.levelup.entity.TaskCompleted;
import it.ilogreco.levelup.entity.UserTask;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.StepCounterTaskRepository;
import it.ilogreco.levelup.service.BackgroundService;
import it.ilogreco.levelup.utils.CalendarExt;
import it.ilogreco.levelup.utils.LevelUpUtils;
import it.ilogreco.levelup.utils.LiveDataExt;

/**
     * Worker that checks whether a StepCounterTask is completed (check if the user has completed the goal km) and take count on the steps since restart
 */
public class StepCounterWorker extends BaseWorker implements SensorEventListener, Observer<FullUserTask> {
    public static final String ID = "STEP_COUNTER";

    private PendingIntent currentAlarmIntent;

    private FullUserTask currentStepTask;
    private boolean isTaskLoading;

    /** current steps taken for this current task */
    private int stepsTaken;
    /** starting offset */
    private int stepsStartingFrom;
    private boolean stepCountingStarted;

    private StepCounterTaskRepository stepCounterTaskRepository;

    /** step length */
    private float stepDistance;

    public StepCounterWorker(int workerIndex, BackgroundService holder) {
        super(workerIndex, holder);
    }

    @Override
    public boolean init() {
        stepsStartingFrom = -1;
        SensorManager sensorManager = (SensorManager) holder.getSystemService(SENSOR_SERVICE);
        if(sensorManager == null)
            return false;

        stepCounterTaskRepository = BaseRepository.getInstance(StepCounterTaskRepository.class, getApplication());
        if(stepCounterTaskRepository == null)
            return false;

        SharedPreferences preferences = LevelUpUtils.initializeDefaultSharedPreferences(getApplicationContext());
        if(preferences == null) {
            return false;
        }

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(sensor == null || !sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL))
            return false;

        stepDistance = preferences.getFloat("KEY_STEP_LENGTH", 0) / 1000;
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
            if(s.equals("KEY_STEP_LENGTH"))
                stepDistance = sharedPreferences.getFloat(s, 0) / 1000;
        });

        return true;
    }

    @Override
    public boolean start() {
        selectNextTask();
        return true;
    }

    @Override
    public void onActionReceived(@NotNull Intent intent) {
        String action = intent.getStringExtra(KEY_ACTION);
        if(action == null) return;

        switch (action) {
            case KEY_START_ALARM:
                onAlarmStart();
                break;
            case KEY_END_ALARM:
                onAlarmEnd();
                break;
        }
    }

    /**
     * Called once a start event is triggered, set the current task as started or select a new task
     */
    private void onAlarmStart() {
        currentAlarmIntent = null;
        if(currentStepTask == null) {
            selectNextTask();
        } else {
            onTaskStart();
        }
    }

    /**
     * Called once a end event is triggered, set the current task as finished (failure) or select a new task
     */
    private void onAlarmEnd() {
        currentAlarmIntent = null;
        if (currentStepTask != null) {
            onTaskFinished();
        }
        selectNextTask();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // total steps since restart
        int totalStepsTaken = (int) sensorEvent.values[0];

        setNotificationTitle("Current record steps: " + totalStepsTaken);
        if(currentStepTask == null || !stepCountingStarted) {
            updateNotificationUI();
            return;
        }

        // set the offset if unset
        if(stepsStartingFrom == -1)
            stepsStartingFrom = totalStepsTaken;

        // update current steps taken
        stepsTaken = totalStepsTaken - stepsStartingFrom;
        float kmTraveled = stepDistance * stepsTaken;
        float diff = currentStepTask.getStepCounterTask().getGoalKm() - kmTraveled;

        // if diff is positive there is are still diff kilometers left
        if(diff > 0) {
            String text = String.format(Locale.getDefault(), "You are %.2f km away from your goal! %d steps traveled!", diff, stepsTaken);
            String contentTitle = String.format(Locale.getDefault(), "Counter Task '%s'", LevelUpUtils.abbreviateString(currentStepTask.getUserTask().getTitle(), 10));
            updateWorkerText(contentTitle, text);
            updateNotificationUI();
            // set this task as completed otherwise
        } else {
            onTaskFinished();
            selectNextTask();
        }
    }

    @Override
    public void onTaskChangedFromUI(long id, FullUserTask fullUserTask) {
        checkTask(id, fullUserTask);
    }

    private void checkTask(long id, FullUserTask fullUserTask) {
        boolean isInputTaskStepCounter = fullUserTask != null && fullUserTask.getUserTask().getType() == UserTaskType.StepCounter;
        if(currentStepTask == null) {
            if(isInputTaskStepCounter) {
                UserTask task = fullUserTask.getUserTask();
                Calendar startDate = task.getBeginDate();
                Calendar endDate = task.getEndDate();
                Calendar now = Calendar.getInstance();
                if (now.before(startDate) || CalendarExt.isBetweenDates(now, startDate, endDate)) {
                    setCurrentStepTask(fullUserTask);
                }
            }
            return;
        }

        // check if the task change affects this service
        if(currentStepTask.getUserTask().getId() == id) {
            // if deleted or changed type pick another task
            if(fullUserTask == null || fullUserTask.getUserTask().getType() != UserTaskType.StepCounter) {
                selectNextTask();
            } else {
                UserTask updatedTask = fullUserTask.getUserTask();
                Calendar startDate = updatedTask.getBeginDate();
                Calendar endDate = updatedTask.getEndDate();
                Calendar now = Calendar.getInstance();

                if(stepCountingStarted) {
                    if(!CalendarExt.isBetweenDates(now, startDate, endDate)) {
                        saveStepsDone(currentStepTask);
                        stepCountingStarted = false;
                        selectNextTask();
                    } else {
                        currentAlarmIntent = setAlarm(currentAlarmIntent, CalendarExt.getDateTimeWithoutSeconds(fullUserTask.getUserTask().getEndDate()), ID, KEY_END_ALARM);
                        currentStepTask = fullUserTask;
                    }
                } else {
                    setCurrentStepTask(fullUserTask);
                }
            }
        } else if(isInputTaskStepCounter && !stepCountingStarted) {
            // check the dates of this other task
            UserTask task = fullUserTask.getUserTask();
            Calendar startDate = task.getBeginDate();
            Calendar endDate = task.getEndDate();
            Calendar now = Calendar.getInstance();
            if (CalendarExt.isBetweenDates(now, startDate, endDate) ||
                    CalendarExt.isBetweenDates(startDate, now, currentStepTask.getUserTask().getBeginDate())) {
                setCurrentStepTask(fullUserTask);
            }
        }
    }

    private void selectNextTask() {
        if(isTaskLoading) return;

        isTaskLoading = true;
        LiveDataExt.observeOnce(stepCounterTaskRepository.getFullOrderedByBeginDate(System.currentTimeMillis()), this);
    }

    @Override
    public void onChanged(FullUserTask fullUserTask) {
        isTaskLoading = false;
        setCurrentStepTask(fullUserTask);
    }

    private void setCurrentStepTask(FullUserTask fullUserTask) {
        currentStepTask = fullUserTask;
        if(currentStepTask == null) {
            updateWorkerText("No step counter tasks available yet!");
            updateNotificationUI();
            return;
        }

        Calendar start = currentStepTask.getUserTask().getBeginDate();
        Calendar end = currentStepTask.getUserTask().getEndDate();
        Calendar now = Calendar.getInstance();

        if(now.before(start)) {
            currentAlarmIntent = setAlarm(currentAlarmIntent, CalendarExt.getDateTimeWithoutSeconds(start), ID, KEY_START_ALARM);
            String title = LevelUpUtils.abbreviateString(currentStepTask.getUserTask().getTitle(), 10);
            String timeText = CalendarExt.areCalendarsDateSame(Calendar.getInstance(), start) ?
                    "at " + CalendarExt.getFormattedTime(start, getApplicationContext()) : "on " + CalendarExt.getFormattedDateTime(start, getApplicationContext());
            String text = String.format(Locale.getDefault(), "Your next counter task '%s' will start %s", title, timeText);
            updateWorkerText(text);
            updateNotificationUI();
        } else if(CalendarExt.isBetweenDates(now, start, end)) {
            onTaskStart();
        } else {
            selectNextTask();
        }
    }

    private void onTaskStart() {
        if(currentStepTask == null) {
            stepCountingStarted = false;
            return;
        }

        stepCountingStarted = true;
        stepsStartingFrom = -1;

        currentAlarmIntent = setAlarm(currentAlarmIntent, CalendarExt.getDateTimeWithoutSeconds(currentStepTask.getUserTask().getEndDate()), ID, KEY_END_ALARM);

        UserTask userTask = currentStepTask.getUserTask();
        String title = LevelUpUtils.abbreviateString(userTask.getTitle(), 10);
        pushNewNotification("Counter Task '" + title + "' started!");

        updateWorkerText("Take your first step to start your task!");
        updateNotificationUI();
    }

    // Save current task and show notification
    private void onTaskFinished() {
        cancelAlarm(currentAlarmIntent);
        stepCountingStarted = false;
        if(currentStepTask == null) {
            return;
        }

        saveCurrentStepTask();

        float kmTraveled = stepDistance * stepsTaken;
        float diff = currentStepTask.getStepCounterTask().getGoalKm() - kmTraveled;

        String title = LevelUpUtils.abbreviateString(currentStepTask.getUserTask().getTitle(), 10);
        if(diff <= 0) {
            pushNewNotification("Congratulations!",
                    String.format(Locale.getDefault(), "You completed your task '%s' by traveling %.2fkm", title, kmTraveled));
        } else {
            pushNewNotification("Nice Try!",
                    String.format(Locale.getDefault(), "You failed completing your task '%s' by traveling %.2fkm", title, kmTraveled));
        }

        updateWorkerText("Current Task finished! Waiting for another one...");
        updateNotificationUI();
    }

    private void saveCurrentStepTask() {
        saveStepTask(currentStepTask);
    }

    private void saveStepTask(FullUserTask fullUserTask) {
        if(fullUserTask == null) return;

        boolean completed = stepsTaken * stepDistance >= fullUserTask.getStepCounterTask().getGoalKm();
        if(completed) {
            TaskCompleted taskCompleted = new TaskCompleted();
            taskCompleted.setCompletionDate(Calendar.getInstance());
            fullUserTask.setTaskCompleted(taskCompleted);
        }

        fullUserTask.getStepCounterTask().setCurrentSteps(stepsTaken);
        fullUserTask.getUserTask().setCompleted(completed);

        stepCounterTaskRepository.markAsComplete(fullUserTask, completed);
    }

    private void saveStepsDone(FullUserTask fullUserTask) {
        if(fullUserTask == null) return;
        StepCounterTask stepCounterTask = fullUserTask.getStepCounterTask();
        if(stepCounterTask == null) return;

        stepCounterTask.setCurrentSteps(stepsTaken);
        stepCounterTask.setTaskId(fullUserTask.getUserTask().getId());
        stepCounterTaskRepository.insertOrUpdate(stepCounterTask);
    }

    @Override
    public void onStop() {
        saveCurrentStepTask();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}
