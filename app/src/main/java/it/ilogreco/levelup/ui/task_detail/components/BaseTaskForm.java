package it.ilogreco.levelup.ui.task_detail.components;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import androidx.fragment.app.FragmentManager;

import java.lang.ref.WeakReference;

import it.ilogreco.levelup.databinding.FragmentTaskDetailBinding;
import it.ilogreco.levelup.ui.task_detail.TaskDetailViewModel;
import it.ilogreco.levelup.ui.task_detail.UserTaskDetailFragment;

public abstract class BaseTaskForm<T> {
    protected final WeakReference<UserTaskDetailFragment> fragmentWeakReference;
    protected final TaskDetailViewModel taskDetailViewModel;

    protected boolean isLoaded;

    public BaseTaskForm(UserTaskDetailFragment userTaskDetailFragment) {
        fragmentWeakReference = new WeakReference<>(userTaskDetailFragment);
        this.taskDetailViewModel = userTaskDetailFragment.getViewModel();
    }

    public abstract void init();

    public void onResume() { }

    public boolean validate() {
        return true;
    }

    public T getValue() {
        return null;
    }

    public void setValue(T value) {}

    public void onEdit(boolean editMode) { }

    public void onPause() {

    }

    protected UserTaskDetailFragment getUserFragment() {
        return fragmentWeakReference.get();
    }

    protected Activity requireActivity() {
        UserTaskDetailFragment userTaskDetailFragment = fragmentWeakReference.get();
        if(userTaskDetailFragment == null)
            return null;

        return userTaskDetailFragment.requireActivity();
    }

    protected FragmentManager getChildFragmentManager() {
        UserTaskDetailFragment userTaskDetailFragment = fragmentWeakReference.get();
        if(userTaskDetailFragment == null)
            return null;

        return userTaskDetailFragment.getChildFragmentManager();
    }

    // Dont store this context anywhere
    protected Context getContext() {
        UserTaskDetailFragment userTaskDetailFragment = fragmentWeakReference.get();
        if(userTaskDetailFragment == null)
            return null;

        return userTaskDetailFragment.getContext();
    }

    // Dont store this binding anywhere
    protected FragmentTaskDetailBinding getBinding() {
        UserTaskDetailFragment userTaskDetailFragment = fragmentWeakReference.get();
        if(userTaskDetailFragment == null)
            return null;

        return userTaskDetailFragment.getBinding();
    }

    protected Resources getResources() {
        UserTaskDetailFragment userTaskDetailFragment = fragmentWeakReference.get();
        if(userTaskDetailFragment == null)
            return null;

        return userTaskDetailFragment.getResources();
    }
}
