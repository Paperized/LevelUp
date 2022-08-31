package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.entity.StepCounterTask;

/**
 * Dao used to access data for StepCounterTasks
 */
@Dao
public interface StepCounterTaskDao  extends BaseDao<StepCounterTask>{
    @Transaction
    @Query("SELECT * FROM UserTask WHERE type = 2 AND beginDate >= :startFrom AND completed = 0  ORDER BY beginDate ASC LIMIT 1")
    LiveData<FullUserTask> getLiveFullOrderedByBeginDate(long startFrom);

    @Query("SELECT * FROM StepCounterTask")
    @Override
    LiveData<List<StepCounterTask>> getAll();

    @Query("SELECT * FROM StepCounterTask WHERE id=:id")
    @Override
    LiveData<StepCounterTask> get(long id);

    @Query("SELECT COUNT(*) FROM StepCounterTask")
    @Override
    LiveData<Integer> count();

    @Query("SELECT * FROM StepCounterTask")
    @Override
    List<StepCounterTask> getAllInternal();

    @Query("SELECT * FROM StepCounterTask WHERE id=:id")
    @Override
    List<StepCounterTask> getAllByIdInternal(long id);

    @Query("SELECT * FROM StepCounterTask WHERE id=:id")
    @Override
    StepCounterTask getInternal(long id);

    @Query("SELECT COUNT(*) FROM StepCounterTask")
    @Override
    Integer countInternal();
}
