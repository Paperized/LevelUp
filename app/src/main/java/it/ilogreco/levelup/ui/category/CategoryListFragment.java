package it.ilogreco.levelup.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.adapter.RecycleItemClickListener;
import it.ilogreco.levelup.adapter.TaskCategoryAdapter;
import it.ilogreco.levelup.databinding.FragmentCategoryListBinding;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.ui.category_detail.CategoryDetailFragment;

/**
 * Fragment holding a list of category tasks
 */
public class CategoryListFragment extends Fragment implements View.OnClickListener, RecycleItemClickListener.OnItemClickListener {
    private FragmentCategoryListBinding binding;
    private TaskCategoryAdapter taskCategoryAdapter;
    private CategoryListViewModel categoryListViewModel;
    private RecycleItemClickListener rvClickListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryListViewModel =
                new ViewModelProvider(requireActivity()).get(CategoryListViewModel.class);

        binding = FragmentCategoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = binding.rvEditCategories;
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        rv.setLayoutManager(llm);

        taskCategoryAdapter = new TaskCategoryAdapter();
        rv.setAdapter(taskCategoryAdapter);

        rvClickListener = new RecycleItemClickListener(requireContext(), binding.rvEditCategories, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        binding.createBtn.setOnClickListener(this);

        binding.rvEditCategories.addOnItemTouchListener(rvClickListener);
        categoryListViewModel.categories.observe(this, taskCategoryAdapter::submitList);
    }

    @Override
    public void onClick(View view) {
        createNewCategory();
    }

    @Override
    public void onPause() {
        super.onPause();

        binding.rvEditCategories.removeOnItemTouchListener(rvClickListener);
        categoryListViewModel.categories.removeObservers(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }

    @Override
    public void onItemClick(View view, int position) {
        showDetail(position);
    }

    @Override
    public void onLongItemClick(View view, int position) {
        showDetail(position);
    }

    private void createNewCategory() {
        gotoDetail(-1, true);
    }

    private void showDetail(int index) {
        TaskCategory category = taskCategoryAdapter.getItem(index);

        gotoDetail(category.getId(), false);
    }

    private void gotoDetail(long categoryId, boolean editMode) {
        NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        Bundle b = new Bundle();
        b.putLong(CategoryDetailFragment.KEY_CATEGORY_ID, categoryId);
        b.putBoolean(CategoryDetailFragment.KEY_CATEGORY_EDIT_MODE, editMode);
        nav.navigate(R.id.action_nav_edit_category_list_to_nav_task_category_detail, b);
    }
}