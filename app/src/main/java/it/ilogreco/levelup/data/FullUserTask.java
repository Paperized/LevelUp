package it.ilogreco.levelup.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

import it.ilogreco.levelup.entity.LocalizationTask;
import it.ilogreco.levelup.entity.StepCounterTask;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.TaskCategoryRef;
import it.ilogreco.levelup.entity.TaskCompleted;
import it.ilogreco.levelup.entity.UserTask;

/**
 * Aggregated data for a Task, it's a representation of a full tasks.
 * It contains UserTask, a list of TaskCategories, a TaskCompleted (if the task was completed successfully), and a possible specialization such as StepCounter, Localization and more to be added.
 */
public class FullUserTask {
    @Embedded
    private UserTask userTask;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(value = TaskCategoryRef.class, entityColumn = "secondId", parentColumn = "firstId")
    )
    private List<TaskCategory> taskCategory;

    @Relation(
            parentColumn = "id",
            entityColumn = "taskId"
    )
    private TaskCompleted taskCompleted;

    // Possible specializations, choose based on userTask.type

    @Relation(
            parentColumn = "id",
            entityColumn = "taskId"
    )
    private StepCounterTask stepCounterTask;

    @Relation(
            parentColumn = "id",
            entityColumn = "taskId"
    )
    private LocalizationTask localizationTask;

    // Add more specs

    public UserTask getUserTask() {
        return userTask;
    }

    public void setUserTask(UserTask userTask) {
        this.userTask = userTask;
    }

    public StepCounterTask getStepCounterTask() {
        return stepCounterTask;
    }

    public void setStepCounterTask(StepCounterTask stepCounterTask) {
        this.stepCounterTask = stepCounterTask;
    }

    public List<TaskCategory> getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(List<TaskCategory> taskCategory) {
        this.taskCategory = taskCategory;
    }

    public TaskCompleted getTaskCompleted() {
        return taskCompleted;
    }

    public void setTaskCompleted(TaskCompleted taskCompleted) {
        this.taskCompleted = taskCompleted;
    }

    public LocalizationTask getLocalizationTask() {
        return localizationTask;
    }

    public void setLocalizationTask(LocalizationTask localizationTask) {
        this.localizationTask = localizationTask;
    }
}
