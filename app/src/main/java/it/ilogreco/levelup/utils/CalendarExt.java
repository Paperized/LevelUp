package it.ilogreco.levelup.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Calendar Utils static methods to work with Calendar
 */
public class CalendarExt {

    public static Calendar getCurrentWithoutTime() {
        Calendar c = Calendar.getInstance();
        int year  = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date  = c.get(Calendar.DATE);
        c.clear();
        c.set(year, month, date);
        return c;
    }

    public static long getDateTimeWithoutSeconds(Calendar calendar) {
        if(calendar == null) return 0L;

        int s = calendar.get(Calendar.SECOND);
        int _ms = calendar.get(Calendar.MILLISECOND);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long res = calendar.getTimeInMillis();
        calendar.set(Calendar.SECOND, s);
        calendar.set(Calendar.MILLISECOND, _ms);

        return calendar.getTimeInMillis();
    }

    public static long getDayMillis(Calendar calendar) {
        if(calendar == null) return 0L;

        long ms = calendar.getTimeInMillis();

        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day  = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.clear();
        calendar.set(year, month, day);
        long dayMs = calendar.getTimeInMillis();
        calendar.setTimeInMillis(ms);
        return dayMs;
    }

    public static long getNextDayMillis(Calendar calendar) {
        if(calendar == null) return 0L;

        long ms = calendar.getTimeInMillis();

        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day  = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.clear();
        calendar.set(year, month, day + 1);
        long nextDayMs = calendar.getTimeInMillis();
        calendar.setTimeInMillis(ms);
        return nextDayMs;
    }

    public static boolean areCalendarsDateSame(Calendar c1, Calendar c2) {
        if(c1 == null || c2 == null) return false;
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isBetweenDates(Calendar check, Calendar min, Calendar max) {
        if(check == null || min == null || max == null) return false;
        return check.after(min) && check.before(max);
    }

    public static String getFormattedTime(Calendar calendar, Context context) {
        if(calendar == null) return "";
        return DateFormat.getTimeFormat(context).format(calendar.getTime());
    }

    public static String getFormattedDate(Calendar calendar, Context context) {
        if(calendar == null) return "";
        return DateFormat.getDateFormat(context).format(calendar.getTime());
    }

    public static String getFormattedDateTime(Calendar calendar, Context context) {
        if(calendar == null) return "";
        return String.format(Locale.getDefault(), "%s %s",
                getFormattedDate(calendar, context), getFormattedTime(calendar, context));
    }
}
