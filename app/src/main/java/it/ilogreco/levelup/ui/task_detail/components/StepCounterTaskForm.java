package it.ilogreco.levelup.ui.task_detail.components;

import android.content.SharedPreferences;
import android.text.InputType;
import android.view.View;

import androidx.lifecycle.Observer;

import java.util.Locale;

import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.databinding.FragmentTaskDetailBinding;
import it.ilogreco.levelup.entity.StepCounterTask;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.ui.task_detail.UserTaskDetailFragment;
import it.ilogreco.levelup.utils.LevelUpUtils;

public class StepCounterTaskForm extends BaseTaskForm<StepCounterTask> implements Observer<Integer> {

    private float stepDistance;

    public StepCounterTaskForm(UserTaskDetailFragment userTaskDetailFragment) {
        super(userTaskDetailFragment);
    }

    @Override
    public void init() {
        SharedPreferences preferences = LevelUpUtils.initializeDefaultSharedPreferences(getContext());
        if(preferences == null) {
            stepDistance = 0;
        } else {
            stepDistance = preferences.getFloat("KEY_STEP_LENGTH", 0) / 1000;
        }
    }

    @Override
    public void onResume() {
        taskDetailViewModel.userTaskTypeSelected.observeForever(this);
    }

    @Override
    public boolean validate() {
        resetErrors();

        boolean isValid = true;
        if(getBinding().includeStepTask.taskSCGoal.getText().toString().equalsIgnoreCase("")) {
            getBinding().includeStepTask.taskSCGoal.setError("Kilometers required!");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onPause() {
        taskDetailViewModel.userTaskTypeSelected.removeObserver(this);
    }

    @Override
    public StepCounterTask getValue() {
        StepCounterTask stepCounterTask = new StepCounterTask();
        String goalStr = getBinding().includeStepTask.taskSCGoal.getText().toString().replace(',', '.');
        stepCounterTask.setGoalKm(Float.parseFloat(goalStr));
        stepCounterTask.setCurrentSteps(getOriginalSteps());
        return stepCounterTask;
    }

    @Override
    public void setValue(StepCounterTask stepCounterTask) {
        FragmentTaskDetailBinding binding = getBinding();
        if(stepCounterTask == null) stepCounterTask = new StepCounterTask();
        if(!setVisibility(taskDetailViewModel.userTaskTypeSelected.getValue()))
            return;

        resetErrors();
        binding.includeStepTask.taskSCGoal.setText(String.format(Locale.getDefault(), "%.2f", stepCounterTask.getGoalKm()));
        binding.includeStepTask.taskSCCurrentKm.setText(String.format(Locale.getDefault(), "%.2f", stepCounterTask.getCurrentSteps() * stepDistance));
        binding.includeStepTask.taskSCCurrentSteps.setText(String.format(Locale.getDefault(), "%d", stepCounterTask.getCurrentSteps()));
    }

    @Override
    public void onEdit(boolean editMode) {
        FragmentTaskDetailBinding binding = getBinding();
        boolean isEditable = editMode && !taskDetailViewModel.isCompleted;
        final int textDecimalInput = isEditable ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL : InputType.TYPE_NULL;

        binding.includeStepTask.taskSCGoal.setInputType(textDecimalInput);
        binding.includeStepTask.taskSCCurrentKm.setInputType(InputType.TYPE_NULL);
        binding.includeStepTask.taskSCCurrentSteps.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void onChanged(Integer type) {
        setVisibility(type);
    }

    private boolean setVisibility(Integer type) {
        boolean isStepCounter = type != null && type == UserTaskType.StepCounter;
        FullUserTask fullUserTask = taskDetailViewModel.getCurrentTask();
        if(fullUserTask != null && isStepCounter && !isLoaded) {
            isLoaded = true;
            setValue(fullUserTask.getStepCounterTask());
        }
        else {
            int vs = isStepCounter ? View.VISIBLE : View.GONE;
            getBinding().stepTaskWrapper.setVisibility(vs);
        }
        return isStepCounter;
    }

    private void resetErrors() {
        getBinding().includeStepTask.taskSCGoal.setError(null);
    }

    private int getOriginalSteps() {
        if(taskDetailViewModel.doTaskExists()) {
            StepCounterTask stepCounterTask = taskDetailViewModel.getCurrentTask().getStepCounterTask();
            return stepCounterTask == null ? 0 : stepCounterTask.getCurrentSteps();
        }

        return 0;
    }
}
