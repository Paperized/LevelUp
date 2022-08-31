package it.ilogreco.levelup.utils;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;

/**
 * Custom EditTextPreferences for float numbers
 */
public class FloatEditTextPreference extends EditTextPreference {

    public FloatEditTextPreference(Context context) {
        super(context);
    }

    public FloatEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setText(String text) {
        try {
            float f = Float.parseFloat(text);
        } catch (NumberFormatException ex) {
            text = "" + getPersistedFloat(0f);
        }

        super.setText(text);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedFloat(0f));
    }

    @Override
    protected boolean persistString(String value) {
        try {
            return persistFloat(Float.parseFloat(value));
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
