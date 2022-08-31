package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.MapInfo;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;
import java.util.Map;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.entity.UserTask;

/**
 * Dao used to access data for UserTasks
 */
@Dao
public interface UserTaskDao extends BaseDao<UserTask> {

    @MapInfo(keyColumn = "type", valueColumn = "typeCount")
    @Query("SELECT type, COUNT(*) as typeCount FROM UserTask GROUP BY type")
    Map<Integer, Integer> getTypesAndCount();

    @Transaction
    @Query("SELECT * FROM UserTask WHERE NOT (:start > endDate OR :end < beginDate) ORDER BY beginDate")
    LiveData<List<FullUserTask>> getAllFullTasksEndDateBetween(long start, long end);

    @Transaction
    @Query("SELECT * FROM UserTask WHERE id=:id")
    LiveData<FullUserTask> getFullTasks(long id);

    @Query("SELECT * FROM UserTask")
    @Override
    LiveData<List<UserTask>> getAll();

    @Query("SELECT * FROM UserTask WHERE id=:id")
    @Override
    LiveData<UserTask> get(long id);

    @Query("SELECT COUNT(*) FROM UserTask")
    @Override
    LiveData<Integer> count();

    @Query("SELECT * FROM UserTask")
    @Override
    List<UserTask> getAllInternal();

    @Query("SELECT * FROM UserTask WHERE id=:id")
    @Override
    List<UserTask> getAllByIdInternal(long id);

    @Query("SELECT * FROM UserTask WHERE id=:id")
    @Override
    UserTask getInternal(long id);

    @Query("SELECT COUNT(*) FROM UserTask")
    @Override
    Integer countInternal();

    @Query("SELECT COUNT(*) FROM UserTask WHERE completed = 1")
    Integer countCompletedInternal();

    @Query("SELECT COUNT(*) FROM UserTask t1 JOIN TaskCompleted c1 ON t1.id = c1.id WHERE completed = 1")
    Integer countSucceededInternal();

    @Query("SELECT completed FROM UserTask WHERE id=:id")
    Boolean isCompletedInternal(long id);

    @Query("UPDATE UserTask SET completed = 1 WHERE id=:id")
    void setAsCompleted(long id);

    @Query("UPDATE UserTask SET completed = 1 WHERE endDate < :current")
    void refreshAllPreviousTasksAsCompleted(long current);
}
