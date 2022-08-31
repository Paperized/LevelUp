package it.ilogreco.levelup.ui.category;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.TaskCategoryRepository;

/**
 * View Model containing a LiveData of task categories
 */
public class CategoryListViewModel extends AndroidViewModel {
    public final LiveData<List<TaskCategory>> categories;

    public CategoryListViewModel(@NonNull Application application) {
        super(application);

        categories = BaseRepository.getInstance(TaskCategoryRepository.class, application).getAll();
    }
}