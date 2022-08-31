package it.ilogreco.levelup.ui.metrics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import it.ilogreco.levelup.data.UserMetrics;
import it.ilogreco.levelup.repository.BaseRepository;
import it.ilogreco.levelup.repository.UserTaskRepository;
import it.ilogreco.levelup.utils.LiveDataExt;

public class MetricsViewModel extends AndroidViewModel {
    private final UserTaskRepository userTaskRepository;

    public MetricsViewModel(@NonNull Application application) {
        super(application);

        userTaskRepository = BaseRepository.getInstance(UserTaskRepository.class, application);
    }

    public void getMetricsOnce(Observer<UserMetrics> observer) {
        LiveDataExt.observeOnce(userTaskRepository.getUserMetrics(), observer);
    }
}