package it.ilogreco.levelup;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

/**
 * Settings preferences fragment
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}