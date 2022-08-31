package it.ilogreco.levelup.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

import it.ilogreco.levelup.dao.TaskCategoryDao;
import it.ilogreco.levelup.entity.TaskCategory;

/**
 * CRUD Repository for TaskCategory
 */
public class TaskCategoryRepository extends BaseRepository<TaskCategory, TaskCategoryDao> {

    private TaskCategoryRepository(Application app) {
        super(app);
    }

    @Override
    protected TaskCategoryDao initPrimaryDao() {
        return db.taskCategoryDao();
    }

    public void addExperience(long id, int experience) {
        dao.addExperience(id, experience);
    }

    public LiveData<Long> saveOrUpdateWithoutExperience(TaskCategory taskCategory) {
        MutableLiveData<Long> longLiveData = new MutableLiveData<>();
        if(!checkInsertRequirement(taskCategory)) {
            longLiveData.setValue(-1L);
            return longLiveData;
        }

        db.databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                taskCategory.setTotalExperience(dao.getExperience(taskCategory.getId()));
                longLiveData.postValue(insertOrUpdateInternal(taskCategory));
            });
        });

        return longLiveData;
    }

    public Map<TaskCategory, Integer> getTaskCategoriesAndCount() {
        return dao.getCategoriesAndTaskCounts();
    }
}
