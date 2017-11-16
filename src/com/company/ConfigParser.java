package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigParser {

    HashMap<String, String> configHashMap;


    public ConfigParser(File file) {
        ArrayList<String> linesOfConfigFile = readLinesOfFile(file);
        this.configHashMap = buildConfigHashMap(linesOfConfigFile);
    }

    public ConfigParser() {
        this.configHashMap = defaultConfiguration(true);
    }

    public static HashMap<String, String> defaultConfiguration(boolean checkForLocalFile) {
        HashMap<String, String> configHashMap = new HashMap<>();
        String currentDir = Main.currentDirectory().getPath();
        File defaultFile = new File(currentDir.substring(0, currentDir.lastIndexOf(File.separator))
                + "/resources/config/default.txt");
        System.out.println("Searching " + defaultFile.getPath() + " for default.txt");

        if (defaultFile.exists() && checkForLocalFile) {
            configHashMap = buildConfigHashMap(readLinesOfFile(defaultFile));
            System.out.println("Found default file! Using it...");
        } else {
            if (checkForLocalFile) {
                System.out.println("Couldn't find default file, using internal alternative...");
            } else {
                System.out.println("Was told not to look for internal file");
            }

            configHashMap.put("ResourcesFolder", "resources/default");
            configHashMap.put("ClockSize", "0.90");
            configHashMap.put("DigitalClock", "false");
            configHashMap.put("DigitalClockColor", "#00ff00");
            configHashMap.put("DigitalClockFont", "Courier New");
            configHashMap.put("DigitalClockSize", "48");
            configHashMap.put("Schedule", "true");
            configHashMap.put("ScheduleColor", "#00ff00");
            configHashMap.put("ScheduleFont", "Consolas");
            configHashMap.put("ScheduleSize", "24");
            configHashMap.put("PeriodHighlightColor", "#000000");
            configHashMap.put("HourHandLength", "0.25");
            configHashMap.put("MinuteHandLength", "0.36");
            configHashMap.put("SecondHandLength", "0.36");
            configHashMap.put("FlyingImages", "false");
            configHashMap.put("BackgroundColor", "#46494e");
            configHashMap.put("Framerate", "60");
            configHashMap.put("RandomImages", "resources/default/a.jpg, 5, random, 12-15, 0.3-0.8 | resources/default/b.png, 8, random, 20-28, 0.6-1.0");
            //configHashMap.put("", "");
        }

        return configHashMap;
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

    public static HashMap<String, String> buildConfigHashMap(ArrayList<String> linesOfConfigFile) {
        HashMap<String, String> configHashMap = defaultConfiguration(false);

        for (String line : linesOfConfigFile) {
            String[] parts = line.split(":");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
            if (!line.isEmpty() && !line.startsWith("//")) { //ignore empty lines and commented out lines
                configHashMap.put(parts[0], parts[1]);
            }
        }
        return configHashMap;
    }

    public HashMap<String, String> getConfigHashMap() {
        return configHashMap;
    }

}