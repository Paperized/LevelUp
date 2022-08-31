package it.ilogreco.levelup.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Objects;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.utils.TaskDifficultyType;
import it.ilogreco.levelup.entity.UserTask;
import it.ilogreco.levelup.utils.LevelUpUtils;

/**
 * Adapter used to list all FullUserTasks with recycler view, shows Title, Icon, Difficulty and Categories
 */
public class TaskAdapter extends ListAdapter<FullUserTask, TaskAdapter.TaskViewHolder> {
    public TaskAdapter() {
        super(new ListDiff());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_edit_task, viewGroup, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder taskViewHolder, int i) {
        FullUserTask fullTask = getItem(i);
        UserTask task = fullTask.getUserTask();

        String iconUrl = task.getIcon();
        if(iconUrl == null || iconUrl.equals("")) {
            taskViewHolder.taskImage.setImageDrawable(getDefaultIcon(taskViewHolder.getContext()));
        } else if(iconUrl.startsWith("+id/")) {
            int start_substring = 4;
            taskViewHolder.taskImage.setImageDrawable(
                    getIconsById(iconUrl.substring(start_substring), taskViewHolder.getContext()));
        } else {
            Uri uri = Uri.parse(iconUrl);
            taskViewHolder.taskImage.setImageURI(uri);
        }

        TaskDifficultyType td = task.getDifficultyType();
        taskViewHolder.difficultyText.setText(LevelUpUtils.taskDifficultyToString(td));
        taskViewHolder.difficultyText.setChipBackgroundColor(ColorStateList.valueOf(LevelUpUtils.taskDifficultyToColor(td)));

        taskViewHolder.titleText.setText(task.getTitle());

        List<TaskCategory> tc = fullTask.getTaskCategory();
        if(tc == null) return;
        LayoutInflater inflater = LayoutInflater.from(taskViewHolder.getContext());
        taskViewHolder.chipGroup.removeAllViews();

        for(TaskCategory category : tc) {
            Chip chip = (Chip) inflater.inflate(R.layout.small_chip_no_pad, null, false);
            chip.setText(category.getName());

            String iconName = category.getIcon();
            if(iconName != null && iconName.startsWith("+id/")) {
                int start_substring = 4;
                chip.setChipIcon(getIconsById(iconName.substring(start_substring), taskViewHolder.getContext()));
            }

            taskViewHolder.chipGroup.addView(chip);
        }
    }

    @NonNull
    public FullUserTask getItem(int i) {
        return super.getItem(i);
    }

    private Drawable getIconsById(String str, Context context) {
        try {
            int id = Integer.parseInt(str);
            Drawable drawable = AppCompatResources.getDrawable(context, id);
            return drawable == null ? getDefaultIcon(context) : drawable;
        } catch(NumberFormatException e) {
            return null;
        }
    }

    private Drawable getDefaultIcon(Context context) {
        return AppCompatResources.getDrawable(context, android.R.drawable.ic_menu_compass);
    }

    // VIEW HOLDER
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView taskImage;
        TextView titleText;
        ChipGroup chipGroup;
        Chip difficultyText;

        TaskViewHolder(View itemView) {
            super(itemView);
            taskImage = itemView.findViewById(R.id.imageCategory);
            titleText = itemView.findViewById(R.id.titleText);
            difficultyText = itemView.findViewById(R.id.difficultyText);
            chipGroup = itemView.findViewById(R.id.chipGroup);
        }

        public Context getContext() {
            return taskImage.getContext();
        }
    }

    // DIFF BETWEEN ITEMS
    private static class ListDiff extends DiffUtil.ItemCallback<FullUserTask> {
        @Override
        public boolean areItemsTheSame(@NonNull FullUserTask userTask, @NonNull FullUserTask t1) {
            return userTask.getUserTask().getId() == t1.getUserTask().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FullUserTask userTask, @NonNull FullUserTask t1) {
            return equalsTask(userTask.getUserTask(), t1.getUserTask())
                    && equalsTaskCategory(userTask.getTaskCategory(), t1.getTaskCategory());
        }

        private boolean equalsTask(UserTask t1, UserTask t2) {
            return Objects.equals(t1.getTitle(), t2.getTitle())
                    && Objects.equals(t1.getIcon(), t2.getIcon())
                    && t1.getDifficultyType() == t2.getDifficultyType();
        }

        private boolean equalsTaskCategory(List<TaskCategory> t1, List<TaskCategory> t2) {
            if(t1 == null && t2 == null) return true;
            if(t1 == null || t2 == null) return false;
            for(TaskCategory c1 : t1) {
                for(TaskCategory c2 : t2) {
                    if(!c1.getName().equals(c2.getName()) || !c1.getIcon().equals(c2.getIcon()))
                        return false;
                }
            }

            return true;
        }
    }
}
