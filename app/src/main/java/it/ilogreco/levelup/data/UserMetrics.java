package it.ilogreco.levelup.data;

import java.util.Map;

import it.ilogreco.levelup.entity.TaskCategory;

/**
 * Metrics shown in the profile fragment
 */
public class UserMetrics {
    public int totalTasks; // COUNT(*)
    public int ongoingTasks; // totalTasks - isCompleted
    public int completedTasks; // COUNT (*) isCompleted
    public int successTasks; // COUNT (*) isCompleted JOIN taskCompleted
    public int failedTasks; // completedTasks - successTasks

    public Map<Integer, Integer> tasksPerType; // SELECT type, COUNT(*) FROM Task GROUP BY task.type
    public Map<TaskCategory, Integer> tasksPerCategory; // SELECT cat, COUNT(
}
