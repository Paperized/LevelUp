package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import it.ilogreco.levelup.entity.TaskCategoryRef;
import it.ilogreco.levelup.entity.TaskCompleted;

/**
 * Dao used to access data for TaskCompleted
 */
@Dao
public interface TaskCompletedDao extends BaseDao<TaskCompleted> {
    @Query("SELECT * FROM TaskCompleted")
    @Override
    LiveData<List<TaskCompleted>> getAll();

    @Query("SELECT * FROM TaskCompleted WHERE id=:id")
    @Override
    LiveData<TaskCompleted> get(long id);

    @Query("SELECT COUNT(*) FROM TaskCompleted")
    @Override
    LiveData<Integer> count();

    @Query("SELECT * FROM TaskCompleted")
    @Override
    List<TaskCompleted> getAllInternal();

    @Query("SELECT * FROM TaskCompleted WHERE id=:id")
    @Override
    List<TaskCompleted> getAllByIdInternal(long id);

    @Query("SELECT * FROM TaskCompleted WHERE id=:id")
    @Override
    TaskCompleted getInternal(long id);

    @Query("SELECT COUNT(*) FROM TaskCompleted")
    @Override
    Integer countInternal();
}
