package it.ilogreco.levelup.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap;

import it.ilogreco.levelup.MainActivity;
import it.ilogreco.levelup.R;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.UserTaskRepository;
import it.ilogreco.levelup.service.features.BaseWorker;
import it.ilogreco.levelup.service.features.LocalizationWorker;
import it.ilogreco.levelup.service.features.StepCounterWorker;
import it.ilogreco.levelup.utils.LiveDataExt;

/**
 * Foreground service that dispatch intents to workers and handle their callbacks.
 * Contains also utility methods for workers
 */
public class BackgroundService extends Service {
    public class BackgroundBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private final BackgroundBinder binder = new BackgroundBinder();

    public static final String KEY_WORKER = "KEY_WK";
    public static final String KEY_ACTION = "KEY_ACTION";

    private final HashMap<String, BaseWorker> workerHashMap = new HashMap<>();

    private PendingIntent mainActivityIntent;
    private final int MAIN_NOTIFICATION_ID = Integer.MAX_VALUE;
    private final String CHANNEL_SERVICE = "MY_CHANNEL";
    private NotificationManager notificationManager;

    private UserTaskRepository userTaskRepository;

    private String notificationTitle;
    private String[] notificationLines;

    private int notificationsIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        NotificationChannelCompat.Builder channelCompat = new NotificationChannelCompat.Builder(
                CHANNEL_SERVICE, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setDescription("LevelUp Notification!")
                .setName("LevelUp");
        notificationManagerCompat.createNotificationChannel(channelCompat.build());

        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        mainActivityIntent = PendingIntent.getActivity(getApplicationContext(), 0, mainActivity, flags);
        userTaskRepository = BaseRepository.getInstance(UserTaskRepository.class, getApplication());

        notificationLines = new String[2];
        addWorker(StepCounterWorker.ID, new StepCounterWorker(0, this));
        addWorker(LocalizationWorker.ID, new LocalizationWorker(1, this));

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(MAIN_NOTIFICATION_ID, getMainNotification().build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String workerId = intent.getStringExtra(KEY_WORKER);

            if(workerId != null) {
                BaseWorker baseWorker = workerHashMap.get(workerId);
                if (baseWorker != null)
                    baseWorker.onActionReceived(intent);
                else
                    Log.w("Service", "Worker with id " + workerId + " does not exists!");
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void onTaskCounterUpdated(final long id) {
        if(id <= 0) return;

        LiveDataExt.observeOnce(userTaskRepository.getFullTasks(id), x -> {
            for(BaseWorker baseWorker : workerHashMap.values())
                baseWorker.onTaskChangedFromUI(id, x);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for(BaseWorker baseWorker : workerHashMap.values())
            if(baseWorker != null)
                baseWorker.onStop();
    }

    public void updateWorkerText(String text, BaseWorker baseWorker) {
        if(baseWorker == null) return;
        notificationLines[baseWorker.getWorkerIndex()] = text;
    }

    public NotificationCompat.Builder createDefaultNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_SERVICE)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setSubText("Status")
                .setContentTitle(getString(R.string.app_name))
                .setContentIntent(mainActivityIntent);
    }

    public void updateMainNotification() {
        publishNotification(MAIN_NOTIFICATION_ID, getMainNotification());
    }

    private NotificationCompat.Builder getMainNotification() {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (String line : notificationLines) {
            if(line != null)
                inboxStyle.addLine(line);
        }

        return new NotificationCompat.Builder(this, CHANNEL_SERVICE)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(notificationTitle)
                        .setStyle(inboxStyle)
                        .setContentIntent(mainActivityIntent);
    }

    public void publishNotification(int id, NotificationCompat.Builder builder) {
        notificationManager.notify(id, builder.build());
    }

    public void publishNotification(String content) {
        notificationsIndex += 1;
        notificationManager.notify(notificationsIndex,
                createDefaultNotification().setAutoCancel(true).setContentText(content).build());

        if(notificationsIndex == Integer.MAX_VALUE)
            notificationsIndex = 0;
    }

    public void publishNotification(String title, String content) {
        notificationsIndex += 1;
        notificationManager.notify(notificationsIndex,
                createDefaultNotification().setAutoCancel(true).setContentTitle(title).setContentText(content).build());

        if(notificationsIndex == Integer.MAX_VALUE)
            notificationsIndex = 0;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected boolean addWorker(String id, BaseWorker baseWorker) {
        if(baseWorker == null) {
            Log.w("AddWorker", "Worker cannot be null!");
            return false;
        }

        if(id == null || workerHashMap.containsKey(id)) {
            Log.w("AddWorker", "Worker ID cannot be null or already used!");
            return false;
        }

        if(!baseWorker.init()) {
            Log.w("InitWorker", "Worker ID " + id + " failed initialization!");
            return false;
        }

        workerHashMap.put(id, baseWorker);
        baseWorker.start();
        return true;
    }
}
