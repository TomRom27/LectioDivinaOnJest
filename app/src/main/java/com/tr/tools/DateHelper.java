package com.tr.tools;

import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by bpl2111 on 2014-05-29.
 */
public class DateHelper {
    public static String DATE_INTERNAL_FORMAT = "";

    public static String toInternalString(Date date) {
        return new SimpleDateFormat(DATE_INTERNAL_FORMAT).format(date).toString();
    }

    public static Date fromInternalString(String dateString) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_INTERNAL_FORMAT);

        Date date;
        try {
            date = dateFormat.parse(dateString);
            return date;
        } catch (Exception ex) {

            return getToday();
        }
    }

    public static String toString(String formatString, Date date) {
        return new SimpleDateFormat(formatString).format(date);
    }

    public static int compareDates(Date d1, Date d2) {
        Calendar c1 = new GregorianCalendar();
        c1.setTime(d1);
        Calendar c2 = new GregorianCalendar();
        c2.setTime(d2);

        if (c1.before(c2))
            return -1;
        else if (c1.after(c2))
            return 1;
        else
            return 0;
    }

    public static boolean isInRange(Date d, Date d1, Date d2) {
        if (compareDates(d1, d) <= 0 && compareDates(d, d2) <= 0)
            return true;
        else
            return false;
    }

    public static int days(Date d1 , Date d2, boolean inclusive) {
        int days = (int)((d1.getTime() - d2.getTime() ) / (1000 * 60 * 60 * 24));
        if (inclusive)
            days = days + 1;
        return days;
    }

    public static String periodToShortestString(Date firstDate, Date lastDate) {
        String fullDateFormat = "d.MM.yyyy";
        String dayMonthFormat = "d.MM";
        String dayOnlyFormt = "d";

        SimpleDateFormat dateFirstFormat;
        SimpleDateFormat dateLastFormat;

        Calendar dateFirstCalendar = Calendar.getInstance();
        dateFirstCalendar.setTime(firstDate);

        Calendar dateLastCalendar = Calendar.getInstance();
        dateLastCalendar.setTime(lastDate);

        dateLastFormat = new SimpleDateFormat(fullDateFormat);
        if (dateFirstCalendar.get(Calendar.YEAR) != dateLastCalendar.get(Calendar.YEAR)) {
            // different years - we show full date for each
            dateFirstFormat = new SimpleDateFormat(fullDateFormat);
        } else {
            if (dateFirstCalendar.get(Calendar.MONTH) != dateLastCalendar.get(Calendar.MONTH)) {
                // year the same, but different months
                dateFirstFormat = new SimpleDateFormat(dayMonthFormat);
            } else
                // year, month the same
                dateFirstFormat = new SimpleDateFormat(dayOnlyFormt);
        }

        return dateFirstFormat.format(firstDate) + " - " + dateLastFormat.format(lastDate);
    }

    /**
     * @return date with hour:min set to 00:00
     */
    public static Date getToday() {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        // calendar's get uses 0-based month value !!!
        return makeDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.MONTH) + 1;// calendar's set uses 0-based month value !!!
    }

    /**
     * @param year
     * @param realMonth 1-12 month value
     * @param day
     * @return date with hour:min set to 00:00
     */
    public static Date makeDate(int year, int realMonth, int day) {
        Calendar calendar = Calendar.getInstance();
        int month = realMonth - 1; // calendar's set uses 0-based month value !!!
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date getDateOnly(Date date) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        return makeDate(calendar.get(Calendar.YEAR),
                // calender returns 0-based month while makeDate uses 1-based, that's why we add 1
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static Long toLong(Date date) {
        if (date != null) {
            return date.getTime();
        }
        return null;
    }

    public static Date fromLong(long dateAsLong) {
        return new Date(dateAsLong);
    }

    public static Date addDay(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);

        return c.getTime();
    }

    public static Date getNextSaturday(Date date) {
        Calendar c = Calendar.getInstance();

        c.setTime(date);

        int moreDays = Calendar.SATURDAY - c.get(Calendar.DAY_OF_WEEK);
        if (moreDays <= 0)
            moreDays = 7;

        return addDay(date, moreDays);
    }

    public static Date getPreviousSunday(Date date) {
        Calendar c = Calendar.getInstance();

        c.setTime(date);

        int lessDays = Calendar.SUNDAY - c.get(Calendar.DAY_OF_WEEK);
        if (lessDays >= 0)
            lessDays = -7;

        return addDay(date, lessDays);
    }

    public static Date getClosestSunday(Date date) {
        Calendar c = Calendar.getInstance();

        c.setTime(date);

        int lessDays = Calendar.SUNDAY - c.get(Calendar.DAY_OF_WEEK);
        if (lessDays > 0)
            lessDays = -7;

        return addDay(date, lessDays);
    }

}
