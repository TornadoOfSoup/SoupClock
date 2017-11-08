package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ScheduleParser {
    HashMap<String, String> scheduleHashMap;


    public ScheduleParser(File file) {
        ArrayList<String> linesOfScheduleFile = readLinesOfFile(file);
        this.scheduleHashMap = buildScheduleHashMap(linesOfScheduleFile);
    }

    public ScheduleParser() {
        this.scheduleHashMap = defaultSchedule();
    }

    public static HashMap<String, String> defaultSchedule() {
        HashMap<String, String> scheduleHashMap = new HashMap<>();
        return scheduleHashMap;
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

    public static HashMap<String, String> buildScheduleHashMap(ArrayList<String> linesOfScheduleFile) {
        HashMap<String, String> scheduleHashMap = defaultSchedule();

        for (String line : linesOfScheduleFile) {
            String[] parts = line.split(":");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
            if (!line.isEmpty() && !line.startsWith("//")) { //ignore empty lines and commented out lines
                scheduleHashMap.put(parts[0], parts[1]);
            }
        }
        return scheduleHashMap;
    }

    public HashMap<String, String> getScheduleHashMap() {
        return scheduleHashMap;
    }
}
