package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.TaskCategoryRef;

/**
 * Dao used to access data for TaskCategoryRefs
 */
@Dao
public interface TaskCategoryRefDao extends CombinedDao<TaskCategoryRef> {
    @Query("SELECT * FROM TaskCategoryRef")
    @Override
    LiveData<List<TaskCategoryRef>> getAll();

    @Query("SELECT * FROM TaskCategoryRef WHERE firstId=:id1")
    @Override
    LiveData<TaskCategoryRef> get(long id1);

    @Query("SELECT * FROM TaskCategoryRef WHERE firstId=:id1 AND secondId=:id2")
    @Override
    LiveData<TaskCategoryRef> get(long id1, long id2);

    @Query("SELECT COUNT(*) FROM TaskCategoryRef")
    @Override
    LiveData<Integer> count();

    @Query("SELECT * FROM TaskCategoryRef")
    @Override
    List<TaskCategoryRef> getAllInternal();

    @Query("SELECT * FROM TaskCategoryRef WHERE firstId=:id")
    @Override
    List<TaskCategoryRef> getAllByIdInternal(long id);

    @Query("SELECT * FROM TaskCategoryRef WHERE firstId=:id")
    @Override
    TaskCategoryRef getInternal(long id);

    @Query("SELECT * FROM TaskCategoryRef WHERE firstId=:id1 AND secondId=:id2")
    @Override
    TaskCategoryRef getInternal(long id1, long id2);

    @Query("SELECT COUNT(*) FROM TaskCategoryRef")
    @Override
    Integer countInternal();
}
