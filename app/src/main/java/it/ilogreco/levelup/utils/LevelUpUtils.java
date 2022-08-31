package it.ilogreco.levelup.utils;

import static java.lang.Integer.parseInt;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;

import androidx.core.content.res.ResourcesCompat;

import java.util.Random;

import it.ilogreco.levelup.R;
import it.ilogreco.levelup.data.FullUserTask;
import it.ilogreco.levelup.data.GainedExperienceResult;
import it.ilogreco.levelup.entity.utils.TaskDifficultyType;
import it.ilogreco.levelup.entity.utils.UserTaskType;

/**
 * Application utils methods, mainly drawable icon loading, entity enums methods and experience generation
 */
public class LevelUpUtils {
    public static int taskDifficultyToColor(TaskDifficultyType difficultyType) {
        switch (difficultyType) {
            case F: return Color.argb(255, 53, 255, 27);
            case E: return Color.argb(255, 120, 255, 103);
            case D: return Color.argb(255, 207, 255, 103);
            case C: return Color.argb(255, 255, 255, 103);
            case B: return Color.argb(255, 255, 204, 0);
            case A: return Color.argb(255, 255, 96, 0);
            case S: return Color.argb(255, 255, 0, 0);
            default: throw new IllegalArgumentException("Not a valid difficulty type");
        }
    }

    public static String taskDifficultyToString(TaskDifficultyType difficultyType) {
        if (difficultyType == null) throw new IllegalArgumentException("Not a valid difficulty type");
        switch (difficultyType) {
            case A: return "A";
            case B: return "B";
            case C: return "C";
            case D: return "D";
            case E: return "E";
            case F: return "F";
            case S: return "S";
            default: throw new IllegalArgumentException("Not a valid difficulty type");
        }
    }

    public static TaskDifficultyType taskDifficultyFromSliderValue(float sliderValue) {
        TaskDifficultyType[] diffs = TaskDifficultyType.values();
        float valueStep = 100f / (diffs.length - 1);
        if(sliderValue < 0 || sliderValue > 100) throw new IllegalArgumentException("Value need to be between 0 and 100");
        int i = 0;
        final float offset = 0.05f;
        while(Math.abs(sliderValue - valueStep * i) > offset) {
            i++;
        }

        return diffs[i];
    }

    public static float sliderValueFromTaskDifficulty(TaskDifficultyType difficultyType) {
        if(difficultyType == null) throw new IllegalArgumentException("Type cannot be null");

        TaskDifficultyType[] diffs = TaskDifficultyType.values();
        float valueStep = 100f / (diffs.length - 1);
        for (int i = 0; i < diffs.length; i++) {
            if(diffs[i] == difficultyType) {
                return i * valueStep;
            }
        }

        throw new IllegalArgumentException("Invalid type");
    }

    public static String taskTypeToString(int userTaskType, Context context) {
        String[] types = context.getResources().getStringArray(R.array.task_types);
        switch (userTaskType) {
            case UserTaskType.Generic: return types[0];
            case UserTaskType.StepCounter: return types[1];
            case UserTaskType.Localization: return types[2];
            default: throw new IllegalArgumentException("Not a valid user type");
        }
    }

    public static Integer stringToTaskType(String type, Context context) {
        if (type == null) throw new IllegalArgumentException("Not a valid task type");
        String[] types = context.getResources().getStringArray(R.array.task_types);
        if(type.equalsIgnoreCase(types[0])) {
            return UserTaskType.Generic;
        } else if(type.equalsIgnoreCase(types[1])) {
            return UserTaskType.StepCounter;
        } else if(type.equalsIgnoreCase(types[2])) {
            return UserTaskType.Localization;
        }

        return null;
    }

    public static GainedExperienceResult calculateExperience(FullUserTask fullUserTask) {
        GainedExperienceResult gainedExperienceResult = new GainedExperienceResult();
        if(fullUserTask == null || fullUserTask.getUserTask() == null) return gainedExperienceResult;

        gainedExperienceResult.additionalExperience = fullUserTask.getUserTask().getPointsPrize();
        gainedExperienceResult.experienceGained = getExperienceFromDifficulty(fullUserTask.getUserTask().getDifficultyType());
        gainedExperienceResult.bonusPercentage = tryExperiencePercentageBonus(fullUserTask.getUserTask().getDifficultyType());

        return gainedExperienceResult;
    }

    private static int getExperienceFromDifficulty(TaskDifficultyType difficultyType) {
        if(difficultyType == null) return 0;
        int exp = 0;

        Random random = new Random();

        switch (difficultyType) {
            case F: return random.nextInt(((20 - 15) + 1) + 15);
            case E: return random.nextInt(((28 - 19) + 1) + 19);
            case D: return random.nextInt(((39 - 25) + 1) + 25);
            case C: return random.nextInt(((50 - 33) + 1) + 33);
            case B: return random.nextInt(((95 - 45) + 1) + 45);
            case A: return random.nextInt(((140 - 60) + 1) + 60);
            case S: return random.nextInt(((350 - 200) + 1) + 200);
        }

        return exp;
    }

    private static float tryExperiencePercentageBonus(TaskDifficultyType difficultyType) {
        if(difficultyType == null) return 0;
        Random random = new Random();
        // 15% to find a bonus exp
        if(random.nextFloat() > 0.15f) {
            return 0f;
        }

        switch (difficultyType) {
            case F: return random.nextInt(5) / 100f;
            case E: return random.nextInt(7) / 100f;
            case D: return random.nextInt(12) / 100f;
            case C: return random.nextInt(16) / 100f;
            case B: return random.nextInt(20) / 100f;
            case A: return random.nextInt(24) / 100f;
            case S: return random.nextInt(30) / 100f;
        }

        return 0f;
    }

    public static String abbreviateString(String str, int maxLength) {
        if(str == null) return "";
        if(str.length() > maxLength) {
            return str.substring(0, maxLength).concat("...");
        }

        return str;
    }

    public static SharedPreferences initializeDefaultSharedPreferences(Context appContext) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        if(sharedPreferences == null) return null;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(!sharedPreferences.contains("KEY_STEP_LENGTH")) {
            editor.putFloat("KEY_STEP_LENGTH", 0.7f);
        }
        if(!sharedPreferences.contains("KEY_NAME")) {
            editor.putString("KEY_NAME", "Bob");
        }
        editor.apply();

        return sharedPreferences;
    }

    public static Drawable getIconByString(String iconId, Context context) {
        if(iconId == null) return getDefaultIcon(context);
        Drawable icon = getInternalIcon(iconId, context);
        if(icon != null) return icon;
        return getDefaultIcon(context);
    }

    public static Drawable getInternalIcon(String resStr, Context context) {
        if(resStr.startsWith("+id/")) {
            try {
                int resId = Integer.parseInt(resStr.substring(4));
                return ResourcesCompat.getDrawable(context.getResources(), resId, null);
            } catch (NumberFormatException ignored) { }
        }

        return null;
    }

    public static Drawable getDefaultIcon(Context context) {
        return ResourcesCompat.getDrawable(context.getResources(), android.R.drawable.ic_menu_close_clear_cancel, null);
    }
}
