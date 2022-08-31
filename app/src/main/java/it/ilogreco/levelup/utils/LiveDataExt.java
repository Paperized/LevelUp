package it.ilogreco.levelup.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

/**
 * Custom extension methods for LiveData
 */
public class LiveDataExt {

    /**
     * List one time then unsubscribe
     * @param ld live data
     * @param obs callback
     * @param <T> type
     */
    public static <T> void observeOnce(LiveData<T> ld, final Observer<T> obs) {
        ld.observeForever(new Observer<T>() {
            @Override
            public void onChanged(T t) {
                ld.removeObserver(this);
                obs.onChanged(t);
            }
        });
    }
}
