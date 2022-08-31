package it.ilogreco.levelup.service.features;

import static android.content.Context.LOCATION_SERVICE;
import static it.ilogreco.levelup.service.BackgroundService.KEY_ACTION;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Locale;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.entity.LocalizationTask;
import it.ilogreco.levelup.entity.TaskCompleted;
import it.ilogreco.levelup.entity.UserTask;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.LocalizationTaskRepository;
import it.ilogreco.levelup.service.BackgroundService;
import it.ilogreco.levelup.utils.CalendarExt;
import it.ilogreco.levelup.utils.LevelUpUtils;
import it.ilogreco.levelup.utils.LiveDataExt;

/**
 * Worker that checks whether a LocalizationTask is completed (check if the user is inside a radius from the address destination)
 */
public class LocalizationWorker extends BaseWorker implements Observer<FullUserTask> {
    public static final String ID = "LOCALIZATION";

    public static final String KEY_PROXIMITY = "KEY_PROXIMITY";

    private PendingIntent currentAlarmIntent;
    private PendingIntent currentProximityIntent;

    private FullUserTask currentLocalizationTask;
    private boolean isTaskLoading;

    private boolean localizationTaskStarted;

    private LocationManager locationManager;
    private LocalizationTaskRepository localizationTaskRepository;

    public LocalizationWorker(int workerIndex, BackgroundService holder) {
        super(workerIndex, holder);
    }

    @Override
    public boolean init() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        localizationTaskRepository = BaseRepository.getInstance(LocalizationTaskRepository.class, getApplication());
        if (localizationTaskRepository == null)
            return false;

        locationManager = (LocationManager) holder.getSystemService(LOCATION_SERVICE);
        if (locationManager == null)
            return false;

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
        if (action == null) return;

