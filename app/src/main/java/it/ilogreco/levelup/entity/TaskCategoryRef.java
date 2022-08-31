package it.ilogreco.levelup.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import it.ilogreco.levelup.entity.utils.CombinedEntity;

/**
 * Many to Many table to associate more Tasks with more Categories
 */
@Entity(primaryKeys = {"firstId", "secondId"},
    foreignKeys = {@ForeignKey(entity = UserTask.class, parentColumns = "id", childColumns = "firstId", onDelete = CASCADE),
            @ForeignKey(entity = TaskCategory.class, parentColumns = "id", childColumns = "secondId", onDelete = CASCADE)},
    indices = {@Index("secondId")})
public class TaskCategoryRef extends CombinedEntity {

}
