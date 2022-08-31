package it.ilogreco.levelup.ui.task_detail.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.adapter.GridIconAdapter;
import it.ilogreco.levelup.adapter.RecycleItemClickListener;
import it.ilogreco.levelup.databinding.FragmentTaskDetailBinding;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.UserTask;
import it.ilogreco.levelup.entity.utils.TaskDifficultyType;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.ui.task_detail.UserTaskDetailFragment;
import it.ilogreco.levelup.utils.AutoFitGridLayoutManager;
import it.ilogreco.levelup.utils.CalendarExt;
import it.ilogreco.levelup.utils.LevelUpUtils;

public class GeneralTaskForm extends BaseTaskForm<UserTask> implements LabelFormatter, CompoundButton.OnCheckedChangeListener,
        Observer<List<TaskCategory>>, View.OnClickListener, MaterialPickerOnPositiveButtonClickListener<Long> {
    private boolean isBeginDateSelecting = true;

    private MaterialTimePicker timePicker;
    private final View.OnClickListener timePickerListener = this::onTimeSelected;

    public GeneralTaskForm(UserTaskDetailFragment userTaskDetailFragment) {
        super(userTaskDetailFragment);
    }


    @Override
    public void init() {
        FragmentTaskDetailBinding binding = getBinding();
        Context context = getContext();

        String[] types = context.getResources().getStringArray(R.array.task_types);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, types);
        binding.includeGenericTask.taskType.setAdapter(typeAdapter);

        binding.includeGenericTask.taskType.setOnItemClickListener(
                (adapterView, view1, i, l) -> onTaskTypeChanged((String) adapterView.getItemAtPosition(i)));

        binding.includeGenericTask.difficultySlider.setLabelFormatter(this);

        RecyclerView layout = binding.includeGenericTask.iconGrids;
        layout.removeAllViews();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        AutoFitGridLayoutManager autoFitGridLayoutManager = new AutoFitGridLayoutManager(context, (int)(70 * displayMetrics.density));
        layout.setLayoutManager(autoFitGridLayoutManager);

        GridIconAdapter adapter = new GridIconAdapter();
        layout.setAdapter(adapter);

        List<Integer> integerList = new ArrayList<>();
        TypedArray icons = getResources().obtainTypedArray(R.array.task_icons);
        for(int i = 0; i < icons.length(); i++) {
            int rsc = icons.getResourceId(i, -1);
            if(rsc == -1) continue;

            integerList.add(rsc);
        }

        adapter.submitList(integerList);

        layout.addOnItemTouchListener(new RecycleItemClickListener(context, layout, new RecycleItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                setTaskIcon(adapter.getItem(position));
                binding.includeGenericTask.iconGridContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                setTaskIcon(adapter.getItem(position));
                binding.includeGenericTask.iconGridContainer.setVisibility(View.INVISIBLE);
            }
        }));
    }

    @Override
    public void onResume() {
        taskDetailViewModel.categoriesLiveData.observeForever(this);
    }

    @Override
    public boolean validate() {
        resetErrors();

        FragmentTaskDetailBinding binding = getBinding();

        boolean isValid = true;
        if(taskDetailViewModel.iconSelected <= 0)
            isValid = false;
        if(binding.includeGenericTask.taskTitle.getText().length() == 0) {
            binding.includeGenericTask.taskTitle.setError("Title is required!");
            isValid = false;
        }
        boolean datesSet = true;
        if(taskDetailViewModel.latestBeginDateSelected == null) {
            binding.includeGenericTask.taskBeginDate.setError("Date is required!");
            isValid = false;
            datesSet = false;
        }
        if(taskDetailViewModel.latestEndDateSelected == null) {
            binding.includeGenericTask.taskEndDate.setError("Date is required!");
            isValid = false;
            datesSet = false;
        }
        if(datesSet && taskDetailViewModel.latestBeginDateSelected.after(taskDetailViewModel.latestEndDateSelected)) {
            binding.includeGenericTask.taskBeginDate.setError("Start date must be before End date!");
            isValid = false;
        }
        if(!UserTaskType.isValid(taskDetailViewModel.userTaskTypeSelected.getValue())) {
            binding.includeGenericTask.taskType.setError("Type is required!");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void setValue(UserTask task) {
        FragmentTaskDetailBinding binding = getBinding();
        Context context = getContext();

        resetErrors();

        if(task.getIcon() == null || !task.getIcon().startsWith("+id/")) {
            setTaskIcon(-1);
        } else {
            setTaskIcon(Integer.parseInt(task.getIcon().substring(4)));
        }

        binding.includeGenericTask.taskTitle.setText(task.getTitle());
        binding.includeGenericTask.taskDescription.setText(task.getDescription());
        binding.includeGenericTask.taskAdditionalRw.setText(String.format(Locale.getDefault(), "%d", task.getPointsPrize()));
        binding.includeGenericTask.taskType.setText(LevelUpUtils.taskTypeToString(task.getType(), context), false);
        taskDetailViewModel.userTaskTypeSelected.setValue(task.getType());

        binding.includeGenericTask.difficultySlider.setValue(LevelUpUtils.sliderValueFromTaskDifficulty(task.getDifficultyType()));
        ColorStateList csl = ColorStateList.valueOf(LevelUpUtils.taskDifficultyToColor(task.getDifficultyType()));
        binding.includeGenericTask.difficultySlider.setTrackActiveTintList(csl);

        String beginDateString = CalendarExt.getFormattedDateTime(taskDetailViewModel.latestBeginDateSelected, context);
        String endDateString = CalendarExt.getFormattedDateTime(taskDetailViewModel.latestEndDateSelected, context);

        binding.includeGenericTask.taskBeginDate.setText(beginDateString);
        binding.includeGenericTask.taskEndDate.setText(endDateString);

        binding.includeGenericTask.iconGridContainer.setVisibility(task.getId() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    public void setCategoriesValue(List<TaskCategory> categories) {
        FragmentTaskDetailBinding binding = getBinding();
        for(int i = 0; i < binding.includeGenericTask.chipGroup.getChildCount(); i++) {
            Chip currChip = (Chip) binding.includeGenericTask.chipGroup.getChildAt(i);

            boolean selected = false;
            for(TaskCategory category : categories) {
                if(category.getId() == currChip.getId()) {
                    selected = true;
                    break;
                }
            }

            currChip.setChecked(selected);
        }
    }

    @Override
    public UserTask getValue() {
        FragmentTaskDetailBinding binding = getBinding();
        UserTask userTask = new UserTask();

        userTask.setTitle(binding.includeGenericTask.taskTitle.getText().toString());
        userTask.setDescription(binding.includeGenericTask.taskDescription.getText().toString());
        userTask.setPointsPrize(Integer.parseInt(binding.includeGenericTask.taskAdditionalRw.getText().toString()));

        userTask.setType(taskDetailViewModel.userTaskTypeSelected.getValue());

        userTask.setBeginDate(taskDetailViewModel.latestBeginDateSelected);
        userTask.setEndDate(taskDetailViewModel.latestEndDateSelected);

        userTask.setIcon("+id/" + taskDetailViewModel.iconSelected);

        userTask.setDifficultyType(LevelUpUtils.taskDifficultyFromSliderValue(binding.includeGenericTask.difficultySlider.getValue()));
        userTask.setCompleted(taskDetailViewModel.isCompleted);
        return userTask;
    }

    public void getCategories(List<TaskCategory> categories) {
        categories.clear();
        for(Integer id : getBinding().includeGenericTask.chipGroup.getCheckedChipIds()) {
            TaskCategory category = new TaskCategory();
            category.setId(id);
            categories.add(category);
        }
    }

    @Override
    public void onChanged(List<TaskCategory> categories) {
        onCategoriesChanged(categories);
    }

    private void onCategoriesChanged(List<TaskCategory> categories) {
        FragmentTaskDetailBinding binding = getBinding();
        binding.includeGenericTask.chipGroup.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for(TaskCategory category : categories) {
            Chip mChip = (Chip) inflater.inflate(R.layout.chip_entry_item, null, false);
            mChip.setId((int)category.getId());

            if(category.getIcon() != null && category.getIcon().startsWith("+id/")) {
                mChip.setChipIconResource(Integer.parseInt(category.getIcon().substring(4)));
            } else {
                mChip.setChipIconResource(android.R.drawable.star_on);
            }

            mChip.setText(category.getName());
            binding.includeGenericTask.chipGroup.addView(mChip);

            if(taskDetailViewModel.doTaskExists()) {
                for(TaskCategory userCategory : taskDetailViewModel.getCurrentTask().getTaskCategory()) {
                    if(userCategory.getId() == category.getId()) {
                        mChip.setChecked(true);
                        break;
                    }
                }
            }

            mChip.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onEdit(boolean editMode) {
        FragmentTaskDetailBinding binding = getBinding();
        boolean isEditable = editMode && !taskDetailViewModel.isCompleted;

        final int textInput = isEditable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL;
        final int textNumberInput = isEditable ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_NULL;

        binding.includeGenericTask.taskTitle.setInputType(textInput);
        binding.includeGenericTask.taskDescription.setInputType(textInput);
        binding.includeGenericTask.taskAdditionalRw.setInputType(textNumberInput);

        binding.includeGenericTask.taskBeginDate.setInputType(InputType.TYPE_NULL);
        binding.includeGenericTask.taskEndDate.setInputType(InputType.TYPE_NULL);
        binding.includeGenericTask.taskType.setEnabled(isEditable);
        binding.includeGenericTask.difficultySlider.setEnabled(isEditable);

        if(isEditable) {
            binding.includeGenericTask.taskBeginDate.setOnClickListener(this);
            binding.includeGenericTask.taskEndDate.setOnClickListener(this);
        } else {
            binding.includeGenericTask.taskBeginDate.setOnClickListener(null);
            binding.includeGenericTask.taskEndDate.setOnClickListener(null);
        }

        if(editMode) {
            binding.includeGenericTask.taskIcon.setOnClickListener(this);
        } else {
            binding.includeGenericTask.taskIcon.setOnClickListener(null);
        }
    }

    @Override
    public void onClick(View view) {
        FragmentTaskDetailBinding binding = getBinding();
        if (view == binding.includeGenericTask.taskBeginDate) {
            // open dialog
            if(!taskDetailViewModel.doTaskExists()) return;
            isBeginDateSelecting = true;
            long currentSelection = taskDetailViewModel.latestBeginDateSelected != null ?
                    taskDetailViewModel.latestBeginDateSelected.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker().setSelection(currentSelection);

            if (taskDetailViewModel.latestEndDateSelected != null) {
                builder.setCalendarConstraints((new CalendarConstraints.Builder())
                        .setValidator(DateValidatorPointBackward.before(taskDetailViewModel.latestEndDateSelected.getTimeInMillis())).build());
            }

            MaterialDatePicker<Long> dataPicker = builder.setTitleText("Begin Date").build();
            dataPicker.addOnPositiveButtonClickListener(this);
            dataPicker.show(getChildFragmentManager(), "Begin Date");
        } else if (view == binding.includeGenericTask.taskEndDate) {
            // open dialog
            if(!taskDetailViewModel.doTaskExists()) return;
            isBeginDateSelecting = false;
            long currentSelection = taskDetailViewModel.latestEndDateSelected != null ? taskDetailViewModel.latestEndDateSelected.getTimeInMillis() : -1;
            if (currentSelection == -1) {
                currentSelection = taskDetailViewModel.latestBeginDateSelected != null ?
                        taskDetailViewModel.latestBeginDateSelected.getTimeInMillis() : MaterialDatePicker.todayInUtcMilliseconds();
            }

            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker().setSelection(currentSelection);

            if (taskDetailViewModel.latestBeginDateSelected != null) {
                builder.setCalendarConstraints((new CalendarConstraints.Builder())
                        .setValidator(DateValidatorPointForward.from(CalendarExt.getDayMillis(taskDetailViewModel.latestBeginDateSelected))).build());
            }

            MaterialDatePicker<Long> dataPicker = builder.setTitleText("End Date").build();
            dataPicker.addOnPositiveButtonClickListener(this);
            dataPicker.show(getChildFragmentManager(), "End Date");
        } else if(view == binding.includeGenericTask.taskIcon) {
            int visibility = binding.includeGenericTask.iconGridContainer.getVisibility();
            if(visibility == View.GONE || visibility == View.INVISIBLE) {
                binding.includeGenericTask.iconGridContainer.setVisibility(View.VISIBLE);
            } else {
                binding.includeGenericTask.iconGridContainer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(!taskDetailViewModel.editMode || taskDetailViewModel.isCompleted)
            compoundButton.setChecked(!b);
    }

    @NonNull
    @Override
    public String getFormattedValue(float value) {
        TaskDifficultyType newDiff = LevelUpUtils.taskDifficultyFromSliderValue(value);
        ColorStateList csl = ColorStateList.valueOf(LevelUpUtils.taskDifficultyToColor(newDiff));
        getBinding().includeGenericTask.difficultySlider.setTrackActiveTintList(csl);
        return LevelUpUtils.taskDifficultyToString(newDiff);
    }

    private void onTimeSelected(View view) {
        int hours = timePicker.getHour();
        int minutes = timePicker.getMinute();

        Calendar calendar;
        if(isBeginDateSelecting) {
            calendar = taskDetailViewModel.latestBeginDateSelected;
            if(CalendarExt.areCalendarsDateSame(taskDetailViewModel.latestBeginDateSelected, taskDetailViewModel.latestEndDateSelected)) {
                int endH = taskDetailViewModel.latestEndDateSelected.get(Calendar.HOUR_OF_DAY);
                int endM = taskDetailViewModel.latestEndDateSelected.get(Calendar.MINUTE);
                boolean validation = hours < endH || hours == endH && minutes <= endM;
                if (!validation) {
                    hours = endH;
                    minutes = endM;
                }
            }
        } else {
            calendar = taskDetailViewModel.latestEndDateSelected;
            if(CalendarExt.areCalendarsDateSame(taskDetailViewModel.latestBeginDateSelected, taskDetailViewModel.latestEndDateSelected)) {
                int startH = taskDetailViewModel.latestBeginDateSelected.get(Calendar.HOUR_OF_DAY);
                int startM = taskDetailViewModel.latestBeginDateSelected.get(Calendar.MINUTE);
                boolean validation = hours > startH || hours == startH && minutes >= startM;
                if (!validation) {
                    hours = startH;
                    minutes = startM;
                }
            }
        }


        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);

        if(isBeginDateSelecting) {
            String beginDateString = CalendarExt.getFormattedDateTime(taskDetailViewModel.latestBeginDateSelected, getContext());
            getBinding().includeGenericTask.taskBeginDate.setText(beginDateString);
        } else {
            String endDateString = CalendarExt.getFormattedDateTime(taskDetailViewModel.latestEndDateSelected, getContext());
            getBinding().includeGenericTask.taskEndDate.setText(endDateString);
        }
    }

    @Override
    public void onPositiveButtonClick(Long selection) {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(selection);

        if(isBeginDateSelecting) {
            if(taskDetailViewModel.latestBeginDateSelected != null) {
                hours = taskDetailViewModel.latestBeginDateSelected.get(Calendar.HOUR_OF_DAY);
                minutes = taskDetailViewModel.latestBeginDateSelected.get(Calendar.MINUTE);
            }

            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);
            taskDetailViewModel.latestBeginDateSelected = calendar;
        } else {
            if(taskDetailViewModel.latestEndDateSelected != null) {
                hours = taskDetailViewModel.latestEndDateSelected.get(Calendar.HOUR_OF_DAY);
                minutes = taskDetailViewModel.latestEndDateSelected.get(Calendar.MINUTE);
            }

            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);
            taskDetailViewModel.latestEndDateSelected = calendar;
        }

        MaterialTimePicker picker = getTimePicker(hours, minutes);
        picker.show(getChildFragmentManager(), "Time Picker");
    }

    private MaterialTimePicker getTimePicker(int h, int m) {
        if(timePicker != null) {
            timePicker.removeOnPositiveButtonClickListener(timePickerListener);
        }

        int format = DateFormat.is24HourFormat(getContext()) ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H;
        timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(format)
                .setTitleText("Select Time")
                .setHour(h)
                .setMinute(m)
                .build();

        timePicker.addOnPositiveButtonClickListener(timePickerListener);
        return timePicker;
    }

    private void onTaskTypeChanged(String type) {
        Integer taskType = LevelUpUtils.stringToTaskType(type, getContext());
        if(taskType == null) {
            taskDetailViewModel.userTaskTypeSelected.setValue(UserTaskType.Generic);
            return;
        }

        taskDetailViewModel.userTaskTypeSelected.setValue(taskType);
    }

    private void resetErrors() {
        FragmentTaskDetailBinding binding = getBinding();
        binding.includeGenericTask.taskTitle.setError(null);
        binding.includeGenericTask.taskBeginDate.setError(null);
        binding.includeGenericTask.taskEndDate.setError(null);
        binding.includeGenericTask.taskBeginDate.setError(null);
        binding.includeGenericTask.taskType.setError(null);
    }

    private void setTaskIcon(int resId) {
        if(resId < 0) {
            resId = android.R.drawable.ic_menu_close_clear_cancel;
        }

        taskDetailViewModel.iconSelected = resId;
        getBinding().includeGenericTask.taskIcon.setImageResource(taskDetailViewModel.iconSelected);
    }

    @Override
    public void onPause() {
        taskDetailViewModel.categoriesLiveData.removeObserver(this);
    }
}
