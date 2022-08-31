package it.ilogreco.levelup.receiver;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.UserTaskRepository;
import it.ilogreco.levelup.utils.CalendarExt;

/**
 * Broadcast Receiver used to set all previous tasks as completed (failure), no rewards will be given from this receiver.
 * Runs every midnight and reschedule for the next one
 */
public class MidnightTimerReceiver extends BroadcastReceiver {
    public final static int ALARM_ID = 23746284;

    @Override
    public void onReceive(Context context, Intent intent) {
        UserTaskRepository userTaskRepository = BaseRepository.getInstance(UserTaskRepository.class, (Application) context.getApplicationContext());
        if(userTaskRepository != null)
            userTaskRepository.refreshTasksCompleteness();

        scheduleNextMidnight(context);
    }

    public static void initializeAlarm(Context context) {
        scheduleNextMidnight(context);
    }

    private static void scheduleNextMidnight(Context context) {
        Intent intent = new Intent(context, MidnightTimerReceiver.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            flags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, intent, flags);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC, getNextMidnightTimestamp(), pendingIntent);
    }

    private static long getNextMidnightTimestamp() {
        Calendar calendar = CalendarExt.getCurrentWithoutTime();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        return calendar.getTimeInMillis();
    }
}
