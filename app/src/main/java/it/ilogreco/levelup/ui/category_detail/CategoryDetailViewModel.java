package it.ilogreco.levelup.ui.category_detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.TaskCategoryRepository;
import it.ilogreco.levelup.utils.LiveDataExt;

public class CategoryDetailViewModel extends AndroidViewModel implements Observer<TaskCategory> {
    private final TaskCategoryRepository userCategoryRepository;
    public final MutableLiveData<TaskCategory> categoryLiveData;

    public boolean editMode;
    public Integer iconSelected;

    public CategoryDetailViewModel(@NonNull Application application) {
        super(application);

        userCategoryRepository = BaseRepository.getInstance(TaskCategoryRepository.class, application);
        categoryLiveData = new MutableLiveData<>();
    }


    public boolean isCategoryNew() {
        return doCategoryExists() && categoryLiveData.getValue().getId() <= 0;
    }

    public TaskCategory getCurrentCategory() {
        return categoryLiveData.getValue();
    }

    public boolean doCategoryExists() {
        return categoryLiveData.getValue() != null;
    }

    public void resetUIState() {
        editMode = false;
        iconSelected = -1;
    }

    public void loadCategoryById(long id) {
        if(id < 0) {
            onChanged(null);
            return;
        }

        LiveDataExt.observeOnce(userCategoryRepository.get(id), this);
    }

    public void saveOrUpdateCategory() {
        final TaskCategory category = categoryLiveData.getValue();
        if(category == null) return;

        LiveDataExt.observeOnce(userCategoryRepository.saveOrUpdateWithoutExperience(category), this::loadCategoryById);
    }

    public void deleteCategory(Observer<Integer> callback) {
        if(doCategoryExists() && !isCategoryNew()) {
            LiveDataExt.observeOnce(userCategoryRepository.delete(getCurrentCategory()), callback);
        }
    }

    // Load a new task if it exists, create it otherwise
    @Override
    public void onChanged(TaskCategory taskCategory) {
        if(taskCategory != null)
            categoryLiveData.setValue(taskCategory);
        else {
            categoryLiveData.setValue(new TaskCategory());
        }
    }
}