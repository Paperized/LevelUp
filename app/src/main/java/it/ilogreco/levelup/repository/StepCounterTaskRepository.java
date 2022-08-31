package it.ilogreco.levelup.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import it.ilogreco.levelup.dao.StepCounterTaskDao;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.data.GainedExperienceResult;
import it.ilogreco.levelup.entity.StepCounterTask;

/**
 * CRUD Repository for StepCounterTasks
 */
public class StepCounterTaskRepository extends BaseRepository<StepCounterTask, StepCounterTaskDao> {
    private UserTaskRepository userTaskRepository;

    private StepCounterTaskRepository(Application app) {
        super(app);
    }

    @Override
    protected StepCounterTaskDao initPrimaryDao() {
        return db.stepCounterTaskDao();
    }

    @Override
    protected void requireAdditionalRepositories() {
        userTaskRepository = getInstance(UserTaskRepository.class, app);
    }

    public LiveData<FullUserTask> getFullOrderedByBeginDate(long startFrom) {
        return dao.getLiveFullOrderedByBeginDate(startFrom);
    }

    public LiveData<GainedExperienceResult> markAsComplete(FullUserTask userTask, boolean succeeded) {
        MutableLiveData<GainedExperienceResult> gainedLiveData = new MutableLiveData<>();
        if(userTask == null || userTask.getUserTask() == null || userTask.getUserTask().getId() == 0) {
            gainedLiveData.setValue(null);
            return gainedLiveData;
        }

        db.databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                GainedExperienceResult gainedExperienceResult = userTaskRepository.markAsCompleteInternal(userTask, succeeded);
                update(userTask.getStepCounterTask());

                gainedLiveData.postValue(gainedExperienceResult);
            });
        });

        return gainedLiveData;
    }
}
