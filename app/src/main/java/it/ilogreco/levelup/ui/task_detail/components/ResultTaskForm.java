package it.ilogreco.levelup.ui.task_detail.components;

import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.List;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.adapter.SliderImageAdapter;
import it.ilogreco.levelup.databinding.FragmentTaskDetailBinding;
import it.ilogreco.levelup.entity.TaskCompleted;
import it.ilogreco.levelup.ui.task_detail.UserTaskDetailFragment;
import it.ilogreco.levelup.utils.CalendarExt;

public class ResultTaskForm extends BaseTaskForm<TaskCompleted> implements ActivityResultCallback<List<Uri>>, View.OnClickListener {

    private SliderImageAdapter sliderImageAdapter;
    private ActivityResultLauncher<String[]> registeredIntent;

    public ResultTaskForm(UserTaskDetailFragment userTaskDetailFragment) {
        super(userTaskDetailFragment);
    }

    @Override
    public void init() {
        FragmentTaskDetailBinding binding = getBinding();

        SliderView sliderView = binding.includeResultTask.imageSlider;
        sliderImageAdapter = new SliderImageAdapter();
        sliderView.setIndicatorVisibility(true);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.startAutoCycle();

        sliderView.setSliderAdapter(sliderImageAdapter, true);

        UserTaskDetailFragment fragment = getUserFragment();
        if(fragment != null)
            registeredIntent = fragment.registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), this);
    }

    @Override
    public void onClick(View view) {
        FragmentTaskDetailBinding binding = getBinding();
        if(view == binding.includeResultTask.pickImagesButton) {
            pickImages();
        } else if(view == binding.includeResultTask.removeImageButton) {
            removeCurrentImage();
        }
    }

    @Override
    public void onEdit(boolean editMode) {
        FragmentTaskDetailBinding binding = getBinding();
        boolean isCompletedEditable = editMode && taskDetailViewModel.isCompleted;

        binding.includeResultTask.taskResultNote.setInputType(isCompletedEditable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        binding.includeResultTask.pickImagesButton.setClickable(isCompletedEditable);
        binding.includeResultTask.removeImageButton.setClickable(isCompletedEditable);
    }

    @Override
    public void setValue(TaskCompleted taskCompleted) {
        FragmentTaskDetailBinding binding = getBinding();
        if(taskCompleted == null) {
            binding.resultTaskWrapper.setVisibility(View.GONE);
            return;
        }

        binding.resultTaskWrapper.setVisibility(View.VISIBLE);
        binding.includeResultTask.taskResultNote.setText(taskCompleted.getDescription());
        binding.includeResultTask.taskCompletionDate.setText(getResources()
                .getString(R.string.task_result_date, CalendarExt.getFormattedDateTime(taskCompleted.getCompletionDate(), getContext())));
        binding.includeResultTask.taskResultExperience.setText(taskCompleted.getTotalExperience() + "");
        binding.includeResultTask.taskResultBonusExperience.setText(taskCompleted.getBonusPercentage() + "%");
        binding.includeResultTask.taskResultExperienceEach.setText(taskCompleted.getExperienceEachCategory() + "");

        sliderImageAdapter.setItems(taskCompleted.getPhotos());
        binding.includeResultTask.imageSlider.setSliderAdapter(sliderImageAdapter, true);

        refreshImageContainer();
    }

    @Override
    public TaskCompleted getValue() {
        TaskCompleted taskCompleted = new TaskCompleted();
        taskCompleted.setDescription(getBinding().includeResultTask.taskResultNote.getText().toString());
        taskCompleted.setPhotos(sliderImageAdapter.getItems());
        return taskCompleted;
    }

    @Override
    public void onPause() {
        if(registeredIntent != null)
            registeredIntent.unregister();
    }

    @Override
    public void onActivityResult(List<Uri> result) {
        if(result != null && result.size() > 0) {
            for (Uri uri : result) {
                requireActivity().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            sliderImageAdapter.addItems(result);
            getBinding().includeResultTask.imageSlider.setSliderAdapter(sliderImageAdapter, true);
            refreshImageContainer();
        }
    }

    private void refreshImageContainer() {
        boolean expanded = sliderImageAdapter.getItems() != null && sliderImageAdapter.getItems().size() > 0;

        if(expanded) {
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            getBinding().includeResultTask.imageContainer.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, displayMetrics);
        } else {
            getBinding().includeResultTask.imageContainer.getLayoutParams().height = 0;
        }
    }

    public void pickImages() {
        registeredIntent.launch(new String[] { "image/*" });
    }

    public void removeCurrentImage() {
        FragmentTaskDetailBinding binding = getBinding();

        sliderImageAdapter.deleteItem(binding.includeResultTask.imageSlider.getCurrentPagePosition());
        binding.includeResultTask.imageSlider.setSliderAdapter(sliderImageAdapter, true);
    }
}
