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
        this.configHashMap = defaultConfiguration();
    }

    public static HashMap<String, String> defaultConfiguration() {
        HashMap<String, String> configHashMap = new HashMap<>();

        configHashMap.put("ResourcesFolder", "resources/default");
        configHashMap.put("DigitalClock", "false");
        configHashMap.put("DigitalClockColor", "#004469");
        configHashMap.put("DigitalClockFont", "Courier New");
        configHashMap.put("HourHandLength", "0.25");
        configHashMap.put("MinuteHandLength", "0.36");
        configHashMap.put("SecondHandLength", "0.36");
        configHashMap.put("FlyingImages", "false");
        configHashMap.put("BackgroundColor", "#cabbbb");
        configHashMap.put("Framerate", "60");
        //configHashMap.put("", "");

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
        HashMap<String, String> configHashMap = defaultConfiguration();

        for (String line : linesOfConfigFile) {
            String[] parts = line.split(":");
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].trim();
            }
            configHashMap.put(parts[0], parts[1]);
        }
        return configHashMap;
    }

    public HashMap<String, String> getConfigHashMap() {
        return configHashMap;
    }

}