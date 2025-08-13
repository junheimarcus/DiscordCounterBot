package com.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Main {
    public static void main(String[] args) throws Exception {
        String token = System.getenv("DISCORD_TOKEN");
        if (token == null) {
            System.out.println("DISCORD_TOKEN environment variable not set.");
            return;
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new BotCommands())
                .build();

        // Register slash commands
        jda.updateCommands().addCommands(
                Commands.slash("addcount", "Adds count to a user.")
                        .addOption(OptionType.USER, "user", "The user to add count to.", true)
                        .addOption(OptionType.INTEGER, "count", "The amount of count to add.", true)
                        .addOption(OptionType.STRING, "reason", "The reason for adding the count.", false),
                Commands.slash("removecount", "Removes count from a user.")
                        .addOption(OptionType.USER, "user", "The user to remove count from.", true)
                        .addOption(OptionType.INTEGER, "count", "The amount of count to remove.", true),
                Commands.slash("countleaderboard", "Shows the top users by total count.")
                        .addOption(OptionType.INTEGER, "page", "The page number to display.", false),
                Commands.slash("setleaderboardtitle", "Sets a prefix for the leaderboard title.")
                        .addOption(OptionType.STRING, "prefix", "The prefix for the leaderboard title.", true),
                Commands.slash("resetleaderboardtitle", "Resets the leaderboard title to default."),
                Commands.slash("counterbothelp", "Displays the bot's commands."),
                Commands.slash("fullresetcounterbot", "Resets all bot data for this server (leaderboard prefix, all counts and users)."),
                Commands.slash("counterratio", "Calculates the ratio of a user's count to the server's total count.")
                        .addOption(OptionType.USER, "user", "The user to calculate the ratio for.", true)
        ).queue();

        new DailyReset().start();
    }
}