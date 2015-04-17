package com.ramonli.stockforum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * Manipulate day field of date.
     */
    public static Date addDay(Date date, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, num);
        return calendar.getTime();
    }

    /**
     * Manipulate year field of date.
     */
    public static Date addYear(Date date, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, num);
        return calendar.getTime();
    }

    public static boolean isSameDay(Date date1, Date date2) {
        return format(date1, "yyyyMMdd").equalsIgnoreCase(format(date2, "yyyyMMdd"));
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static Date parse(String dateStr, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(isSameDay(parse("2015-04-17", "yyyy-MM-dd"), new Date()));
    }
}
