package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.MapInfo;
import androidx.room.Query;

import java.util.List;
import java.util.Map;

import it.ilogreco.levelup.entity.StepCounterTask;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.UserTask;

/**
 * Dao used to access data for TaskCategories
 */
@Dao
public interface TaskCategoryDao  extends BaseDao<TaskCategory> {

    @MapInfo(valueColumn = "taskCount")
    @Query("SELECT c.*, COUNT(DISTINCT firstId) as taskCount FROM TaskCategory c LEFT JOIN TaskCategoryRef ON id = secondId GROUP BY id")
    Map<TaskCategory, Integer> getCategoriesAndTaskCounts();

    @Query("SELECT * FROM TaskCategory")
    @Override
    LiveData<List<TaskCategory>> getAll();

    @Query("SELECT * FROM TaskCategory WHERE id=:id")
    @Override
    LiveData<TaskCategory> get(long id);

    @Query("SELECT COUNT(*) FROM TaskCategory")
    @Override
    LiveData<Integer> count();

    @Query("SELECT * FROM TaskCategory")
    @Override
    List<TaskCategory> getAllInternal();

    @Query("SELECT * FROM TaskCategory WHERE id=:id")
    @Override
    List<TaskCategory> getAllByIdInternal(long id);

    @Query("SELECT * FROM TaskCategory WHERE id=:id")
    @Override
    TaskCategory getInternal(long id);

    @Query("SELECT COUNT(*) FROM TaskCategory")
    @Override
    Integer countInternal();

    @Query("UPDATE TaskCategory SET totalExperience = totalExperience + :experience WHERE id=:id")
    void addExperience(long id, int experience);

    @Query("SELECT totalExperience FROM TaskCategory WHERE id=:id")
    int getExperience(long id);
}
