package it.ilogreco.levelup.ui.task_detail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.ilogreco.levelup.MainActivity;
import it.ilogreco.levelup.R;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.data.GainedExperienceResult;
import it.ilogreco.levelup.databinding.FragmentTaskDetailBinding;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.ui.task_detail.components.GeneralTaskForm;
import it.ilogreco.levelup.ui.task_detail.components.LocalizationTaskForm;
import it.ilogreco.levelup.ui.task_detail.components.ResultTaskForm;
import it.ilogreco.levelup.ui.task_detail.components.StepCounterTaskForm;

/**
 * Task Detail Fragment divided in sub-forms that needs to be populated, validated and saved
 * This also has functionalities for Completion and Deletion
 */
public class UserTaskDetailFragment extends Fragment implements Observer<FullUserTask>, View.OnClickListener, MenuProvider {
    public static final String KEY_ID = "KEY_ID";

    private TaskDetailViewModel mViewModel;
    private FragmentTaskDetailBinding binding;

    private androidx.appcompat.app.AlertDialog completionDialog;
    private final DialogInterface.OnClickListener completionDialogListener = this::onCompletionDialogResult;
    private androidx.appcompat.app.AlertDialog deleteDialog;
    private final DialogInterface.OnClickListener deleteDialogListener = this::onDeleteDialogResult;

    private Snackbar snackbar;
    private final List<String> stringsToPrint = new ArrayList<>();

    private GeneralTaskForm generalTaskForm;
    private ResultTaskForm resultTaskForm;
    private StepCounterTaskForm stepCounterTaskForm;
    private LocalizationTaskForm localizationTaskForm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(TaskDetailViewModel.class);
        Bundle args = getArguments();
        mViewModel.resetUIState();
        long id = -1;

        if(args != null) {
            id = args.getLong(KEY_ID, 0);
        }

        // edit mode if it's a new task (id = 0)
        mViewModel.editMode = id <= 0;
        mViewModel.loadTaskById(id);

        binding = FragmentTaskDetailBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(this, getViewLifecycleOwner());

        // Initialize all sub-forms

        generalTaskForm = new GeneralTaskForm(this);
        generalTaskForm.init();

        stepCounterTaskForm = new StepCounterTaskForm(this);
        stepCounterTaskForm.init();

        resultTaskForm = new ResultTaskForm(this);
        resultTaskForm.init();

        localizationTaskForm = new LocalizationTaskForm(this);
        localizationTaskForm.init();

        // Listen to view model changes and setup components

        mViewModel.userTaskLiveData.observe(getViewLifecycleOwner(), this);
        mViewModel.onSaveCompletedLiveData.observe(getViewLifecycleOwner(), this::onSaveCompleted);

