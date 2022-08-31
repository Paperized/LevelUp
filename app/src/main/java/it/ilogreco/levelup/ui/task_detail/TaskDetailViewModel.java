package it.ilogreco.levelup.ui.task_detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.data.GainedExperienceResult;
import it.ilogreco.levelup.entity.LocalizationTask;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.TaskCompleted;
import it.ilogreco.levelup.entity.UserTask;
import it.ilogreco.levelup.entity.utils.AddressAdapterData;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.LocalizationTaskRepository;
import it.ilogreco.levelup.repository.TaskCategoryRepository;
import it.ilogreco.levelup.repository.UserTaskRepository;
import it.ilogreco.levelup.utils.LiveDataExt;

/**
 * View model for the task detail fragment, it has some functionalities to interact with the repository and some variable for UI state
 */
public class TaskDetailViewModel extends AndroidViewModel implements Observer<FullUserTask> {
    private final UserTaskRepository userTaskRepository;
    private final LocalizationTaskRepository localizationTaskRepository;
    public final MutableLiveData<FullUserTask> userTaskLiveData;
    public final LiveData<List<TaskCategory>> categoriesLiveData;
    public final MutableLiveData<Long> onSaveCompletedLiveData;

    // UI state ///////////////////
    public Calendar latestBeginDateSelected;
    public Calendar latestEndDateSelected;
    public boolean isCompleted;
    public Integer iconSelected;
    public boolean editMode;
    public AddressAdapterData selectedAddress;
    public final MutableLiveData<Integer> userTaskTypeSelected;
    ///////////////////////////////

    public TaskDetailViewModel(@NonNull Application application) {
        super(application);

        userTaskTypeSelected = new MutableLiveData<>();
        userTaskLiveData = new MutableLiveData<>();
        onSaveCompletedLiveData = new MutableLiveData<>();
        userTaskRepository = BaseRepository.getInstance(UserTaskRepository.class, application);
        localizationTaskRepository = BaseRepository.getInstance(LocalizationTaskRepository.class, application);
        TaskCategoryRepository taskCategoryRepository = BaseRepository.getInstance(TaskCategoryRepository.class, application);

        categoriesLiveData = taskCategoryRepository.getAll();
    }

    public boolean isTaskNew() {
        return doTaskExists() && userTaskLiveData.getValue().getUserTask().getId() <= 0;
    }

    public FullUserTask getCurrentTask() {
        return userTaskLiveData.getValue();
    }

    public boolean doTaskExists() {
        return userTaskLiveData.getValue() != null && userTaskLiveData.getValue().getUserTask() != null;
    }

    public void resetUIState() {
        editMode = false;
        latestBeginDateSelected = null;
        latestEndDateSelected = null;
        isCompleted = false;
        iconSelected = -1;
        selectedAddress = null;
        userTaskTypeSelected.setValue(UserTaskType.Generic);
    }

    public void loadTaskById(long id) {
        if(id < 0) {
            onChanged(null);
            return;
        }

        LiveDataExt.observeOnce(userTaskRepository.getFullTasks(id), this);
    }

    public void saveOrUpdateTask(FullUserTask fullUserTask) {
        if(fullUserTask == null) return;
        if(!isTaskNew()) {
            fullUserTask.getUserTask().setId(getCurrentTask().getUserTask().getId());
        }

        LiveDataExt.observeOnce(userTaskRepository.insertOrUpdateFullUserTask(fullUserTask), this::onSaveCompleted);
    }

    private void onSaveCompleted(Long id) {
        onSaveCompletedLiveData.setValue(id);
        loadTaskById(id);
    }

    public boolean updateTaskAsCompleted(Observer<GainedExperienceResult> callback) {
        final FullUserTask fullUserTask = userTaskLiveData.getValue();
        if(fullUserTask == null) return false;

        LiveData<GainedExperienceResult> resultLiveData = userTaskRepository.markAsComplete(fullUserTask, true);
        LiveDataExt.observeOnce(resultLiveData, this::onTaskCompleted);
        LiveDataExt.observeOnce(resultLiveData, callback);
        return true;
    }

    public void onTaskCompleted(GainedExperienceResult gainedExperienceResult) {
        if(gainedExperienceResult == null) {
            onChanged(null);
        }
    }

    public void deleteTask(Observer<Integer> callback) {
        if(doTaskExists() && !isTaskNew()) {
            LiveDataExt.observeOnce(userTaskRepository.deleteAndUpdateExperience(getCurrentTask()), callback);
        }
    }

    public void getAddressesByName(String name, Observer<List<AddressAdapterData>> callback) {
        localizationTaskRepository.getAddressesByName(name, 5, callback);
    }

    public void getNewestAddresses(Observer<List<AddressAdapterData>> callback) {
        LiveDataExt.observeOnce(localizationTaskRepository.getNewestAddresses(5), callback);
    }

    // Load a new task if it exists, create it otherwise
    @Override
    public void onChanged(FullUserTask userTask) {
        if(userTask != null) {
            latestBeginDateSelected = userTask.getUserTask().getBeginDate();
            if(latestBeginDateSelected != null)
                latestBeginDateSelected = (Calendar) latestBeginDateSelected.clone();

            latestEndDateSelected = userTask.getUserTask().getEndDate();
            if(latestEndDateSelected != null)
                latestEndDateSelected = (Calendar) latestEndDateSelected.clone();

            isCompleted = userTask.getUserTask().isCompleted();
            selectedAddress = null;
            LocalizationTask localizationTask = userTask.getLocalizationTask();
            if(localizationTask != null && localizationTask.isPlaceValid()) {
                selectedAddress = localizationTask.getAsAddressAdapterData();
            } else {
                selectedAddress = null;
            }

            userTaskTypeSelected.setValue(userTask.getUserTask().getType());
            userTaskLiveData.setValue(userTask);
        } else {
            FullUserTask fullUserTask = new FullUserTask();
            UserTask task = new UserTask();

            fullUserTask.setUserTask(task);
            fullUserTask.setTaskCategory(new ArrayList<>());

            isCompleted = false;

            userTaskTypeSelected.setValue(UserTaskType.Generic);
            userTaskLiveData.setValue(fullUserTask);
        }
    }
}