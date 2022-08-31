package it.ilogreco.levelup.entity;

import static androidx.room.ForeignKey.CASCADE;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.util.Calendar;
import java.util.List;

import it.ilogreco.levelup.entity.utils.BaseEntity;

/**
 * This entity is created after a task is created successfully, it holds a list of photos (Uri), a description and the experience gained for completion.
 */
@Entity(foreignKeys = {@ForeignKey(entity = UserTask.class, parentColumns = "id", childColumns = "taskId", onDelete = CASCADE)},
    indices = {@Index("taskId")})
public class TaskCompleted extends BaseEntity {
    private List<Uri> photos;
    private String description;
    private Calendar completionDate;
    private int experienceGained;
    private int additionalExperience;
    private int experienceEachCategory;
    private float bonusPercentage;
    private long taskId;

    public TaskCompleted() {
        completionDate = Calendar.getInstance();
    }

    public Calendar getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Calendar completionDate) {
        this.completionDate = completionDate;
    }

    public List<Uri> getPhotos() {
        return photos;
    }

    public int getExperienceGained() {
        return experienceGained;
    }

    public void setExperienceGained(int experienceGained) {
        this.experienceGained = experienceGained;
    }

    public int getAdditionalExperience() {
        return additionalExperience;
    }

    public void setAdditionalExperience(int additionalExperience) {
        this.additionalExperience = additionalExperience;
    }

    public int getExperienceEachCategory() {
        return experienceEachCategory;
    }

    public void setExperienceEachCategory(int experienceEachCategory) {
        this.experienceEachCategory = experienceEachCategory;
    }

    public float getBonusPercentage() {
        return bonusPercentage;
    }

    public void setBonusPercentage(float bonusPercentage) {
        this.bonusPercentage = bonusPercentage;
    }

    public void setPhotos(List<Uri> photos) {
        this.photos = photos;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public int getTotalExperience() {
        int bonus = (int)(experienceGained * bonusPercentage);
        return experienceGained + bonus + additionalExperience;
    }
}
