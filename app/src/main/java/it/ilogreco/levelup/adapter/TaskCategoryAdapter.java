package it.ilogreco.levelup.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.entity.TaskCategory;

public class TaskCategoryAdapter extends ListAdapter<TaskCategory, TaskCategoryAdapter.TaskCategoryViewHolder> {
    public TaskCategoryAdapter() {
        super(new ListDiff());
    }

    @NonNull
    @Override
    public TaskCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_edit_category, viewGroup, false);
        return new TaskCategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskCategoryViewHolder taskCategoryViewHolder, int i) {
        TaskCategory taskCategory = getItem(i);

        String iconUrl = taskCategory.getIcon();
        if(iconUrl == null) {
            taskCategoryViewHolder.categoryImage.setImageDrawable(getDefaultIcon(taskCategoryViewHolder.getContext()));
        } else if(iconUrl.startsWith("+id/")) {
            int start_substring = 4;
            taskCategoryViewHolder.categoryImage.setImageDrawable(
                    getIconsById(iconUrl.substring(start_substring), taskCategoryViewHolder.getContext()));
        } else {
            Uri uri = Uri.parse(iconUrl);
            taskCategoryViewHolder.categoryImage.setImageURI(uri);
        }

        taskCategoryViewHolder.nameText.setText(taskCategory.getName());
    }

    @NonNull
    public TaskCategory getItem(int i) {
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
        return AppCompatResources.getDrawable(context, R.drawable.ic_menu_camera);
    }

    // VIEW HOLDER
    public static class TaskCategoryViewHolder extends RecyclerView.ViewHolder {
        View backgroundLayout;
        ImageView categoryImage;
        TextView nameText;

        public final Drawable wrapBackgroundLayout;

        TaskCategoryViewHolder(View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.itemCategoryLayout);
            nameText = itemView.findViewById(R.id.titleText);
            categoryImage = itemView.findViewById(R.id.imageCategory);

            wrapBackgroundLayout = DrawableCompat.wrap(backgroundLayout.getBackground()).mutate();
        }

        public Context getContext() {
            return backgroundLayout.getContext();
        }
    }

    // DIFF BETWEEN ITEMS
    private static class ListDiff extends DiffUtil.ItemCallback<TaskCategory> {
        @Override
        public boolean areItemsTheSame(@NonNull TaskCategory t, @NonNull TaskCategory t1) {
            return t.getId() == t1.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaskCategory t, @NonNull TaskCategory t1) {
            return Objects.equals(t.getName(), t1.getName())
                    && Objects.equals(t.getIcon(), t1.getIcon())
                    && t.getTotalExperience() == t1.getTotalExperience();
        }
    }
}
