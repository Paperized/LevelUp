package it.ilogreco.levelup.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

/**
 * Provides basic CRUD functionalities for Entities with two primary keys
 * Similarly to BaseDao<T> all Queries must be overridden with the right query
 * @param <T> Entity of this dao
 */
@Dao
public interface CombinedDao<T> extends BaseDao<T> {
    String TEMPLATE_STR = "SELECT * FROM Profile";

    @Query(TEMPLATE_STR)
    LiveData<T> get(long id1, long id2);

    @Query(TEMPLATE_STR)
    T getInternal(long id1, long id2);
}
