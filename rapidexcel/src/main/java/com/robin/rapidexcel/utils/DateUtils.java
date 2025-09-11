package com.robin.rapidexcel.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    private static final BigDecimal BD_NANOSEC_DAY = BigDecimal.valueOf(8.64E13);
    private static final BigDecimal BD_MILISEC_RND = BigDecimal.valueOf(500000.0);
    private static final BigDecimal BD_SECOND_RND = BigDecimal.valueOf(5.0E8);
    private static final int BAD_DATE = -1;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int SECONDS_PER_DAY = (HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE);
    public static final long DAY_MILLISECONDS = SECONDS_PER_DAY * 1000L;

    public static Double convertDate(Date date) {
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        int year = calStart.get(Calendar.YEAR);
        int dayOfYear = calStart.get(Calendar.DAY_OF_YEAR);
        int hour = calStart.get(Calendar.HOUR_OF_DAY);
        int minute = calStart.get(Calendar.MINUTE);
        int second = calStart.get(Calendar.SECOND);
        int milliSecond = calStart.get(Calendar.MILLISECOND);
        return internalGetExcelDate(year, dayOfYear, hour, minute, second, milliSecond);
    }

    public static Double convertDate(LocalDateTime date) {
        int year = date.getYear();
        int dayOfYear = date.getDayOfYear();
        int hour = date.getHour();
        int minute = date.getMinute();
        int second = date.getSecond();
        int milliSecond = date.getNano() / 1_000_000;
        return internalGetExcelDate(year, dayOfYear, hour, minute, second, milliSecond);
    }


    public static Double convertDate(LocalDate date) {
        int year = date.getYear();
        int dayOfYear = date.getDayOfYear();
        int hour = 0;
        int minute = 0;
        int second = 0;
        int milliSecond = 0;
        return internalGetExcelDate(year, dayOfYear, hour, minute, second, milliSecond);
    }


    public static Double convertZonedDateTime(ZonedDateTime zdt) {
        return convertDate(zdt.toLocalDateTime());
    }

    private static double internalGetExcelDate(int year, int dayOfYear, int hour, int minute, int second, int milliSecond) {
        if (year < 1900) {
            return BAD_DATE;
        }

        // Because of daylight time saving we cannot use
        //     date.getTime() - calStart.getTimeInMillis()
        // as the difference in milliseconds between 00:00 and 04:00
        // can be 3, 4 or 5 hours but Excel expects it to always
        // be 4 hours.
        // E.g. 2004-03-28 04:00 CEST - 2004-03-28 00:00 CET is 3 hours
        // and 2004-10-31 04:00 CET - 2004-10-31 00:00 CEST is 5 hours
        double fraction = (((hour * 60.0 + minute) * 60.0 + second) * 1000.0 + milliSecond) / DAY_MILLISECONDS;

        double value = fraction + absoluteDay(year, dayOfYear);

        if (value >= 60) {
            value++;
        }

        return value;
    }

    private static int absoluteDay(int year, int dayOfYear) {
        return dayOfYear + daysInPriorYears(year);
    }

    static int daysInPriorYears(int yr) {
        if (yr < 1900) {
            throw new IllegalArgumentException("'year' must be 1900 or greater");
        }
        int yr1 = yr - 1;
        int leapDays = yr1 / 4   // plus julian leap days in prior years
                - yr1 / 100 // minus prior century years
                + yr1 / 400 // plus years divisible by 400
                - 460;      // leap days in previous 1900 years

        return 365 * (yr - 1900) + leapDays;
    }

    public static LocalDateTime getLocalDateTime(double date) {
        return getLocalDateTime(date, false, false);
    }



    public static LocalDateTime getLocalDateTime(double date, boolean use1904windowing, boolean roundSeconds) {
        if (!isValidExcelDate(date)) {
            return null;
        } else {
            BigDecimal bd = new BigDecimal(date);
            int wholeDays = bd.intValue();
            int startYear = 1900;
            int dayAdjust = -1;
            if (use1904windowing) {
                startYear = 1904;
                dayAdjust = 1;
            } else if (wholeDays < 61) {
                dayAdjust = 0;
            }

            LocalDateTime ldt = LocalDateTime.of(startYear, 1, 1, 0, 0);
            ldt = ldt.plusDays((long)(wholeDays + dayAdjust - 1));
            long nanosTime = bd.subtract(BigDecimal.valueOf((long)wholeDays)).multiply(BD_NANOSEC_DAY).add(roundSeconds ? BD_SECOND_RND : BD_MILISEC_RND).longValue();
            ldt = ldt.plusNanos(nanosTime);
            ldt = ldt.truncatedTo(roundSeconds ? ChronoUnit.SECONDS : ChronoUnit.MILLIS);
            return ldt;
        }
    }
    public static boolean isValidExcelDate(double value) {
        return value > -4.9E-324;
    }
}
