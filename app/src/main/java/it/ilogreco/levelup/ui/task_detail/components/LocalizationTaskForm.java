package it.ilogreco.levelup.ui.task_detail.components;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;

import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.ilogreco.levelup.adapter.AddressAdapter;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.databinding.FragmentTaskDetailBinding;
import it.ilogreco.levelup.entity.LocalizationTask;
import it.ilogreco.levelup.entity.StepCounterTask;
import it.ilogreco.levelup.entity.utils.AddressAdapterData;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.ui.task_detail.UserTaskDetailFragment;

public class LocalizationTaskForm extends BaseTaskForm<LocalizationTask> implements Observer<Integer> {

    private final Observer<List<AddressAdapterData>> defaultAddressesLoad = addressList -> {
        AddressAdapter addressAdapter = (AddressAdapter) getBinding().includeLocalizationTask.localizationTaskAddress.getAdapter();
        addressAdapter.clear();
        addressAdapter.addAll(addressList);
    };

    public LocalizationTaskForm(UserTaskDetailFragment userTaskDetailFragment) {
        super(userTaskDetailFragment);
    }

    @Override
    public void init() {
        FragmentTaskDetailBinding binding = getBinding();

        AddressAdapter addressAdapter = new AddressAdapter(getContext(), new ArrayList<>());
        binding.includeLocalizationTask.localizationTaskAddress.setAdapter(addressAdapter);
        binding.includeLocalizationTask.localizationTaskAddress.addTextChangedListener(new TextWatcher() {
            boolean isLoading = false;
            final Observer<List<AddressAdapterData>> obs = addressAdapterData -> {
                addressAdapter.clear();
                addressAdapter.addAll(addressAdapterData);

                isLoading = false;
            };

            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override public void afterTextChanged(Editable editable) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().equalsIgnoreCase("") || isLoading) return;
                isLoading = true;
                taskDetailViewModel.getAddressesByName(charSequence.toString(), obs);
            }
        });

        binding.includeLocalizationTask.localizationTaskAddress.setOnItemClickListener(
                (adapterView, view12, i, l) -> {
                    taskDetailViewModel.selectedAddress = addressAdapter.getItem(i);

                    binding.includeLocalizationTask.localizationTaskAddress.setText(taskDetailViewModel.selectedAddress.getAddressName(), false);
                });
    }

    @Override
    public void onResume() {
        taskDetailViewModel.userTaskTypeSelected.observeForever(this);
    }

    @Override
    public void onPause() {
        taskDetailViewModel.userTaskTypeSelected.removeObserver(this);
    }

    @Override
    public boolean validate() {
        resetErrors();

        FragmentTaskDetailBinding binding = getBinding();
        boolean isValid = true;

        if(taskDetailViewModel.selectedAddress == null) {
            binding.includeLocalizationTask.localizationTaskAddress.setError("Address is required!");
            isValid = false;
        }
        String radius = binding.includeLocalizationTask.localizationTaskRadius.getText().toString();
        if(radius.equalsIgnoreCase("")) {
            binding.includeLocalizationTask.localizationTaskRadius.setError("Radius is required!");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void setValue(LocalizationTask localizationTask) {
        FragmentTaskDetailBinding binding = getBinding();
        if(localizationTask == null) localizationTask = new LocalizationTask();
        if(!setVisibility(taskDetailViewModel.userTaskTypeSelected.getValue()))
            return;

        resetErrors();

        if(localizationTask.isPlaceValid()) {
            AddressAdapter addressAdapter = (AddressAdapter) binding.includeLocalizationTask.localizationTaskAddress.getAdapter();
            addressAdapter.add(taskDetailViewModel.selectedAddress);
            binding.includeLocalizationTask.localizationTaskAddress.setText(localizationTask.getAddressName(), false);
        } else {
            binding.includeLocalizationTask.localizationTaskAddress.setText("", false);
            taskDetailViewModel.getNewestAddresses(defaultAddressesLoad);
        }

        binding.includeLocalizationTask.localizationTaskRadius.setText(String.format(Locale.getDefault(), "%d", localizationTask.getCompletionRadius()));
    }

    @Override
    public LocalizationTask getValue() {
        LocalizationTask localizationTask = new LocalizationTask();

        String goalStr = getBinding().includeLocalizationTask.localizationTaskRadius.getText().toString().replace(',', '.');
        localizationTask.setCompletionRadius(Integer.parseInt(goalStr));
        if(taskDetailViewModel.selectedAddress != null) {
            localizationTask.setAddressName(taskDetailViewModel.selectedAddress.getAddressName());
            localizationTask.setLatitude(taskDetailViewModel.selectedAddress.getLatitude());
            localizationTask.setLongitude(taskDetailViewModel.selectedAddress.getLongitude());
        } else {
            localizationTask.setAddressName("");
            localizationTask.setLatitude(0);
            localizationTask.setLongitude(0);
        }
        return localizationTask;
    }

    @Override
    public void onEdit(boolean editMode) {
        FragmentTaskDetailBinding binding = getBinding();
        boolean isEditable = editMode && !taskDetailViewModel.isCompleted;

        final int textInput = isEditable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL;
        final int textNumberInput = isEditable ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_NULL;

        binding.includeLocalizationTask.localizationTaskAddress.setInputType(textInput);
        binding.includeLocalizationTask.localizationTaskRadius.setInputType(textNumberInput);
    }

    @Override
    public void onChanged(Integer type) {
        setVisibility(type);
    }

    private boolean setVisibility(Integer type) {
        boolean isLocalization = type != null && type == UserTaskType.Localization;
        FullUserTask fullUserTask = taskDetailViewModel.getCurrentTask();
        if(fullUserTask != null && isLocalization && !isLoaded) {
            isLoaded = true;
            setValue(fullUserTask.getLocalizationTask());
        }
        else {
            int vs = isLocalization ? View.VISIBLE : View.GONE;
            getBinding().localizationTaskWrapper.setVisibility(vs);
        }
        return isLocalization;
    }

    private void resetErrors() {
        getBinding().includeLocalizationTask.localizationTaskAddress.setError(null);
        getBinding().includeLocalizationTask.localizationTaskRadius.setError(null);
    }
}
