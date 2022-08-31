package it.ilogreco.levelup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.UserTask;

/**
 * Used with a RecyclerView to show a grid of icons based on ids (Used in TaskCategory and UserTask fragment forms to pick an icon)
 */
public class GridIconAdapter extends ListAdapter<Integer, GridIconAdapter.GridIconView> {


    public GridIconAdapter() {
        super(new ListDiff());
    }

    @NonNull
    @Override
    public GridIconAdapter.GridIconView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_icon_pick_grid, viewGroup, false);
        return new GridIconAdapter.GridIconView(v);
    }

    public Integer getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public void onBindViewHolder(@NonNull GridIconAdapter.GridIconView holder, int position) {
        Integer resId = getItem(position);
        if(resId == -1) {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            holder.imageView.setImageResource(resId);
        }
    }

    public static class GridIconView extends RecyclerView.ViewHolder {
        ImageView imageView;

        GridIconView(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewIcon);
        }

        public Context getContext() {
            return imageView.getContext();
        }
    }

    // DIFF BETWEEN ITEMS
    private static class ListDiff extends DiffUtil.ItemCallback<Integer> {

        @Override
        public boolean areItemsTheSame(@NonNull Integer oldItem, @NonNull Integer newItem) {
            return Objects.equals(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Integer oldItem, @NonNull Integer newItem) {
            return Objects.equals(oldItem, newItem);
        }
    }
}
