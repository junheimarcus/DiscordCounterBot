package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DataManager {
    private static final String DATA_FILE = "data.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, BotData>>() {}.getType();

    public static synchronized Map<String, BotData> loadAllData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Map<String, BotData> data = GSON.fromJson(reader, TYPE);
            return data != null ? data : new HashMap<>();
        } catch (IOException e) {
            return new HashMap<>();
        }
    }

    public static synchronized void saveAllData(Map<String, BotData> allData) {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            GSON.toJson(allData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BotData getGuildData(String guildId) {
        Map<String, BotData> allData = loadAllData();
        return allData.computeIfAbsent(guildId, k -> new BotData());
    }

    public static void saveGuildData(String guildId, BotData data) {
        Map<String, BotData> allData = loadAllData();
        allData.put(guildId, data);
        saveAllData(allData);
    }

    public static class UserData {
        public int dailyCount;
        public int totalCount;
    }

    public static class BotData {
        public Map<String, UserData> users = new HashMap<>();
        public String leaderboardPrefix = "";
    }
}
