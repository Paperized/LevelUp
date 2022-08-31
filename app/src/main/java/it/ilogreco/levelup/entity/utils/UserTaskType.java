package it.ilogreco.levelup.entity.utils;

/**
 *  Utility constants for user task type (specialization)
 */
public class UserTaskType {
    public final static int Generic = 1;
    public final static int StepCounter = 2;
    public final static int Localization = 3;

    public static boolean isValid(int type) {
        return type > 0 && type < 4;
    }
}
