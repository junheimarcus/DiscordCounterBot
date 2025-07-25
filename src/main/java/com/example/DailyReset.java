package com.example;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyReset {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start() {
        long initialDelay = calculateInitialDelay();
        scheduler.scheduleAtFixedRate(this::resetDailyCounts, initialDelay, 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    private void resetDailyCounts() {
        Map<String, DataManager.BotData> allData = DataManager.loadAllData();
        for (DataManager.BotData data : allData.values()) {
            for (DataManager.UserData userData : data.users.values()) {
                userData.dailyCount = 0;
            }
        }
        DataManager.saveAllData(allData);
        System.out.println("Daily counts have been reset for all guilds.");
    }

    private long calculateInitialDelay() {
        long now = System.currentTimeMillis();
        long midnight = (now / (24 * 60 * 60 * 1000) + 1) * (24 * 60 * 60 * 1000);
        return midnight - now;
    }
}