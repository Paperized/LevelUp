package it.ilogreco.levelup.entity;

import androidx.room.Entity;

import it.ilogreco.levelup.entity.utils.BaseEntity;

/**
 * A TaskCategory can be assigned to tasks, each task is rewarded with experience on the specific category
 */
@Entity(tableName = "TaskCategory")
public class TaskCategory extends BaseEntity {
    private String icon;
    private String name;
    private int totalExperience;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }
}
