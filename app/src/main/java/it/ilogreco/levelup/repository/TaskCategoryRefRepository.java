package it.ilogreco.levelup.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import it.ilogreco.levelup.dao.TaskCategoryRefDao;
import it.ilogreco.levelup.entity.TaskCategoryRef;

/**
 * CRUD Repository for TaskCategoryRef
 */
public class TaskCategoryRefRepository extends BaseRepository<TaskCategoryRef, TaskCategoryRefDao> {

    protected TaskCategoryRefRepository(Application app) {
        super(app);

        insertWithoutId = false;
    }

    @Override
    protected TaskCategoryRefDao initPrimaryDao() {
        return db.taskCategoryRefDao();
    }

    public LiveData<TaskCategoryRef> get(long id1, long id2) {
        return dao.get(id1, id2);
    }

    public TaskCategoryRef getInternal(long id1, long id2) {
        return dao.getInternal(id1, id2);
    }
}
