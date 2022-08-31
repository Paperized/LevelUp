package it.ilogreco.levelup.ui.home;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.adapter.RecycleItemClickListener;
import it.ilogreco.levelup.adapter.TaskAdapter;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.databinding.FragmentHomeBinding;
import it.ilogreco.levelup.ui.task_detail.UserTaskDetailFragment;

/**
 * Fragment that hold a list of full user tasks, it can also filter by date
 */
public class HomeFragment extends Fragment implements View.OnClickListener, RecycleItemClickListener.OnItemClickListener,
        MenuProvider, DatePickerDialog.OnDateSetListener {

    private FragmentHomeBinding binding;
    private TaskAdapter taskAdapter;
    private HomeViewModel homeViewModel;
    private RecycleItemClickListener rvClickListener;
    private DatePickerDialog datePickerDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = binding.rvTasks;
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        rv.setLayoutManager(llm);

        taskAdapter = new TaskAdapter();
        rv.setAdapter(taskAdapter);

        rvClickListener = new RecycleItemClickListener(requireContext(), binding.rvTasks, this);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner());
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.action_calendar) {
            getDatePickerDialog().show();
        }

        return false;
    }

    @Override
    public void onPrepareMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_calendar).setVisible(true);
        MenuProvider.super.onPrepareMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        binding.createBtn.setOnClickListener(this);
        binding.rvTasks.addOnItemTouchListener(rvClickListener);
        homeViewModel.tasks.observe(this, taskAdapter::submitList);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
        homeViewModel.tasksDay.set(y, m, d);
    }

    @Override
    public void onClick(View view) {
        createNewTask();
    }

    @Override
    public void onPause() {
        super.onPause();

        binding.rvTasks.removeOnItemTouchListener(rvClickListener);
        homeViewModel.tasks.removeObservers(this);
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

    private void createNewTask() {
        gotoDetail(-1);
    }

    private void showDetail(int index) {
        TaskAdapter taskAdapter = (TaskAdapter) binding.rvTasks.getAdapter();
        FullUserTask task = taskAdapter.getItem(index);

        gotoDetail(task.getUserTask().getId());
    }

    private void gotoDetail(long taskId) {
        NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
        Bundle b = new Bundle();
        b.putLong(UserTaskDetailFragment.KEY_ID, taskId);
        nav.navigate(R.id.action_nav_home_to_nav_edit_tasks, b);
    }

    private DatePickerDialog getDatePickerDialog() {
        int y = homeViewModel.tasksDay.get(Calendar.YEAR);
        int m = homeViewModel.tasksDay.get(Calendar.MONTH);
        int d = homeViewModel.tasksDay.get(Calendar.DAY_OF_MONTH);

        if(datePickerDialog == null) {
            datePickerDialog = new DatePickerDialog(requireContext(), this, y, m, d);
            datePickerDialog.setOnDismissListener(x -> homeViewModel.updateTasks());
        } else
            datePickerDialog.updateDate(y, m, d);

        return datePickerDialog;
    }
}