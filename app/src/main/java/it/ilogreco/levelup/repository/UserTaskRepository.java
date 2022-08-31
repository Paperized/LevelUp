package it.ilogreco.levelup.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.ilogreco.levelup.dao.UserTaskDao;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.data.GainedExperienceResult;
import it.ilogreco.levelup.data.UserMetrics;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.TaskCategoryRef;
import it.ilogreco.levelup.entity.TaskCompleted;
import it.ilogreco.levelup.entity.UserTask;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.utils.LevelUpUtils;

/**
 * CRUD Repository for UserTask.
 * It holds some custom methods to mark a task as complete (successfully or failure), provides rewards on completion, find metrics, and all methods regarding FullUserTasks data
 */
public class UserTaskRepository extends BaseRepository<UserTask, UserTaskDao> {
    private StepCounterTaskRepository stepCounterTaskRepository;
    private LocalizationTaskRepository localizationTaskRepository;
    private TaskCompletedRepository taskCompletedRepository;
    private TaskCategoryRefRepository taskCategoryRefRepository;
    private TaskCategoryRepository taskCategoryRepository;

    private UserTaskRepository(Application app) {
        super(app);
    }

    @Override
    protected UserTaskDao initPrimaryDao() {
        return db.userTaskDao();
    }

    @Override
    protected void requireAdditionalRepositories() {
        stepCounterTaskRepository = getInstance(StepCounterTaskRepository.class, app);
        taskCompletedRepository = getInstance(TaskCompletedRepository.class, app);
        taskCategoryRefRepository = getInstance(TaskCategoryRefRepository.class, app);
        taskCategoryRepository = getInstance(TaskCategoryRepository.class, app);
        localizationTaskRepository = getInstance(LocalizationTaskRepository.class, app);
    }

    // Set all tasks previous this date as completed
    public void refreshTasksCompleteness() {
        db.databaseWriteExecutor.execute(() -> dao.refreshAllPreviousTasksAsCompleted(System.currentTimeMillis()));
    }

    /**
     * Get User Metrics up to this point
     * @return LiveData object, it is update only one time
     */
    public LiveData<UserMetrics> getUserMetrics() {
        MutableLiveData<UserMetrics> userMetricsMutableLiveData = new MutableLiveData<>();

        db.databaseWriteExecutor.execute(() -> {
            final UserMetrics userMetrics = new UserMetrics();

            db.runInTransaction(() -> {
                userMetrics.totalTasks = dao.countInternal();
                userMetrics.completedTasks = dao.countCompletedInternal();
                userMetrics.ongoingTasks = userMetrics.totalTasks - userMetrics.completedTasks;

                userMetrics.successTasks = dao.countSucceededInternal();
                userMetrics.failedTasks = userMetrics.completedTasks - userMetrics.successTasks;

                userMetrics.tasksPerType = dao.getTypesAndCount();
                if(!userMetrics.tasksPerType.containsKey(UserTaskType.Generic))
                    userMetrics.tasksPerType.put(UserTaskType.Generic, 0);
                if(!userMetrics.tasksPerType.containsKey(UserTaskType.StepCounter))
                    userMetrics.tasksPerType.put(UserTaskType.StepCounter, 0);
                if(!userMetrics.tasksPerType.containsKey(UserTaskType.Localization))
                    userMetrics.tasksPerType.put(UserTaskType.Localization, 0);

                userMetrics.tasksPerCategory = taskCategoryRepository.getTaskCategoriesAndCount();

                userMetricsMutableLiveData.postValue(userMetrics);
            });
        });

        return userMetricsMutableLiveData;
    }

