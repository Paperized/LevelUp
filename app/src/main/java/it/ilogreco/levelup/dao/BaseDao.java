package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Base dao providing basic CRUD functionalities
 * Queries marked as @Query must be overridden with the correct query
 * @param <T> Entity of this Dao
 */
@Dao
public interface BaseDao<T> {
    String TEMPLATE_STR = "SELECT * FROM Profile";

    @Query(TEMPLATE_STR)
    LiveData<List<T>> getAll();

    @Query(TEMPLATE_STR)
    LiveData<T> get(long id);

    @Query(TEMPLATE_STR)
    LiveData<Integer> count();

    @Query(TEMPLATE_STR)
    List<T> getAllInternal();

    @Query(TEMPLATE_STR)
    List<T> getAllByIdInternal(long id);

    @Query(TEMPLATE_STR)
    T getInternal(long id);

    @Query(TEMPLATE_STR)
    Integer countInternal();

    @Insert
    void insert(T... userTask);

    @Insert
    void insert(List<T> userTask);

    @Insert
    long insert(T userTask);

    @Update
    void update(T... userTask);

    @Update
    void update(List<T> userTask);

    @Update
    void update(T userTask);

    @Delete
    Integer delete(T... userTask);

    @Delete
    Integer delete(List<T> userTask);

    @Delete
    Integer delete(T userTask);
}
