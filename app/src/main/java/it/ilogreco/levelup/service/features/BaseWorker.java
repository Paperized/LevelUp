package it.ilogreco.levelup.service.features;

import static it.ilogreco.levelup.service.BackgroundService.KEY_ACTION;
import static it.ilogreco.levelup.service.BackgroundService.KEY_WORKER;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.jetbrains.annotations.NotNull;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.service.BackgroundService;

/**
 *  Base class for every feature that need to do computation in a background service
 */
public abstract class BaseWorker {
    /** Service reference */
    protected final BackgroundService holder;
    protected AlarmManager alarmManager;

    public static final String KEY_START_ALARM = "KEY_START_ALARM";
    public static final String KEY_END_ALARM = "KEY_END_ALARM";

    /** Index given by the service, it's used to change the notification text for this specific worker */
    protected int workerIndex;

    protected BaseWorker(int workerIndex, BackgroundService holder) {
        if(holder == null) throw new IllegalArgumentException("Holder cannot be null");
        this.holder = holder;
        this.workerIndex = workerIndex;
    }

    /**
     * Called after the constructor, initialize variable and services
     * @return true if the initialization succeeded, false if the worker couldn't be initialized
     */
    public abstract boolean init();

    /**
     * Called after the initialization, start any services you need
     * @return true if the starting succeeded, false if the worker couldn't be started
     */
    public abstract boolean start();

    /**
     * Called before destroying this worker
     */
    public void onStop() { }

    /**
     * Called when an action with destination this worker is received
     * @param intent input intent
     */
    public void onActionReceived(@NotNull Intent intent) { }

    /**
     * Called when a task is changed from UI
     * @param id task id
     * @param fullUserTask full task referring this id
     */
    public void onTaskChangedFromUI(long id, FullUserTask fullUserTask) { }

    /**
     * Change the notification text of this worker
     * @param text notification text
     */
    protected void updateWorkerText(String text) {
        holder.updateWorkerText(text == null || text.equalsIgnoreCase("") ? null : text, this);
    }

    /**
     * Change the notification text of this worker
     * @param texts list of text, one each line
     */
    protected void updateWorkerText(String... texts) {
        String res = "";
        for(int i = 0; i < texts.length; i++) {
            String text = texts[i];
            if(text != null) {
                res = res.concat(text);
                if(i < texts.length - 1)
                    res = res.concat("\n");
            }
        }

        holder.updateWorkerText(res.equalsIgnoreCase("") ? null : res, this);
    }

    /**
     * Change notification title, only one worker can set and show this
     * @param text notification title
     */
    protected void setNotificationTitle(String text) {
        holder.setNotificationTitle(text);
    }

    /**
     * Update notification UI, call updateWorkerText or setNotificationTitle before this
     */
    protected void updateNotificationUI() {
        holder.updateMainNotification();
    }

    /**
     * Create a new notification with this text
     * @param text text
     */
    protected void pushNewNotification(String text) {
        holder.publishNotification(text);
    }

    /**
     * Create a new notification with this text
     * @param title title
     * @param text text
     */
    protected void pushNewNotification(String title, String text) {
        holder.publishNotification(title, text);
    }

    /**
     * Create a new timer with this informtions, if alarm is different then null it will be cancelled and created a new one
     * @param alarm previous alarm
     * @param millis trigger time
     * @param id worker id
     * @param key_op operation key
     * @return new alarm
     */
    protected PendingIntent setAlarm(PendingIntent alarm, long millis, String id, String key_op) {
        cancelAlarm(alarm);

        alarm = createDefaultHolderPending(id, key_op);
        getAlarmManager().setExact(AlarmManager.RTC, millis, alarm);
        return alarm;
    }

    /**
     * Cancel this alarm
     * @param alarm alarm
     */
    protected void cancelAlarm(PendingIntent alarm) {
        if(alarm != null) {
            getAlarmManager().cancel(alarm);
        }
    }

    /**
     * Create immutable pending intent with worker id and operation key
     * @param id worker id
     * @param key_op operation key
     * @return new pending intent
     */
    protected PendingIntent createDefaultHolderPending(String id, String key_op) {
        Intent intent = new Intent(holder, holder.getClass());
        intent.putExtra(KEY_WORKER, id);
        intent.putExtra(KEY_ACTION, key_op);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            flags |= PendingIntent.FLAG_IMMUTABLE;

        return PendingIntent.getService(holder, 0, intent, flags);
    }

    /**
     * Create mutable pending intent with worker id and operation key
     * @param id worker id
     * @param key_op operation key
     * @return new pending intent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    protected PendingIntent createDefaultHolderPendingMut(String id, String key_op) {
        Intent intent = new Intent(holder, holder.getClass());
        intent.putExtra(KEY_WORKER, id);
        intent.putExtra(KEY_ACTION, key_op);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            flags |= PendingIntent.FLAG_MUTABLE;

        return PendingIntent.getService(holder, 0, intent, flags);
    }

    protected Application getApplication() {
        return holder.getApplication();
    }

    protected Context getApplicationContext() {
        return holder.getApplicationContext();
    }

    protected AlarmManager getAlarmManager() {
        if(alarmManager == null)
            alarmManager = (AlarmManager) holder.getSystemService(Context.ALARM_SERVICE);

        return alarmManager;
    }

    public int getWorkerIndex() {
        return workerIndex;
    }
}
