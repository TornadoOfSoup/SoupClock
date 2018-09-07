package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.company.Utils.parseTime;

public class ScheduleParser {
    public Schedule schedule;


    public ScheduleParser(File file) {
        ArrayList<String> linesOfScheduleFile = readLinesOfFile(file);
        schedule = buildSchedule(linesOfScheduleFile);
    }

    public ScheduleParser() {
        schedule = new Schedule(1);
    }

    public static ArrayList<Period> defaultSchedule() {
        ArrayList<Period> periods = new ArrayList<>();

        periods.add(new Period(parseTime("8:05"), parseTime("8:55"), "A"));
        periods.add(new Period(parseTime("8:58"), parseTime("9:42"), "B"));
        periods.add(new Period(parseTime("9:45"), parseTime("10:30"), "C"));
        periods.add(new Period(parseTime("10:33"), parseTime("11:03"), "X"));
        periods.add(new Period(parseTime("11:06"), parseTime("11:51"), "D"));
        periods.add(new Period(parseTime("11:54"), parseTime("12:39"), "E"));
        periods.add(new Period(parseTime("12:39"), parseTime("13:09"), "Lunch"));
        periods.add(new Period(parseTime("13:01"), parseTime("13:53"), "F"));
        periods.add(new Period(parseTime("13:57"), parseTime("14:45"), "G"));

        return periods;
    }

    public static ArrayList<String> readLinesOfFile(File file) {
        ArrayList<String> lineList = new ArrayList<>();
        try {

            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);

            String line;
            while ((line = reader.readLine()) != null) {
                lineList.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineList;
    }

    public static Schedule buildSchedule(ArrayList<String> linesOfScheduleFile) {
        ArrayList<Period> scheduleList = new ArrayList<>();
        Schedule schedule = new Schedule(0);

        for (String line : linesOfScheduleFile) {
            String[] parts = new String[2];

            if (!line.isEmpty() && !line.startsWith("//") && line.contains(":")) { //ignore empty lines and commented out lines and lines that don't have colons in them
                parts[0] = line.substring(0, line.indexOf(":"));
                parts[1] = line.substring(line.indexOf(":") + 1, line.length());
                String[] times = parts[1].trim().split(" - ");

                //System.out.println(parts[0] + " | " + parts[1]);
                scheduleList.add(new Period(parseTime(times[0]), parseTime(times[1]), parts[0]));
            } else {
                if (line.startsWith("NAME - ")) {
                    schedule.setName(line.replace("NAME - ", ""));
                }
            }
        }
        schedule.setPeriods(scheduleList);
        return schedule;
    }

    public Schedule getSchedule() {
        return schedule;
    }
}
