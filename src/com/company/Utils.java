package com.company;

import javax.swing.*;
import java.awt.*;
import java.sql.Time;
import java.util.List;
import java.util.Random;

public class Utils {

    static Random r = new Random();

    public static Time parseTime(String time) {
        String[] parts = time.split(":");
        if (parts.length == 1) {
            return null;
        } else if (parts.length == 2) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if ((hour < 24 && hour >= 0) && ((minute < 60 && minute >= 0))) {
                return new Time(((hour * 3600) + (minute * 60)) * 1000);
            }
        } else if (parts.length == 3) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = Integer.parseInt(parts[2]);

            if ((hour < 24 && hour >= 0) && (minute < 60 && minute >= 0) && (second < 60 && second >= 0)) {
                return new Time(((hour * 3600) + (minute * 60) + second) * 1000);
            }
        }
        return null;
    }

    public static Time parseTimeWithMeridian(String time) {
        if (time.endsWith("PM")) {
            int hour = Integer.parseInt(time.substring(0, time.indexOf(":")));
            hour += 12;
            if (hour == 24) {
                hour = 0;
            }
            time = hour + time.substring(time.indexOf(":"), time.length() - 2);
        } else {
            time = time.substring(0, time.length() - 2);
        }
        String[] parts = time.split(":");
        if (parts.length == 1) {
            return null;
        } else if (parts.length == 2) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if ((hour < 24 && hour >= 0) && ((minute < 60 && minute >= 0))) {
                return new Time(((hour * 3600) + (minute * 60)) * 1000);
            }
        } else if (parts.length == 3) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = Integer.parseInt(parts[2]);

            if ((hour < 24 && hour >= 0) && (minute < 60 && minute >= 0) && (second < 60 && second >= 0)) {
                return new Time(((hour * 3600) + (minute * 60) + second) * 1000);
            }
        }
        return null;
    }


    /**
     *
     * @param time
     * @param withMeridian - true to include AM/PM in result
     * @return String representing time in 12 hour format
     */
    public static String get12HourStringFromTime(Time time, boolean withMeridian) {
        String hour = "" + (time.getTime() / 3600000);
        String minute = "" + ((time.getTime() % 3600000) / 60000);
        boolean isPM = false;

        if (Integer.parseInt(hour) > 12) {
            hour = "" + (Integer.parseInt(hour) - 12);
            isPM = true;
        }

        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        if (minute.length() == 1) {
            minute = "0" + minute;
        }

        if (isPM) {
            return hour + ":" + minute + " PM";
        } else {
            return hour + ":" + minute + " AM";
        }
    }

    public static String get24HourStringFromTime(Time time) {
        String hour = "" + (time.getTime() / 3600000);
        String minute = "" + ((time.getTime() % 3600000) / 60000);

        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        if (minute.length() == 1) {
            minute = "0" + minute;
        }

        return hour + ":" + minute;
    }

    public static String multiplyString(String string, int times) {
        String returnString = "";
        for (int i = 0; i < times; i++) {
            returnString += string;
        }
        return returnString;
    }

    public static Dimension getLargestLabelSizeInList(List<JLabel> list) {
        Dimension largestSize = new Dimension(0, 0);
        for (JLabel label : list) {
            if (label.getWidth() > largestSize.getWidth()) {
                largestSize = label.getSize();
            }
        }
        return largestSize;
    }

    public static float randomNumberBetweenTwoFloats (float f1, float f2) {
        f1 *= 10000;
        f2 *= 10000;

        int one = (int) f1;
        int two = (int) f2;

        int randInt = r.nextInt(two - one) + one;
        return (float) randInt / 10000;
    }

    public static int randomNumberWithinBounds(int min, int max) {
        int difference = max - min;
        return r.nextInt(difference) + min;
    }
}

