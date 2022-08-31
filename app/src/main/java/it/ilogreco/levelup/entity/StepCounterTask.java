package it.ilogreco.levelup.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import it.ilogreco.levelup.entity.utils.BaseEntity;

/**
 *  StepCounterTask is a specialized task, it contains the current steps walked and goal (in kilometers).
 *  The current KM walked is calculated by doing currentSteps * stepLength (saved in SharedPreferences)
 */
@Entity(tableName = "StepCounterTask", foreignKeys = {@ForeignKey(entity = UserTask.class, parentColumns = "id", childColumns = "taskId", onDelete = CASCADE)},
    indices = {@Index("taskId")})
public class StepCounterTask extends BaseEntity {
    private int currentSteps;
    private float goalKm;
    private long taskId;

    public int getCurrentSteps() {
        return currentSteps;
    }

    public void setCurrentSteps(int currentSteps) {
        this.currentSteps = currentSteps;
    }

    public float getGoalKm() {
        return goalKm;
    }

    public void setGoalKm(float goalKm) {
        this.goalKm = goalKm;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
