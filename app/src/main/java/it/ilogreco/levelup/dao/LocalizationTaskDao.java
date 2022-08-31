package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.entity.LocalizationTask;
import it.ilogreco.levelup.entity.utils.AddressAdapterData;

/**
 * Dao used to access data for LocalizationTasks
 */
@Dao
public interface LocalizationTaskDao extends BaseDao<LocalizationTask> {
    @Transaction
    @Query("SELECT * FROM UserTask WHERE type = 3 AND beginDate >= :startFrom AND completed = 0 ORDER BY beginDate ASC LIMIT 1")
    LiveData<FullUserTask> getFullOrderedByBeginDate(long startFrom);

    @Query("SELECT addressName, latitude, longitude FROM LocalizationTask GROUP BY addressName, latitude, longitude ORDER BY id DESC LIMIT :n")
    LiveData<List<AddressAdapterData>> getNewestAddresses(int n);

    @Query("SELECT * FROM LocalizationTask")
    @Override
    LiveData<List<LocalizationTask>> getAll();

    @Query("SELECT * FROM LocalizationTask WHERE id=:id")
    @Override
    LiveData<LocalizationTask> get(long id);

    @Query("SELECT COUNT(*) FROM LocalizationTask")
    @Override
    LiveData<Integer> count();

    @Query("SELECT * FROM LocalizationTask")
    @Override
    List<LocalizationTask> getAllInternal();

    @Query("SELECT * FROM LocalizationTask WHERE id=:id")
    @Override
    List<LocalizationTask> getAllByIdInternal(long id);

    @Query("SELECT * FROM LocalizationTask WHERE id=:id")
    @Override
    LocalizationTask getInternal(long id);

    @Query("SELECT COUNT(*) FROM LocalizationTask")
    @Override
    Integer countInternal();
}
