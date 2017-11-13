package com.company;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.company.Utils.parseTime;

public class Schedule {
    private LinkedHashMap<String, Period> periods = new LinkedHashMap<>();
    String name;

    public static final int EMPTY_SCHEDULE = 0;
    public static final int DEFAULT_SCHEDULE = 1;
    public static final int H_PERIOD_BEGINNING_SCHEDULE = 2;
    public static final int H_PERIOD_ENDING_SCHEDULE = 3;
    public static final int TEST_SCHEDULE = 4;

    /**
    *@param schedule integer representing which schedule to use
    *
    *leave blank for empty schedule
    */
    public Schedule(int schedule) {
        setSchedule(schedule);
    }

    public Schedule(int schedule, String name) {
        this.name = name;
        setSchedule(schedule);
    }

    private void setSchedule(int schedule) {
        if (!(schedule <= 4 && schedule >= 0)) {
            schedule = EMPTY_SCHEDULE;
        }

        if (schedule == DEFAULT_SCHEDULE) {
            periods.put("A", new Period(parseTime("8:19"), parseTime("9:07")));
            periods.put("B", new Period(parseTime("9:11"), parseTime("9:59")));
            periods.put("C", new Period(parseTime("10:03"), parseTime("10:51")));
            periods.put("D", new Period(parseTime("10:55"), parseTime("11:43")));
            periods.put("E", new Period(parseTime("11:47"), parseTime("12:35")));
            periods.put("Lunch", new Period(parseTime("12:35"), parseTime("13:01")));
            periods.put("F", new Period(parseTime("13:01"), parseTime("13:53")));
            periods.put("G", new Period(parseTime("13:57"), parseTime("14:45")));
        } else if (schedule == H_PERIOD_ENDING_SCHEDULE) {
            periods.put("A", new Period(parseTime("8:15"), parseTime("8:57")));
            periods.put("B", new Period(parseTime("9:01"), parseTime("9:43")));
            periods.put("C", new Period(parseTime("9:47"), parseTime("10:29")));
            periods.put("D", new Period(parseTime("10:33"), parseTime("11:15")));
            periods.put("E", new Period(parseTime("11:19"), parseTime("12:01")));
            periods.put("F", new Period(parseTime("12:05"), parseTime("12:47")));
            periods.put("Lunch", new Period(parseTime("12:47"), parseTime("13:18")));
            periods.put("G", new Period(parseTime("13:18"), parseTime("14:00")));
            periods.put("H", new Period(parseTime("14:03"), parseTime("14:45")));
        } else if (schedule == H_PERIOD_BEGINNING_SCHEDULE) {
            periods.put("H", new Period(parseTime("8:15"), parseTime("8:57")));
            periods.put("A", new Period(parseTime("9:01"), parseTime("9:43")));
            periods.put("B", new Period(parseTime("9:47"), parseTime("10:29")));
            periods.put("C", new Period(parseTime("10:33"), parseTime("11:15")));
            periods.put("D", new Period(parseTime("11:19"), parseTime("12:01")));
            periods.put("E", new Period(parseTime("12:05"), parseTime("12:47")));
            periods.put("Lunch", new Period(parseTime("12:47"), parseTime("13:18")));
            periods.put("F", new Period(parseTime("13:18"), parseTime("14:00")));
            periods.put("G", new Period(parseTime("14:03"), parseTime("14:45")));
        } else if (schedule == TEST_SCHEDULE) {
            periods.put("A", new Period(parseTime("00:00"), parseTime("01:00")));
            periods.put("B", new Period(parseTime("11:53"), parseTime("11:54")));
            periods.put("C", new Period(parseTime("11:54"), parseTime("11:55")));
        }
    }

    public void addToSchedule(String periodName, Period period) {
        periods.put(periodName, period);
    }

    public boolean removeFromSchedule(String periodName) {
        try {
            periods.remove(periodName);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public LinkedHashMap<String, Period> getPeriods() {
        return periods;
    }

    public void setPeriods(LinkedHashMap<String, Period> periods) {
        this.periods = periods;
    }
}


