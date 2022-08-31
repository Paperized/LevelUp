package it.ilogreco.levelup.ui.category_detail;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.adapter.GridIconAdapter;
import it.ilogreco.levelup.adapter.RecycleItemClickListener;
import it.ilogreco.levelup.databinding.FragmentCategoryDetailBinding;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.ui.task_detail.UserTaskDetailFragment;
import it.ilogreco.levelup.utils.AutoFitGridLayoutManager;

public class CategoryDetailFragment extends Fragment implements View.OnClickListener, Observer<TaskCategory>, TextWatcher, MenuProvider {
    public static final String KEY_CATEGORY_ID = "TASK_ID";
    public static final String KEY_CATEGORY_EDIT_MODE = "TASK_EDIT";

    private CategoryDetailViewModel categoryDetailViewModel;
    private FragmentCategoryDetailBinding binding;

    private androidx.appcompat.app.AlertDialog deleteDialog;
    private final DialogInterface.OnClickListener deleteDialogListener = this::onDeleteDialogResult;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        categoryDetailViewModel =
                new ViewModelProvider(requireActivity()).get(CategoryDetailViewModel.class);

        Bundle args = getArguments();
        if(args != null) {
            long id = args.getLong(KEY_CATEGORY_ID, -1);
            boolean edit = args.getBoolean(KEY_CATEGORY_EDIT_MODE, true);

            categoryDetailViewModel.resetUIState();
            categoryDetailViewModel.editMode = edit;
            categoryDetailViewModel.loadCategoryById(id);
        }

        binding = FragmentCategoryDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().addMenuProvider(this, getViewLifecycleOwner());

        binding.categoryNameText.addTextChangedListener(this);

        binding.saveBtn.setOnClickListener(this);
        binding.cancelBtn.setOnClickListener(this);
        binding.categoryIcon.setOnClickListener(this);

        categoryDetailViewModel.categoryLiveData.observe(getViewLifecycleOwner(), this);

        onEditModeChanged(categoryDetailViewModel.editMode);

        RecyclerView layout = binding.iconGrids;
        layout.removeAllViews();

        DisplayMetrics displayMetrics = requireContext().getResources().getDisplayMetrics();
        AutoFitGridLayoutManager autoFitGridLayoutManager = new AutoFitGridLayoutManager(requireContext(), (int)(70 * displayMetrics.density));
        layout.setLayoutManager(autoFitGridLayoutManager);

        GridIconAdapter adapter = new GridIconAdapter();
        layout.setAdapter(adapter);

        List<Integer> integerList = new ArrayList<>();
        TypedArray icons = getResources().obtainTypedArray(R.array.category_icons);
        for(int i = 0; i < icons.length(); i++) {
            int rsc = icons.getResourceId(i, -1);
            if(rsc == -1) continue;

            integerList.add(rsc);
        }

        adapter.submitList(integerList);

