package com.company;

import java.sql.Time;

import static com.company.Utils.get12HourStringFromTime;

public class Period {
    Time startTime, endTime;


    public Period(Time startTime, Time endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

        checkTimeValidity();
    }

    public boolean checkTimeValidity() {
        if (startTime.after(endTime)) {
            throw new IllegalArgumentException("startTime must logically go before endTime!");
        } else {
            return true;
        }
    }

    public void setStartTime(long time) {
        this.startTime = new Time(time);
        checkTimeValidity();
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
        checkTimeValidity();
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
        checkTimeValidity();
    }

    public void setEndTime(long time) {
        this.endTime = new Time(time);
        checkTimeValidity();
    }

    public Time getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return get12HourStringFromTime(startTime, true) + " - " + get12HourStringFromTime(endTime, true);
    }
}
