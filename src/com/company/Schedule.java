package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.company.Utils.parseTime;

public class Schedule {
    private ArrayList<Period> periods = new ArrayList<>();
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
        setName("");
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
            name = "Normal Schedule";
            periods.add(new Period(parseTime("8:05"), parseTime("8:55"), "A"));
            periods.add(new Period(parseTime("8:58"), parseTime("9:42"), "B"));
            periods.add(new Period(parseTime("9:45"), parseTime("10:30"), "C"));
            periods.add(new Period(parseTime("10:33"), parseTime("11:03"), "X"));
            periods.add(new Period(parseTime("11:06"), parseTime("11:51"), "D"));
            periods.add(new Period(parseTime("11:54"), parseTime("12:39"), "E"));
            periods.add(new Period(parseTime("12:39"), parseTime("13:09"), "Lunch"));
            periods.add(new Period(parseTime("13:01"), parseTime("13:53"), "F"));
            periods.add(new Period(parseTime("13:57"), parseTime("14:45"), "G"));
        } else if (schedule == H_PERIOD_ENDING_SCHEDULE) {
            name = "H Period Schedule";
            periods.add(new Period(parseTime("00:00"), parseTime("23:59"), "H"));
        } else if (schedule == H_PERIOD_BEGINNING_SCHEDULE) {
            name = "H Period Schedule";
            periods.add(new Period(parseTime("00:00"), parseTime("23:59"), "H"));
        } else if (schedule == TEST_SCHEDULE) {
            name = "Test";
            periods.add(new Period(parseTime("8:05"), parseTime("8:55"), "A"));
            periods.add(new Period(parseTime("8:58"), parseTime("9:42"), "B"));
            periods.add(new Period(parseTime("9:45"), parseTime("10:30"), "C"));
        }
    }

    public void addToSchedule(String periodName, Period period) {
        periods.add(period);
    }

    public boolean removeFromSchedule(String periodName) {
        try {
            periods.remove(periodName);
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Period> getPeriods() {
        return periods;
    }

    public void setPeriods(ArrayList<Period> periods) {
        this.periods = periods;
    }
}


