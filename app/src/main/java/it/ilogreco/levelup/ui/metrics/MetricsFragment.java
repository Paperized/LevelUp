package it.ilogreco.levelup.ui.metrics;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.data.UserMetrics;
import it.ilogreco.levelup.databinding.FragmentMetricsBinding;
import it.ilogreco.levelup.entity.TaskCategory;
import it.ilogreco.levelup.entity.utils.UserTaskType;
import it.ilogreco.levelup.utils.LevelUpUtils;

/**
 * Metrics fragment, if shows all metrics coming from the view model, it displays also a radar chart with the experience distribution
 */
public class MetricsFragment extends Fragment implements Observer<UserMetrics> {
    private UserMetrics currentMetrics;
    private MetricsViewModel mViewModel;
    private FragmentMetricsBinding binding;
    private TaskCategory[] categories;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(MetricsViewModel.class);
        binding = FragmentMetricsBinding.inflate(inflater);

        mViewModel.getMetricsOnce(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = LevelUpUtils.initializeDefaultSharedPreferences(getContext());
        String name = "User";
        if(sharedPreferences != null)
            name = sharedPreferences.getString("KEY_NAME", name);

        String welcomeStr = getString(R.string.home_welcome_user, name);
        binding.welcomeText.setText(welcomeStr);

        RadarChart mChart = binding.dataChart;
        XAxis xAxis = mChart.getXAxis();
        // set the label of each category
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if(categories == null || categories.length == 0) return "" + value;
                TaskCategory category = categories[(int) value % categories.length];
                return String.format(Locale.getDefault(), "%s (%d)", category.getName(), currentMetrics.tasksPerCategory.get(category));
            }
        });

        YAxis yAxis = mChart.getYAxis();
        yAxis.setAxisMinimum(0f);
        yAxis.setLabelCount(6, false);
        yAxis.setDrawLabels(false);

        mChart.getDescription().setEnabled(false);

        // set animation
        mChart.animateXY(
                1400, 1400,
                Easing.EaseInOutQuad,
                Easing.EaseInOutQuad);

        mChart.setContentDescription("Experience Distribution");
    }

    @Override
    public void onChanged(UserMetrics userMetrics) {
        currentMetrics = userMetrics;

        String totalStr = getString(R.string.stat_total_task, userMetrics.totalTasks);
        binding.totalText.setText(totalStr);
        String completedStr = getString(R.string.stat_tasks_completed, userMetrics.completedTasks);
        binding.completedText.setText(completedStr);
        String ongoingStr = getString(R.string.stat_tasks_ongoing, userMetrics.ongoingTasks);
        binding.ongoingText.setText(ongoingStr);
        String succeededStr = getString(R.string.stat_tasks_succeeded, userMetrics.successTasks);
        binding.succeededText.setText(succeededStr);
        String failedStr = getString(R.string.stat_tasks_failed, userMetrics.failedTasks);
        binding.failedText.setText(failedStr);

        for (Map.Entry<Integer, Integer> entry : userMetrics.tasksPerType.entrySet()) {
            if(entry.getKey() == UserTaskType.Generic) {
                String genericCountStr = getString(R.string.stat_tasks_generic_count, entry.getValue());
                binding.genericCountText.setText(genericCountStr);
            } else if(entry.getKey() == UserTaskType.StepCounter) {
                String stepCountStr = getString(R.string.stat_tasks_step_count, entry.getValue());
                binding.stepCountText.setText(stepCountStr);
            } else if(entry.getKey() == UserTaskType.Localization) {
                String stepCountStr = getString(R.string.stat_tasks_localization_count, entry.getValue());
                binding.localizationText.setText(stepCountStr);
            }
        }

        if(categories == null || categories.length != userMetrics.tasksPerCategory.size())
            categories = new TaskCategory[userMetrics.tasksPerCategory.size()];
        Random rnd = new Random();
        RadarData radarData = new RadarData();
        List<RadarEntry> entries = new ArrayList<>();
        int i = 0;
        for(Map.Entry<TaskCategory, Integer> entry : userMetrics.tasksPerCategory.entrySet()) {
            categories[i] = entry.getKey();
            RadarEntry radarEntry = new RadarEntry(entry.getKey().getTotalExperience());
            entries.add(radarEntry);
            i++;
        }

        RadarDataSet radarDataSet = new RadarDataSet(entries, "Experience Distribution");
        radarDataSet.setValueTextSize(10f);

        int rgb = Color.rgb((int)(rnd.nextFloat() * 255), (int)(rnd.nextFloat() * 255), (int)(rnd.nextFloat() * 255));
        radarDataSet.setColor(rgb);
        radarDataSet.setFillColor(rgb);
        radarDataSet.setDrawFilled(true);
        radarDataSet.setFillAlpha(180);
        radarDataSet.setLineWidth(2f);
        radarDataSet.setDrawHighlightCircleEnabled(true);
        radarDataSet.setDrawHighlightIndicators(false);
        radarData.addDataSet(radarDataSet);

        binding.dataChart.setData(radarData);
        binding.dataChart.invalidate();
    }
}