        switch (action) {
            case KEY_START_ALARM:
                onAlarmStart();
                break;
            case KEY_END_ALARM:
                onAlarmEnd();
                break;
            case KEY_PROXIMITY:
                onProximityEvent();
                break;
        }
    }

    /**
     * Called once a proximity event is triggered, set the task as successfully completed and find another task
     */
    private void onProximityEvent() {
        onTaskFinished(true);
        selectNextTask();
    }

    /**
     * Called once a start event is triggered, set the current task as started or select a new task
     */
    private void onAlarmStart() {
        currentAlarmIntent = null;
        if (currentLocalizationTask == null) {
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
        if (currentLocalizationTask != null) {
            onTaskFinished(false);
        }
        selectNextTask();
    }

    @Override
    public void onTaskChangedFromUI(long id, FullUserTask fullUserTask) {
        boolean isInputTaskLocalization = fullUserTask != null && fullUserTask.getUserTask().getType() == UserTaskType.Localization;
        // if the there is no current task and the new one is LocalizationTask, set it
        if(currentLocalizationTask == null) {
            if(isInputTaskLocalization) {
                UserTask task = fullUserTask.getUserTask();
                Calendar startDate = task.getBeginDate();
                Calendar endDate = task.getEndDate();
                Calendar now = Calendar.getInstance();
                if (now.before(startDate) || CalendarExt.isBetweenDates(now, startDate, endDate)) {
                    setCurrentLocalizationTask(fullUserTask);
                }
            }
            return;
        }

        // check if the current task changed
        if(currentLocalizationTask.getUserTask().getId() == id) {
            // if deleted or changed type pick another task
            if(fullUserTask == null || fullUserTask.getUserTask().getType() != UserTaskType.Localization) {
                selectNextTask();
            } else {
                UserTask updatedTask = fullUserTask.getUserTask();
                Calendar startDate = updatedTask.getBeginDate();
                Calendar endDate = updatedTask.getEndDate();
                Calendar now = Calendar.getInstance();
                if(localizationTaskStarted) {
                    if(!CalendarExt.isBetweenDates(now, startDate, endDate)) {
                        localizationTaskStarted = false;
                        selectNextTask();
                    } else {
                        currentAlarmIntent = setAlarm(currentAlarmIntent, CalendarExt.getDateTimeWithoutSeconds(fullUserTask.getUserTask().getEndDate()), ID, KEY_END_ALARM);
                        currentLocalizationTask = fullUserTask;
                    }
                } else {
                    setCurrentLocalizationTask(fullUserTask);
                }
            }
            // check if this new task is placed before the current one (which is not started anyway)
        } else if(isInputTaskLocalization && !localizationTaskStarted) {
            // check the dates of this other task
            UserTask task = fullUserTask.getUserTask();
            Calendar startDate = task.getBeginDate();
            Calendar endDate = task.getEndDate();
            Calendar now = Calendar.getInstance();
            if(CalendarExt.isBetweenDates(now, startDate, endDate) ||
                    CalendarExt.isBetweenDates(startDate, now, currentLocalizationTask.getUserTask().getBeginDate())) {
                setCurrentLocalizationTask(fullUserTask);
            }
        }
    }

    /**
     * Select a new task
     */
    private void selectNextTask() {
        if (isTaskLoading) return;

        isTaskLoading = true;
        LiveDataExt.observeOnce(localizationTaskRepository.getFullOrderedByBeginDate(System.currentTimeMillis()), this);
    }

    @Override
    public void onChanged(FullUserTask fullUserTask) {
        isTaskLoading = false;
        setCurrentLocalizationTask(fullUserTask);
    }

    /**
     * Set the current task, set it in pending, start it or reject it depending on the dates
     * @param fullUserTask new task
     */
    private void setCurrentLocalizationTask(FullUserTask fullUserTask) {
        currentLocalizationTask = fullUserTask;
        if (currentLocalizationTask == null) {
            updateWorkerText("No localization task available!");
            updateNotificationUI();
            return;
        }

        Calendar start = currentLocalizationTask.getUserTask().getBeginDate();
        Calendar end = currentLocalizationTask.getUserTask().getEndDate();
        Calendar now = Calendar.getInstance();

        // set a timer in the future
        if (now.before(start)) {
            currentAlarmIntent = setAlarm(currentAlarmIntent, CalendarExt.getDateTimeWithoutSeconds(start), ID, KEY_START_ALARM);
            String title = LevelUpUtils.abbreviateString(currentLocalizationTask.getUserTask().getTitle(), 10);
            String timeText = CalendarExt.areCalendarsDateSame(Calendar.getInstance(), start) ?
                    "at " + CalendarExt.getFormattedTime(start, getApplicationContext()) : "on " + CalendarExt.getFormattedDateTime(start, getApplicationContext());
            String text = String.format(Locale.getDefault(), "Your next localization task '%s' will start %s", title, timeText);
            updateWorkerText(text);
            updateNotificationUI();
            // start the task
        } else if (CalendarExt.isBetweenDates(now, start, end)) {
            onTaskStart();
            // reject this and select a new one
        } else {
            selectNextTask();
        }
    }

    /**
     * Set the current task as started, add a proximity check and update the notifications
     */
    private void onTaskStart() {
        if (currentLocalizationTask == null) {
            localizationTaskStarted = false;
            return;
        }

        LocalizationTask localizationTask = currentLocalizationTask.getLocalizationTask();
        if (currentProximityIntent != null)
            locationManager.removeProximityAlert(currentProximityIntent);

        currentProximityIntent = createDefaultHolderPendingMut(ID, KEY_PROXIMITY);
        try {
            locationManager.addProximityAlert(localizationTask.getLatitude(), localizationTask.getLongitude(), localizationTask.getCompletionRadius(),
                    currentLocalizationTask.getUserTask().getEndDate().getTimeInMillis(), currentProximityIntent);
        } catch (SecurityException ex) {
            return;
        }

        localizationTaskStarted = true;

        currentAlarmIntent = setAlarm(currentAlarmIntent, CalendarExt.getDateTimeWithoutSeconds(currentLocalizationTask.getUserTask().getEndDate()),
                ID, KEY_END_ALARM);

        UserTask userTask = currentLocalizationTask.getUserTask();
        String title = LevelUpUtils.abbreviateString(userTask.getTitle(), 10);
        pushNewNotification("Localization Task '" + title + "' started!");

        Calendar end = currentLocalizationTask.getUserTask().getEndDate();
        String timeText = CalendarExt.areCalendarsDateSame(Calendar.getInstance(), end) ?
                "at " + CalendarExt.getFormattedTime(end, getApplicationContext()) : "on " + CalendarExt.getFormattedDateTime(end, getApplicationContext());
        updateWorkerText("Go to " + currentLocalizationTask.getLocalizationTask().getAddressName() + " before " + timeText + "!");
        updateNotificationUI();
    }

    /**
     * Save the current task and set it as completed, update also the notification
     * @param isCompletedSuccessful true if successful, false o/w
     */
    private void onTaskFinished(boolean isCompletedSuccessful) {
        cancelAlarm(currentAlarmIntent);
        if (currentProximityIntent != null)
            locationManager.removeProximityAlert(currentProximityIntent);

        currentProximityIntent = null;
        localizationTaskStarted = false;

        if(currentLocalizationTask == null) {
            return;
        }

        saveCurrentLocalizationTask(isCompletedSuccessful);

        String title = LevelUpUtils.abbreviateString(currentLocalizationTask.getUserTask().getTitle(), 10);
        if(isCompletedSuccessful) {
            pushNewNotification("Congratulations!",
                    String.format(Locale.getDefault(), "You completed your task by reaching '%s'!", title));
        } else {
            pushNewNotification("Nice Try!",
                    String.format(Locale.getDefault(), "You did not reach '%s' in time :(", title));
        }

        updateWorkerText("Localization Task finished! Waiting for another one...");
        updateNotificationUI();
    }

    /**
     * Save current localization and set is as completed
     * @param isCompletedSuccessful true if successful, false o/w
     */
    private void saveCurrentLocalizationTask(boolean isCompletedSuccessful) {
        saveLocalizationTask(currentLocalizationTask, isCompletedSuccessful);
    }

    private void saveLocalizationTask(FullUserTask fullUserTask, boolean isCompletedSuccessful) {
        if(fullUserTask == null) return;

        if(isCompletedSuccessful) {
            TaskCompleted taskCompleted = new TaskCompleted();
            taskCompleted.setCompletionDate(Calendar.getInstance());
            fullUserTask.setTaskCompleted(taskCompleted);
        }

        localizationTaskRepository.markAsComplete(fullUserTask, isCompletedSuccessful);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
