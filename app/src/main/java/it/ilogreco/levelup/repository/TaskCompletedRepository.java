package it.ilogreco.levelup.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import it.ilogreco.levelup.dao.TaskCompletedDao;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.TaskCompleted;

/**
 * CRUD Repository for TaskCompleted
 */
public class TaskCompletedRepository extends BaseRepository<TaskCompleted, TaskCompletedDao> {

    protected TaskCompletedRepository(Application app) {
        super(app);
    }

    @Override
    protected TaskCompletedDao initPrimaryDao() {
        return db.taskCompletedDao();
    }

    @Override
    public LiveData<Long> insertOrUpdate(TaskCompleted entity) {
        MutableLiveData<Long> longLiveData = new MutableLiveData<>();
        if(!checkInsertRequirement(entity)) {
            longLiveData.setValue(-1L);
            return longLiveData;
        }

        db.databaseWriteExecutor.execute(()
                -> db.runInTransaction(()
                -> longLiveData.postValue(insertOrUpdateInternal(entity))));

        return longLiveData;
    }

    @Override
    public long insertOrUpdateInternal(TaskCompleted entity) {
        if(!checkInsertRequirement(entity)) {
            return -1;
        }

        if(!entity.isIdValid()) {
            return super.insertOrUpdateInternal(entity);
        }

        TaskCompleted prev = getInternal(entity.getId());
        if(prev != null) {
            entity.setExperienceEachCategory(prev.getExperienceEachCategory());
            entity.setExperienceGained(prev.getExperienceGained());
            entity.setAdditionalExperience(prev.getAdditionalExperience());
            entity.setBonusPercentage(prev.getBonusPercentage());
        }

        long res = super.insertOrUpdateInternal(entity);
        return res;
    }
}
