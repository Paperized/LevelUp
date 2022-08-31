package it.ilogreco.levelup.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import it.ilogreco.levelup.dao.BaseDao;
import it.ilogreco.levelup.dao.CombinedDao;
import it.ilogreco.levelup.database.MyDatabase;
import it.ilogreco.levelup.entity.utils.BaseEntity;
import it.ilogreco.levelup.entity.utils.CombinedEntity;
import it.ilogreco.levelup.entity.utils.Entity;

/**
 * Base Repository class, it holds business logic before any database operation.
 * It holds a reference to the specified Dao.
 * A repository can hold references to other repositories when necessary inside the requireAdditionalRepositories callback.
 * @param <T>
 * @param <D>
 */
public abstract class BaseRepository<T extends Entity, D extends BaseDao<T>> {
    // Current repositories hashmap
    private static final HashMap<Class<? extends BaseRepository<?, ?>>, BaseRepository<?, ?>> instances = new HashMap<>();

    /**
     * Get an instance of a repository if it exists, null otherwise
     * @param clazz repository class
     * @param <R> repository type
     * @param <X> entity managed by that repository
     * @param <Y> dao managed by that repository
     * @return a repository
     */
    public static <R extends BaseRepository<X, Y>, X extends Entity, Y extends BaseDao<X>> R getInstance(Class<R> clazz) {
        return (R) instances.get(clazz);
    }

    /**
     * Get an instance of a repository if it exists, otherwise create it and return the newly created instance
     * @param clazz repository class
     * @param app application
     * @param <R> repository type
     * @param <X> entity managed by that repository
     * @param <Y> dao managed by that repository
     * @return a repository
     */
    public static <R extends BaseRepository<X, Y>, X extends Entity, Y extends BaseDao<X>> R getInstance(Class<R> clazz, Application app) {
        R res = (R) instances.get(clazz);
        if(res == null) {
            try {
                Constructor<R> ctor = clazz.getDeclaredConstructor(Application.class);
                ctor.setAccessible(true);
                res = ctor.newInstance(app);
                instances.put(clazz, res);
                res.requireAdditionalRepositories();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                return null;
            }
        }

        return res;
    }

    protected boolean insertWithoutId = true;
    protected final MyDatabase db;
    protected final Application app;
    protected final D dao;

    protected BaseRepository(Application app) {
        this.app = app;
        db = MyDatabase.getInstance(app);
        dao = initPrimaryDao();
    }

    /**
     * Dao used by this repository
     * @return Dao
     */
    protected abstract D initPrimaryDao();

    /**
     * Additional repository needed
     */
    protected void requireAdditionalRepositories() { }

    public LiveData<List<T>> getAll() {
        return dao.getAll();
    }

    public LiveData<T> get(long id) {
        return dao.get(id);
    }

    public LiveData<Integer> count() {
        return dao.count();
    }

    public LiveData<Long> insertOrUpdate(T entity) {
        MutableLiveData<Long> ldId = new MutableLiveData<>();
        if(!checkInsertRequirement(entity)) {
            ldId.setValue(-1L);
            return ldId;
        }

        db.databaseWriteExecutor.execute(() -> ldId.postValue(insertOrUpdateInternal(entity)));
        return ldId;
    }

    public void insert(T... entities) {
        if(entities.length == 0) return;
        db.databaseWriteExecutor.execute(() -> insertInternal(entities));
    }

    public void insertInternal(T... entities) {
        dao.insert(entities);
    }

    public void insert(List<T> entities) {
        if(entities.size() == 0) return;
        db.databaseWriteExecutor.execute(() -> insertInternal(entities));
    }

    public void insertInternal(List<T> entities) {
        dao.insert(entities);
    }

    public LiveData<Long> insert(T entity) {
        MutableLiveData<Long> ldId = new MutableLiveData<>();
        if(!checkInsertRequirement(entity)) {
            ldId.setValue(-1L);
            return ldId;
        }
        db.databaseWriteExecutor.execute(() -> ldId.postValue(insertInternal(entity)));
        return ldId;
    }

    public long insertInternal(T entity) {
        return dao.insert(entity);
    }

    public void update(T... entities) {
        if(entities.length == 0) return;
        db.databaseWriteExecutor.execute(() -> updateInternal(entities));
    }

    public void updateInternal(T... entities) {
        dao.update(entities);
    }

    public void update(List<T> entities) {
        if(entities.size() == 0) return;
        db.databaseWriteExecutor.execute(() -> updateInternal(entities));
    }

    public void updateInternal(List<T> entities) {
        dao.update(entities);
    }

    public void update(T entity) {
        if(entity == null) return;
        db.databaseWriteExecutor.execute(() -> updateInternal(entity));
    }

    public void updateInternal(T entity) {
        dao.update(entity);
    }

    public LiveData<Integer> delete(T... entities) {
        MutableLiveData<Integer> integerMutableLiveData = new MutableLiveData<>();
        if(entities != null && entities.length == 0) {
            integerMutableLiveData.setValue(0);
            return integerMutableLiveData;
        }

        db.databaseWriteExecutor.execute(() -> integerMutableLiveData.postValue(deleteInternal(entities)));
        return integerMutableLiveData;
    }

    public Integer deleteInternal(T... entities) {
        if(entities == null || entities.length == 0) return 0;
        return dao.delete(entities);
    }

    public LiveData<Integer> delete(List<T> entities) {
        MutableLiveData<Integer> integerMutableLiveData = new MutableLiveData<>();
        if(entities != null && entities.size() == 0) {
            integerMutableLiveData.setValue(0);
            return integerMutableLiveData;
        }

        db.databaseWriteExecutor.execute(() -> integerMutableLiveData.postValue(deleteInternal(entities)));
        return integerMutableLiveData;
    }

    public Integer deleteInternal(List<T> entities) {
        if(entities == null || entities.size() == 0) return 0;
        return dao.delete(entities);
    }

    public LiveData<Integer> delete(T entity) {
        MutableLiveData<Integer> integerMutableLiveData = new MutableLiveData<>();
        if(entity == null) {
            integerMutableLiveData.setValue(0);
            return integerMutableLiveData;
        }

        db.databaseWriteExecutor.execute(() -> integerMutableLiveData.postValue(deleteInternal(entity)));
        return integerMutableLiveData;
    }

    public Integer deleteInternal(T entity) {
        if(entity == null) return 0;
        return dao.delete(entity);
    }

    public long insertOrUpdateInternal(T entity) {
        if(!checkInsertRequirement(entity)) {
            return -1L;
        }

        long res = -1;

        boolean exists;
        if(entity instanceof CombinedEntity) {
            CombinedEntity combinedEntity = (CombinedEntity) entity;
            T prev = ((CombinedDao<T>)dao).getInternal(combinedEntity.getFirstId(), combinedEntity.getSecondId());
            exists = prev != null;
        } else {
            res = ((BaseEntity)entity).getId();
            exists = dao.getInternal(res) != null;
        }

        if (exists)
            dao.update(entity);
        else
            res = dao.insert(entity);

        return res;
    }

    public List<T> getAllInternal() {
        return dao.getAllInternal();
    }

    public List<T> getAllByIdInternal(long id) {
        return dao.getAllByIdInternal(id);
    }

    public T getInternal(long id) {
        return dao.getInternal(id);
    }

    public Integer countInternal() {
        return dao.countInternal();
    }

    protected boolean checkInsertRequirement(T entity) {
        return entity != null && (insertWithoutId || entity.isIdValid());
    }
}