        layout.addOnItemTouchListener(new RecycleItemClickListener(requireContext(), layout, new RecycleItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                setCategoryIcon(adapter.getItem(position));
                binding.iconGridContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLongItemClick(View view, int position) {
                setCategoryIcon(adapter.getItem(position));
                binding.iconGridContainer.setVisibility(View.INVISIBLE);
            }
        }));
    }

    @Override
    public void onClick(View view) {
        if(view == binding.saveBtn) {
            if(categoryDetailViewModel.editMode) {
                updateCurrentCategory();
                categoryDetailViewModel.saveOrUpdateCategory();
            }

            categoryDetailViewModel.editMode = !categoryDetailViewModel.editMode;
            onEditModeChanged(categoryDetailViewModel.editMode);
        } else if(view == binding.cancelBtn) {
            categoryDetailViewModel.editMode = false;
            onEditModeChanged(false);
        } else if(view == binding.categoryIcon) {
            int visibility = binding.iconGridContainer.getVisibility();
            if(visibility == View.GONE || visibility == View.INVISIBLE) {
                binding.iconGridContainer.setVisibility(View.VISIBLE);
            } else {
                binding.iconGridContainer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        categoryDetailViewModel.categoryLiveData.removeObserver(this);
        binding = null;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        updatePreview();
    }

    @Override
    public void onChanged(@Nullable TaskCategory taskCategory) {
        if(taskCategory == null) {
            categoryDetailViewModel.iconSelected = android.R.drawable.ic_menu_close_clear_cancel;
            binding.categoryIcon.setImageResource(categoryDetailViewModel.iconSelected);
            binding.categoryNameText.setText("");
            binding.experienceText.setText("0");
        } else {

            String iconUrl = taskCategory.getIcon();
            if(iconUrl != null && iconUrl.startsWith("+id/")) {
                categoryDetailViewModel.iconSelected = Integer.parseInt(iconUrl.substring(4));
                binding.categoryIcon.setImageResource(Integer.parseInt(iconUrl.substring(4)));
            } else {
                categoryDetailViewModel.iconSelected = android.R.drawable.ic_menu_close_clear_cancel;
                binding.categoryIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            }

            binding.categoryNameText.setText(taskCategory.getName());
            binding.experienceText.setText("" + taskCategory.getTotalExperience());
        }

        updatePreview();
    }

    private void updatePreview() {
        binding.previewCategoryBtn.setText(binding.categoryNameText.getText().toString());
        if(categoryDetailViewModel.iconSelected >= 0)
            binding.previewCategoryBtn.setChipIconResource(categoryDetailViewModel.iconSelected);
    }

    private void onEditModeChanged(boolean editMode) {
        if(editMode) {
            binding.saveBtn.setImageResource(android.R.drawable.ic_menu_save);
            binding.cancelBtn.show();

            binding.categoryIcon.setOnClickListener(this);
        } else {
            binding.saveBtn.setImageResource(android.R.drawable.ic_menu_edit);
            binding.cancelBtn.hide();

            binding.categoryIcon.setOnClickListener(null);
        }

        int inputTypeText = editMode ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL;
        binding.categoryNameText.setInputType(inputTypeText);
        binding.experienceText.setInputType(InputType.TYPE_NULL);
    }

    private void updateCurrentCategory() {
        TaskCategory curr = categoryDetailViewModel.getCurrentCategory();
        if(curr == null) return;

        curr.setName(binding.categoryNameText.getText().toString());
        curr.setIcon("+id/" + categoryDetailViewModel.iconSelected.toString());
    }

    private void setCategoryIcon(int resId) {
        if(resId < 0) {
            resId = android.R.drawable.ic_menu_close_clear_cancel;
        }

        categoryDetailViewModel.iconSelected = resId;
        binding.categoryIcon.setImageResource(categoryDetailViewModel.iconSelected);
        binding.previewCategoryBtn.setChipIconResource(categoryDetailViewModel.iconSelected);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(categoryDetailViewModel.doCategoryExists()
                && !categoryDetailViewModel.isCategoryNew());

        MenuProvider.super.onPrepareMenu(menu);
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.action_delete) {
            getDeleteDialog().show();
            return true;
        }

        return false;
    }

    private AlertDialog getDeleteDialog() {
        if(deleteDialog == null) {
            deleteDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_category_dialog_title)
                    .setMessage(R.string.delete_category_dialog_message)
                    .setNegativeButton(R.string.delete_category_dialog_negative, deleteDialogListener)
                    .setPositiveButton(R.string.delete_category_dialog_positive, deleteDialogListener)
                    .create();
        }

        return deleteDialog;
    }

    private void onDeleteDialogResult(DialogInterface dialogInterface, int i) {
        if(i == DialogInterface.BUTTON_POSITIVE) {
            categoryDetailViewModel.deleteCategory(integer -> {
                if(integer > 0) {
                    Snackbar.make(requireView(), "Category deleted!", Snackbar.LENGTH_SHORT).show();

                    NavController navController = Navigation.findNavController(CategoryDetailFragment.this.requireActivity(), R.id.nav_host_fragment_content_main);
                    navController.popBackStack();
                } else {
                    Snackbar.make(requireView(), "Category cannot be deleted :\\!", Snackbar.LENGTH_SHORT).show();
                }
            });
        }

        dialogInterface.dismiss();
    }
}