    /**
     * Create or Update a FullUserTask, a complete object must be provided, all taskIds related to the UserTask are handled by this method
     * @param userTask full user task object
     * @return LiveData that represent the newly create id or the previous one
     */
    public LiveData<Long> insertOrUpdateFullUserTask(final FullUserTask userTask) {
        final MutableLiveData<Long> ldId = new MutableLiveData<>();
        if(userTask == null) {
            ldId.setValue(-1L);
            return ldId;
        }

        db.databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                long res = insertOrUpdateInternal(userTask.getUserTask());
                if(res == -1) {
                    ldId.setValue(-1L);
                    return;
                }

                List<TaskCategoryRef> prevRefs = taskCategoryRefRepository.getAllByIdInternal(res);
                List<TaskCategoryRef> toAdd = new ArrayList<>();
                List<TaskCategoryRef> toRemove = new ArrayList<>();

                calculateTaskCategoryRefLists(prevRefs, userTask.getTaskCategory(), toRemove, toAdd, res);
                taskCategoryRefRepository.deleteInternal(toRemove);
                taskCategoryRefRepository.insertInternal(toAdd);

                TaskCompleted taskCompleted = userTask.getTaskCompleted();
                if(taskCompleted != null) {
                    taskCompleted.setTaskId(res);
                    taskCompletedRepository.insertOrUpdateInternal(taskCompleted);
                }

                // add more later
                switch (userTask.getUserTask().getType()) {
                    case UserTaskType.StepCounter:
                        userTask.getStepCounterTask().setTaskId(res);
                        stepCounterTaskRepository.insertOrUpdateInternal(userTask.getStepCounterTask());
                        break;
                    case UserTaskType.Localization:
                        userTask.getLocalizationTask().setTaskId(res);
                        localizationTaskRepository.insertOrUpdateInternal(userTask.getLocalizationTask());
                        break;
                    default:
                        break;
                }

                ldId.postValue(res);
            });
        });

        return ldId;
    }

    /**
     * Mark a full user task as completed, if failure no TaskCompleted will be created and no experience will be issued, otherwise create it and randomly assign experience.
     * @param fullUserTask full user task object
     * @param succeeded if the task succeeded
     * @return LiveData containing the generated experience
     */
    public LiveData<GainedExperienceResult> markAsComplete(FullUserTask fullUserTask, boolean succeeded) {
        MutableLiveData<GainedExperienceResult> booleanMutableLiveData = new MutableLiveData<>();
        if(fullUserTask == null || fullUserTask.getUserTask() == null || fullUserTask.getUserTask().getId() == 0) {
            booleanMutableLiveData.setValue(null);
            return booleanMutableLiveData;
        }

        db.databaseWriteExecutor.execute(() -> {
            db.runInTransaction(() -> {
                booleanMutableLiveData.postValue(markAsCompleteInternal(fullUserTask, succeeded));
            });
        });

        return booleanMutableLiveData;
    }

    public GainedExperienceResult markAsCompleteInternal(FullUserTask fullUserTask, boolean succeeded) {
        UserTask userTask = fullUserTask.getUserTask();
        if(dao.isCompletedInternal(userTask.getId())) {
            return new GainedExperienceResult();
        }

        dao.setAsCompleted(userTask.getId());
        userTask.setCompleted(true);

        if(!succeeded) {
            return new GainedExperienceResult();
        }

        TaskCompleted taskCompleted = fullUserTask.getTaskCompleted();
        if (taskCompleted == null)
            fullUserTask.setTaskCompleted(taskCompleted = new TaskCompleted());
        else
            taskCompleted.setCompletionDate(Calendar.getInstance());

        taskCompleted.setTaskId(userTask.getId());

        // UPDATE EXPERIENCE
        List<TaskCategory> categories = fullUserTask.getTaskCategory();
        if(categories == null || categories.size() == 0) {
            taskCompletedRepository.insertOrUpdateInternal(taskCompleted);
            return new GainedExperienceResult();
        }

        GainedExperienceResult gainedExperienceResult = LevelUpUtils.calculateExperience(fullUserTask);
        gainedExperienceResult.wasGiven = true;

        int nCategories = categories.size();
        int totalExp = gainedExperienceResult.getTotalExperience();
        int expEach = totalExp / nCategories;

        for (TaskCategory category : categories) {
            taskCategoryRepository.addExperience(category.getId(), expEach);
        }

        gainedExperienceResult.experienceEachCategory = expEach;

        if (userTask.isCompleted()) {
            taskCompleted.setAdditionalExperience(gainedExperienceResult.additionalExperience);
            taskCompleted.setBonusPercentage(gainedExperienceResult.bonusPercentage);
            taskCompleted.setExperienceGained(gainedExperienceResult.experienceGained);
            taskCompleted.setExperienceEachCategory(expEach);
            taskCompletedRepository.insertOrUpdateInternal(taskCompleted);
        }

        return gainedExperienceResult;
    }

    /**
     * Delete a task and remove the experience obtained by it from the task categories associated
     * @param fullUserTask full user task object
     * @return if removed return 1, 0 otherwise
     */
    public LiveData<Integer> deleteAndUpdateExperience(FullUserTask fullUserTask) {
        MutableLiveData<Integer> integerMutableLiveData = new MutableLiveData<>();
        if(fullUserTask == null || fullUserTask.getUserTask() == null) {
            integerMutableLiveData.setValue(0);
            return integerMutableLiveData;
        }

        db.databaseWriteExecutor.execute(() -> db.runInTransaction(() -> {
            Integer rows = dao.delete(fullUserTask.getUserTask());
            TaskCompleted taskCompleted = fullUserTask.getTaskCompleted();
            boolean hasExp = taskCompleted != null && taskCompleted.getExperienceEachCategory() > 0;
            if(rows > 0 && hasExp) {
                List<TaskCategory> categories = fullUserTask.getTaskCategory();
                if(categories != null && categories.size() > 0) {
                    for (TaskCategory category : categories) {
                        taskCategoryRepository.addExperience(category.getId(), -taskCompleted.getExperienceEachCategory());
                    }
                }
            }

            integerMutableLiveData.postValue(rows);
        }));

        return integerMutableLiveData;
    }

    public LiveData<List<FullUserTask>> getAllFullTasksWhereBeginBetween(long start, long end) {
        return dao.getAllFullTasksEndDateBetween(start, end);
    }

    public LiveData<FullUserTask> getFullTasks(long id) {
        return dao.getFullTasks(id);
    }

    /**
     * Used during insertOrUpdate, calculate which categories must be added and which one will be removed from the task
     * @param prevRef Previously owned categories ref
     * @param newCat Newly given categories
     * @param toRemove Input list, will be filled with categories ref to remove
     * @param toAdd Input list, will be filled with categories ref to add
     * @param taskId Task id of the given task
     */
    private void calculateTaskCategoryRefLists(List<TaskCategoryRef> prevRef, List<TaskCategory> newCat, List<TaskCategoryRef> toRemove, List<TaskCategoryRef> toAdd, long taskId) {
        for(int i = 0; i < prevRef.size(); i++) {
            long prevId = prevRef.get(i).getSecondId();
            boolean exists = false;
            for (TaskCategory category : newCat) {
                if(category.getId() == prevId) {
                    exists = true;
                    break;
                }
            }

            if(!exists) {
                toRemove.add(prevRef.remove(i));
                i--;
            }
        }

        for(int i = 0; i < newCat.size(); i++) {
            TaskCategory currCategory = newCat.get(i);
            boolean exists = false;
            for (TaskCategoryRef ref : prevRef) {
                if(ref.getSecondId() == currCategory.getId()) {
                    exists = true;
                    break;
                }
            }

            if(!exists) {
                TaskCategoryRef ref = new TaskCategoryRef();
                ref.setSecondId(currCategory.getId());
                ref.setFirstId(taskId);
                toAdd.add(ref);
            }
        }
    }
}