        binding.editBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);

        onEditChanged(mViewModel.editMode);
    }

    @Override
    public void onResume() {
        super.onResume();

        generalTaskForm.onResume();
        stepCounterTaskForm.onResume();
        localizationTaskForm.onResume();
        resultTaskForm.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        generalTaskForm.onPause();
        stepCounterTaskForm.onPause();
        localizationTaskForm.onPause();
        resultTaskForm.onPause();
    }

    private void onSaveCompleted(Long id) {
        if(id == null || id == -1) return;

        MainActivity activity = (MainActivity) requireActivity();
        activity.getService().onTaskCounterUpdated(id);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mViewModel.userTaskLiveData.removeObserver(this);
        binding = null;
    }

    @Override
    public void onClick(View view) {
        if (view == binding.editBtn) {
            if (mViewModel.editMode) {
                boolean nextEdit = !saveOrUpdateTask();
                mViewModel.editMode = nextEdit;
                onEditChanged(nextEdit);
            } else {
                mViewModel.editMode = true;
                onEditChanged(true);
            }
        } else if (view == binding.cancelBtn) {
            mViewModel.editMode = false;
            onEditChanged(false);

            // reload task
            onChanged(mViewModel.userTaskLiveData.getValue());
        }
    }

    public void onEditChanged(boolean editMode) {
        generalTaskForm.onEdit(editMode);
        stepCounterTaskForm.onEdit(editMode);
        localizationTaskForm.onEdit(editMode);
        resultTaskForm.onEdit(editMode);

        binding.cancelBtn.setVisibility(editMode ? View.VISIBLE : View.INVISIBLE);

        if(editMode) {
            binding.editBtn.setIcon(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_save, null));
        } else {
            binding.editBtn.setIcon(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_edit, null));
        }

        requireActivity().invalidateMenu();
    }

    private boolean validateForm() {
        int type = mViewModel.userTaskTypeSelected.getValue();
        boolean isValid = generalTaskForm.validate();
        if(type == UserTaskType.StepCounter && !stepCounterTaskForm.validate())
            isValid = false;
        if(type == UserTaskType.Localization && !localizationTaskForm.validate())
            isValid = false;
        if(mViewModel.isCompleted && mViewModel.getCurrentTask().getTaskCompleted() != null && !resultTaskForm.validate())
            isValid = false;

        return isValid;
    }

    private FullUserTask getFormValue() {
        FullUserTask fullUserTask = new FullUserTask();
        fullUserTask.setUserTask(generalTaskForm.getValue());
        fullUserTask.setTaskCategory(new ArrayList<>());
        generalTaskForm.getCategories(fullUserTask.getTaskCategory());

        if (fullUserTask.getUserTask().getType() == UserTaskType.StepCounter)
            fullUserTask.setStepCounterTask(stepCounterTaskForm.getValue());
        else if (fullUserTask.getUserTask().getType() == UserTaskType.Localization)
            fullUserTask.setLocalizationTask(localizationTaskForm.getValue());

        // set the task completed only if is not null (aka was completed successfully)
        if (mViewModel.isCompleted && mViewModel.getCurrentTask().getTaskCompleted() != null) {
            fullUserTask.setTaskCompleted(resultTaskForm.getValue());
        }

        return fullUserTask;
    }

    public boolean saveOrUpdateTask() {
        boolean validate = validateForm();
        if(validate)
            mViewModel.saveOrUpdateTask(getFormValue());
        else
            Snackbar.make(getView(), "Some required fields missing!", Snackbar.LENGTH_SHORT).show();

        return validate;
    }

    @Override
    public void onChanged(FullUserTask userTask) {
        generalTaskForm.setValue(userTask.getUserTask());
        generalTaskForm.setCategoriesValue(userTask.getTaskCategory());

        stepCounterTaskForm.setValue(userTask.getStepCounterTask());
        localizationTaskForm.setValue(userTask.getLocalizationTask());

        resultTaskForm.setValue(userTask.getTaskCompleted());

        requireActivity().invalidateMenu();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        // mark as complete button is available IF a task exist, must be a generic one, isn't completed or new or in edit mode
        menu.findItem(R.id.action_mark_complete).setVisible(
                mViewModel.doTaskExists() &&
                mViewModel.getCurrentTask().getUserTask().getType() == UserTaskType.Generic
                && !mViewModel.isCompleted && !mViewModel.isTaskNew() && !mViewModel.editMode);
        // delete is available if exists and not new
        menu.findItem(R.id.action_delete).setVisible(mViewModel.doTaskExists() && !mViewModel.isTaskNew());
        MenuProvider.super.onPrepareMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.action_mark_complete) {
            getMarkCompleteDialog().show();
            return true;
        } else if(menuItem.getItemId() == R.id.action_delete) {
            getDeleteDialog().show();
            return true;
        }
        return false;
    }

    private AlertDialog getMarkCompleteDialog() {
        if(completionDialog == null) {
            completionDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.completion_dialog_title)
                    .setMessage(R.string.completion_dialog_message)
                    .setNegativeButton(R.string.completion_dialog_negative, completionDialogListener)
                    .setPositiveButton(R.string.completion_dialog_positive, completionDialogListener)
                    .create();
        }

        return completionDialog;
    }

    private void onCompletionDialogResult(DialogInterface dialogInterface, int i) {
        if(i == DialogInterface.BUTTON_POSITIVE) {
            mViewModel.isCompleted = true;
            requireActivity().invalidateMenu();

            // mark this task as completed and wait for a callback reward at this::onRewardReceived
            mViewModel.updateTaskAsCompleted(this::onRewardReceived);
        }

        dialogInterface.dismiss();
    }

    private AlertDialog getDeleteDialog() {
        if(deleteDialog == null) {
            deleteDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_dialog_title)
                    .setMessage(R.string.delete_dialog_message)
                    .setNegativeButton(R.string.delete_dialog_negative, deleteDialogListener)
                    .setPositiveButton(R.string.delete_dialog_positive, deleteDialogListener)
                    .create();
        }

        return deleteDialog;
    }

    private void onDeleteDialogResult(DialogInterface dialogInterface, int i) {
        if(i == DialogInterface.BUTTON_POSITIVE) {
            mViewModel.deleteTask(integer -> {
                if(integer > 0) {
                    Snackbar.make(requireView(), "User task deleted!", Snackbar.LENGTH_SHORT).show();

                    NavController navController = Navigation.findNavController(UserTaskDetailFragment.this.requireActivity(), R.id.nav_host_fragment_content_main);
                    navController.popBackStack();
                } else {
                    Snackbar.make(requireView(), "User task cannot be deleted :\\!", Snackbar.LENGTH_SHORT).show();
                }
            });
        }

        dialogInterface.dismiss();
    }

    /**
     * Callback reward, set the completed as true, create a result task entity and show Snackbar experience messages
     * @param gainedExperienceResult experience
     */
    private void onRewardReceived(GainedExperienceResult gainedExperienceResult) {
        if(gainedExperienceResult == null) {
            if(mViewModel.doTaskExists()) {
                mViewModel.isCompleted = mViewModel.getCurrentTask().getUserTask().isCompleted();
            } else {
                mViewModel.isCompleted = false;
            }

            requireActivity().invalidateMenu();
            return;
        }

        resultTaskForm.setValue(mViewModel.getCurrentTask().getTaskCompleted());

        if(gainedExperienceResult.wasGiven) {
            String baseExpStr, bonusExpStr, addExpStr;
            baseExpStr = String.format(Locale.getDefault(), "You gained %d experience by completing your task", gainedExperienceResult.experienceGained);
            bonusExpStr = gainedExperienceResult.bonusPercentage == 0 ?
                    null : String.format(Locale.getDefault(), "You found a %d%% boost!", (int)(gainedExperienceResult.bonusPercentage * 100));
            addExpStr = gainedExperienceResult.additionalExperience == 0 ?
                    null : String.format(Locale.getDefault(), "You gained %d additional experience!", gainedExperienceResult.additionalExperience);

            stringsToPrint.add(baseExpStr);
            if(bonusExpStr != null)
                stringsToPrint.add(bonusExpStr);
            if(addExpStr != null)
                stringsToPrint.add(addExpStr);

        } else {
            stringsToPrint.add("Task Completed!");
        }

        printAllMessages();
    }

    /**
     * Print all messages one by one
     */
    private void printAllMessages() {
        if(stringsToPrint.size() == 0) return;
        Snackbar snackbar = getSnackbar();
        snackbar.setText(stringsToPrint.remove(0));
        snackbar.show();
    }

    /**
     * Create one snackbar if not exists
     * @return snackbar
     */
    private Snackbar getSnackbar() {
        if(snackbar == null) {
            snackbar = Snackbar.make(requireView(), "", 800);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                    if(stringsToPrint.size() > 0) {
                        transientBottomBar.getView().postDelayed(UserTaskDetailFragment.this::printAllMessages, 200);
                    }
                }
            });
        }

        return snackbar;
    }

    public FragmentTaskDetailBinding getBinding() {
        return binding;
    }

    public TaskDetailViewModel getViewModel() {
        return mViewModel;
    }
}