package it.ilogreco.levelup.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import java.util.Calendar;

import it.ilogreco.levelup.entity.utils.BaseEntity;
import it.ilogreco.levelup.entity.utils.TaskDifficultyType;
import it.ilogreco.levelup.entity.utils.UserTaskType;

/**
 * Main Task table
 */
@Entity(tableName = "UserTask")
public class UserTask extends BaseEntity {
    private String icon;
    private String title;
    private String description;
    private Calendar beginDate;
    private Calendar endDate;
    private boolean completed;

    // default is generic
    private int type;
    @NonNull
    private TaskDifficultyType difficultyType;

    @ColumnInfo(name = "experiencePoints")
    private int pointsPrize;

    public UserTask() {
        type = UserTaskType.Generic;
        difficultyType = TaskDifficultyType.F;
    }

    @NonNull
    public TaskDifficultyType getDifficultyType() {
        return difficultyType;
    }

    public void setDifficultyType(@NonNull TaskDifficultyType difficultyType) {
        this.difficultyType = difficultyType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPointsPrize() {
        return pointsPrize;
    }

    public void setPointsPrize(int pointsPrize) {
        this.pointsPrize = pointsPrize;
    }

    public Calendar getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Calendar beginDate) {
        this.beginDate = beginDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }
}
