package it.ilogreco.levelup.converters;

import androidx.room.TypeConverter;

import java.util.Calendar;

/**
 *  Utility class, converts a Calendar to it's timestamp and viceversa
 */
public class CalendarConverter {
    @TypeConverter
    public static Calendar fromTimestamp(Long value) {
        if(value == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(value);
        return c;
    }

    @TypeConverter
    public static Long dateToTimestamp(Calendar date) {
        if(date == null) return 0L;
        return date.getTimeInMillis();
    }

}
