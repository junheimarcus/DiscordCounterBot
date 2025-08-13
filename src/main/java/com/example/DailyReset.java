package com.example;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DailyReset {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start() {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime nextRun = now.toLocalDate().plusDays(1).atStartOfDay(zoneId);

        long initialDelay = ChronoUnit.MILLIS.between(now, nextRun);
        long period = TimeUnit.DAYS.toMillis(1);

        System.out.println("Scheduling daily reset. Current time: " + now);
        System.out.println("Next reset scheduled at: " + nextRun);
        System.out.println("Initial delay: " + initialDelay + " milliseconds.");

        scheduler.scheduleAtFixedRate(this::resetDailyCounts, initialDelay, period, TimeUnit.MILLISECONDS);
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
}