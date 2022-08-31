package it.ilogreco.levelup.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.ilogreco.levelup.converters.CalendarConverter;
import it.ilogreco.levelup.converters.ListPhotosConverter;
import it.ilogreco.levelup.dao.LocalizationTaskDao;
import it.ilogreco.levelup.dao.StepCounterTaskDao;
import it.ilogreco.levelup.dao.TaskCategoryDao;
import it.ilogreco.levelup.dao.TaskCategoryRefDao;
import it.ilogreco.levelup.dao.TaskCompletedDao;
import it.ilogreco.levelup.dao.UserTaskDao;
import it.ilogreco.levelup.entity.LocalizationTask;
import it.ilogreco.levelup.entity.StepCounterTask;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.TaskCategoryRef;
import it.ilogreco.levelup.entity.TaskCompleted;
import it.ilogreco.levelup.entity.UserTask;

/**
 * Database implementation, holds method to retrieve dao objects.
 */
@Database(entities = {TaskCategory.class, UserTask.class, StepCounterTask.class, TaskCompleted.class, TaskCategoryRef.class, LocalizationTask.class},
        version = 1, exportSchema = false)
@TypeConverters(value = {CalendarConverter.class, ListPhotosConverter.class})
public abstract class MyDatabase extends RoomDatabase {
    private static final String DB_NAME = "LEVEL_UP_DB";
    private static MyDatabase instance;

    public final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    /**
     * Get a singleton instance of the database, create one if not exists.
     * This method is syncrhonized.
     * @param app Application
     * @return Database created
     */
    public static synchronized MyDatabase getInstance(Application app) {
        if(instance == null) {
            instance = Room.databaseBuilder(app, MyDatabase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
        }

        return instance;
    }

    public abstract UserTaskDao userTaskDao();
    public abstract TaskCategoryDao taskCategoryDao();
    public abstract TaskCategoryRefDao taskCategoryRefDao();
    public abstract StepCounterTaskDao stepCounterTaskDao();
    public abstract TaskCompletedDao taskCompletedDao();
    public abstract LocalizationTaskDao localizationTaskDao();
}
