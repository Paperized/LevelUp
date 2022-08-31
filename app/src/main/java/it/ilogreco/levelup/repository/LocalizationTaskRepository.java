package it.ilogreco.levelup.repository;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.ilogreco.levelup.dao.LocalizationTaskDao;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.data.GainedExperienceResult;
import it.ilogreco.levelup.entity.LocalizationTask;
import it.ilogreco.levelup.entity.utils.AddressAdapterData;
import it.ilogreco.levelup.utils.LiveDataExt;

/**
 * Localization Repository, it holds all CRUD functionalities.
 * All android location API are managed here, such as getting addresses by name.
 */
public class LocalizationTaskRepository extends BaseRepository<LocalizationTask, LocalizationTaskDao> {
    private final Geocoder geocoder;
    public final ExecutorService addressExecutorService =
            Executors.newSingleThreadExecutor();

    private UserTaskRepository userTaskRepository;

    protected LocalizationTaskRepository(Application app) {
        super(app);

        if(!Geocoder.isPresent()) geocoder = null;
        else geocoder = new Geocoder(app.getApplicationContext(), Locale.getDefault());
    }

    @Override
    protected LocalizationTaskDao initPrimaryDao() {
        return db.localizationTaskDao();
    }

    @Override
    protected void requireAdditionalRepositories() {
        userTaskRepository = getInstance(UserTaskRepository.class, app);
    }

    public void getAddressesByName(String name, int maxResults, Observer<List<AddressAdapterData>> callback) {
        MutableLiveData<List<AddressAdapterData>> mutableLiveData = new MutableLiveData<>();
        LiveDataExt.observeOnce(mutableLiveData, callback);

        if(geocoder == null) {
            mutableLiveData.setValue(List.of());
            return;
        }

        addressExecutorService.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(name, maxResults);
                List<AddressAdapterData> list = AddressAdapterData.fromAddresses(addresses);
                mutableLiveData.postValue(list);
            } catch (IOException e) {
                e.printStackTrace();
                mutableLiveData.postValue(List.of());
            }
        });
    }

    public LiveData<List<AddressAdapterData>> getNewestAddresses(int maxResults) {
        return dao.getNewestAddresses(maxResults);
    }

    public LiveData<FullUserTask> getFullOrderedByBeginDate(long startFrom) {
        return dao.getFullOrderedByBeginDate(startFrom);
    }

    public LiveData<GainedExperienceResult> markAsComplete(FullUserTask userTask, boolean succeeded) {
        return userTaskRepository.markAsComplete(userTask, succeeded);
    }
}
