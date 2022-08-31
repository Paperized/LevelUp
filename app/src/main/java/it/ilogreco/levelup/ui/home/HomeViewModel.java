package it.ilogreco.levelup.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.Calendar;
import java.util.List;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.UserTaskRepository;
import it.ilogreco.levelup.utils.CalendarExt;

/**
 * View Model that hold a LiveData of full user tasks filtered by date
 */
public class HomeViewModel extends AndroidViewModel implements Observer<List<FullUserTask>> {
    private final UserTaskRepository userTaskRepository;

    public final Calendar tasksDay;
    private final Calendar lastSearchDate;
    public final MutableLiveData<List<FullUserTask>> tasks;
    private LiveData<List<FullUserTask>> internalTasks;

    public HomeViewModel(@NonNull Application application) {
        super(application);

        tasksDay = Calendar.getInstance();
        lastSearchDate = Calendar.getInstance();
        userTaskRepository = BaseRepository.getInstance(UserTaskRepository.class, application);
        tasks = new MutableLiveData<>();
        forceUpdateTasks();
    }

    public void updateTasks() {
        if(!CalendarExt.areCalendarsDateSame(tasksDay, lastSearchDate)) {
            forceUpdateTasks();
        }
    }

    private void forceUpdateTasks() {
        lastSearchDate.set(tasksDay.get(Calendar.YEAR), tasksDay.get(Calendar.MONTH), tasksDay.get(Calendar.DAY_OF_MONTH));

        if(internalTasks != null)
            internalTasks.removeObserver(this);

        internalTasks = userTaskRepository.getAllFullTasksWhereBeginBetween(
                CalendarExt.getDayMillis(lastSearchDate), CalendarExt.getNextDayMillis(lastSearchDate));
        internalTasks.observeForever(this);
    }

    @Override
    public void onChanged(List<FullUserTask> userTask) {
        tasks.setValue(userTask);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if(internalTasks != null)
            internalTasks.removeObserver(this);
    }
}