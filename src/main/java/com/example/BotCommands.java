package com.example;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BotCommands extends ListenerAdapter {
    private static final String MODERATOR_ROLE = "Counter Mod";
    private static final String MASTER_MODERATOR_ROLE = "Counter Mod Master";
    private static final Map<String, String> userNameCache = new ConcurrentHashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "addcount":
                handleAddCommand(event);
                break;
            case "countleaderboard":
                handleLeaderboardCommand(event);
                break;
            case "removecount":
                handleRemoveCommand(event);
                break;
            case "setleaderboardtitle":
                handleSetTitleCommand(event);
                break;
            case "resetleaderboardtitle":
                handleResetTitleCommand(event);
                break;
            case "counterbothelp":
                handleHelpCommand(event);
                break;
            case "fullresetcounterbot":
                handleFullResetCommand(event);
                break;
            case "counterratio":
                handleCounterRatioCommand(event);
                break;
        }
    }

    private void handleCounterRatioCommand(SlashCommandInteractionEvent event) {
        Member member = event.getOption("user").getAsMember();
        String guildId = event.getGuild().getId();
        DataManager.BotData data = DataManager.getGuildData(guildId);

        if (member == null) {
            event.reply("Could not find the specified user.").setEphemeral(true).queue();
            return;
        }

        DataManager.UserData userData = data.users.get(member.getId());
        if (userData == null || userData.totalCount == 0) {
            event.reply(member.getEffectiveName() + " has no counts yet.").setEphemeral(true).queue();
            return;
        }

        long totalServerCount = data.users.values().stream().mapToLong(u -> u.totalCount).sum();
        if (totalServerCount == 0) {
            event.reply("There are no counts in this server yet.").setEphemeral(true).queue();
            return;
        }

        double ratio = (double) userData.totalCount / totalServerCount;
        double percentage = ratio * 100;

        String countTerm = data.leaderboardPrefix.isEmpty() ? "count" : data.leaderboardPrefix + " count";
        event.reply(String.format("%s has %d of the server's %d total %s (%.2f%%).",
                member.getEffectiveName(), userData.totalCount, totalServerCount, countTerm, percentage))
                .queue();
    }

    private void handleHelpCommand(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bot Commands");
        embed.setColor(Color.GREEN);

        embed.addField("/addcount <user> <count> [reason]", "Adds count to a user. (Requires \"Counter Mod\" or \"Counter Mod Master\" role)", false);
        embed.addField("/removecount <user> <count>", "Removes count from a user. (Requires \"Counter Mod\" or \"Counter Mod Master\" role)", false);
        embed.addField("/countleaderboard [page]", "Shows the top users by total count, with pagination, empty count defaults to page 1. (e.g., /countleaderboard 2)", false);
        embed.addField("/setleaderboardtitle <prefix>", "Sets a prefix for the leaderboard title (e.g., \"wombo Leaderboard\"). (Requires \"Counter Mod Master\" role)", false);
        embed.addField("/resetleaderboardtitle", "Resets the leaderboard title to \"Leaderboard\". (Requires \"Counter Mod Master\" role)", false);
        embed.addField("/counterbothelp", "Displays this help message.", false);
        embed.addField("/fullresetcounterbot", "Resets all bot data for this server (leaderboard prefix, all counts and users). (Requires \"Counter Mod Master\" role)", false);
        embed.addField("/counterratio <user>", "Calculates the ratio/percentage of a user's total count against the server total.", false);

        event.replyEmbeds(embed.build()).queue();
    }

    private void handleFullResetCommand(SlashCommandInteractionEvent event) {
        if (!isMasterModerator(event.getMember()) && !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        DataManager.BotData data = DataManager.getGuildData(guildId);
        data.users.clear();
        data.leaderboardPrefix = "";
        DataManager.saveGuildData(guildId, data);
        userNameCache.clear(); // Clear the user name cache for the entire bot
        event.reply("All bot data for this server has been reset.").queue();
    }

    private void handleAddCommand(SlashCommandInteractionEvent event) {
        event.deferReply(false).queue(); // Acknowledge the interaction
        if (!isModerator(event.getMember())) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        Member member = event.getOption("user").getAsMember();
        int count = event.getOption("count").getAsInt();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : null;
        String guildId = event.getGuild().getId();

        DataManager.BotData data = DataManager.getGuildData(guildId);
        DataManager.UserData userData = data.users.computeIfAbsent(member.getId(), k -> new DataManager.UserData());

        userData.dailyCount += count;
        userData.totalCount += count;

        DataManager.saveGuildData(guildId, data);

        String countTerm = data.leaderboardPrefix.isEmpty() ? "count" : data.leaderboardPrefix + " count";
        String replyMessage = "Added " + count + " to " + member.getEffectiveName() + "'s " + countTerm + ". Current total: " + userData.totalCount + ".";
        if (reason != null && !reason.isEmpty()) {
            replyMessage += " Reason: " + reason;
        }
        event.getHook().sendMessage(replyMessage).setEphemeral(false).queue();
    }

    private String getUserName(Guild guild, String userId, int maxNameLength) {
        return userNameCache.computeIfAbsent(userId, id -> {
            try {
                Member member = guild.retrieveMemberById(id).complete();
                String effectiveName = member.getEffectiveName();
                String userName = member.getUser().getName();

                // If effectiveName is too long, try to use the shorter username if it fits
                if (effectiveName.length() > maxNameLength) {
                    if (userName.length() <= maxNameLength) {
                        return userName; // Use username if it fits
                    }
                }
                return effectiveName; // Otherwise, use effectiveName (will be truncated later if needed)
            } catch (Exception e) {
                try {
                    User user = guild.getJDA().retrieveUserById(id).complete();
                    return user.getName();
                } catch (Exception ex) {
                    return "Unknown User";
                }
            }
        });
    }

    private void handleLeaderboardCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue(); // Defer the reply to prevent timeout
        int page = event.getOption("page") != null ? event.getOption("page").getAsInt() : 1;
        sendLeaderboardPage(event, page, event.getGuild().getId(), event.getHook(), false);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("leaderboard:")) {
            event.deferEdit().queue(); // Defer the update to prevent timeout
            String[] parts = event.getComponentId().split(":");
            int newPage = Integer.parseInt(parts[1]);
            sendLeaderboardPage(event, newPage, event.getGuild().getId(), event.getHook(), true);
        }
    }

    private void sendLeaderboardPage(GenericInteractionCreateEvent event, int page, String guildId, InteractionHook hook, boolean isEdit) {
        Guild guild = event.getGuild();
        DataManager.BotData data = DataManager.getGuildData(guildId);
        List<Map.Entry<String, DataManager.UserData>> allSortedUsers = data.users.entrySet().stream()
                .filter(entry -> entry.getValue().totalCount > 0) // Filter out users with 0 total count
                .sorted(Map.Entry.<String, DataManager.UserData>comparingByValue(Comparator.comparingInt(u -> u.totalCount)).reversed())
                .collect(Collectors.toList());

        final int USERS_PER_PAGE = 10;
        int totalUsers = allSortedUsers.size();
        int totalPages = (int) Math.ceil((double) totalUsers / USERS_PER_PAGE);
        if (totalPages == 0) {
            totalPages = 1;
        }

        // Ensure page is within valid bounds
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * USERS_PER_PAGE;
        int endIndex = Math.min(startIndex + USERS_PER_PAGE, totalUsers);

        List<Map.Entry<String, DataManager.UserData>> usersForPage = allSortedUsers.subList(startIndex, endIndex);

        EmbedBuilder embed = new EmbedBuilder();
        String title = data.leaderboardPrefix.isEmpty() ? "Leaderboard" : data.leaderboardPrefix + " Leaderboard";
        embed.setTitle(title + " (Page " + page + "/" + totalPages + ")");
        embed.setColor(Color.BLUE);

        StringBuilder description = new StringBuilder();
        if (usersForPage.isEmpty()) {
            description.append("No counts yet! Be the first to add one.");
        } else {
            final int MAX_NAME_LENGTH = 15; // Set a max length for names to ensure mobile readability
            description.append("```\n"); // Use a plain code block for better mobile scrolling
            // Header
            description.append(String.format("%-3s | %-" + MAX_NAME_LENGTH + "s | %-5s | %-7s%n", "Rank", "User", "Daily", "Total"));
            description.append(String.format("--- | %-" + MAX_NAME_LENGTH + "s | %-5s | %-7s%n", "-----------------".substring(0, MAX_NAME_LENGTH), "-----", "-------"));

            for (int i = 0; i < usersForPage.size(); i++) {
                Map.Entry<String, DataManager.UserData> entry = usersForPage.get(i);
                String name = getUserName(guild, entry.getKey(), MAX_NAME_LENGTH);
                if (name.length() > MAX_NAME_LENGTH) {
                    name = name.substring(0, MAX_NAME_LENGTH - 3) + "...";
                }

                description.append(String.format("%-3d | %-" + MAX_NAME_LENGTH + "s | %-5d | %-7d%n",
                        startIndex + i + 1, name, entry.getValue().dailyCount, entry.getValue().totalCount));
            }
            description.append("```");
        }

        embed.setDescription(description.toString());

        // Add buttons for pagination
        Button prevButton = Button.primary("leaderboard:" + (page - 1) + ":" + totalPages, "Previous Page").withDisabled(page == 1);
        Button nextButton = Button.primary("leaderboard:" + (page + 1) + ":" + totalPages, "Next Page").withDisabled(page == totalPages);

        if (totalPages > 1) {
            if (isEdit) {
                hook.editOriginalEmbeds(embed.build())
                        .setActionRow(prevButton, nextButton)
                        .queue();
            } else {
                hook.sendMessageEmbeds(embed.build())
                        .addActionRow(prevButton, nextButton)
                        .queue();
            }
        } else {
            if (isEdit) {
                hook.editOriginalEmbeds(embed.build())
                        .setActionRow()
                        .queue();
            } else {
                hook.sendMessageEmbeds(embed.build())
                        .queue();
            }
        }
    }

    private void handleRemoveCommand(SlashCommandInteractionEvent event) {
        if (!isModerator(event.getMember())) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        Member member = event.getOption("user").getAsMember();
        int countToRemove = event.getOption("count").getAsInt();
        String guildId = event.getGuild().getId();

        DataManager.BotData data = DataManager.getGuildData(guildId);
        DataManager.UserData userData = data.users.get(member.getId());

        if (userData == null) {
            event.reply(member.getEffectiveName() + " does not have any counts yet.").setEphemeral(true).queue();
            return;
        }

        userData.dailyCount -= countToRemove;
        userData.totalCount -= countToRemove;

        if (userData.dailyCount < 0) userData.dailyCount = 0;
        if (userData.totalCount <= 0) { // Changed to <= 0 to remove if count becomes 0 or less
            data.users.remove(member.getId()); // Remove user from data
            event.reply("Removed " + countToRemove + " from " + member.getEffectiveName() + "'s count. User removed from leaderboard.").queue();
        } else {
            String countTerm = data.leaderboardPrefix.isEmpty() ? "count" : data.leaderboardPrefix + " count";
            event.reply("Removed " + countToRemove + " from " + member.getEffectiveName() + "'s " + countTerm + ". Current total: " + userData.totalCount + ".").queue();
        }

        DataManager.saveGuildData(guildId, data);
    }

    private void handleSetTitleCommand(SlashCommandInteractionEvent event) {
        if (!isMasterModerator(event.getMember())) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String newPrefix = event.getOption("prefix").getAsString();
        String guildId = event.getGuild().getId();

        DataManager.BotData data = DataManager.getGuildData(guildId);
        data.leaderboardPrefix = newPrefix;
        DataManager.saveGuildData(guildId, data);

        event.reply("Leaderboard prefix updated to: " + newPrefix).queue();
    }

    private void handleResetTitleCommand(SlashCommandInteractionEvent event) {
        if (!isMasterModerator(event.getMember())) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();
        DataManager.BotData data = DataManager.getGuildData(guildId);
        data.leaderboardPrefix = "";
        DataManager.saveGuildData(guildId, data);

        event.reply("Leaderboard title has been reset to default.").queue();
    }

    private boolean isModerator(Member member) {
        if (member == null) return false;
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        for (Role role : member.getRoles()) {
            if (role.getName().equalsIgnoreCase(MODERATOR_ROLE) || role.getName().equalsIgnoreCase(MASTER_MODERATOR_ROLE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMasterModerator(Member member) {
        if (member == null) return false;
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        for (Role role : member.getRoles()) {
            if (role.getName().equalsIgnoreCase(MASTER_MODERATOR_ROLE)) {
                return true;
            }
        }
        return false;
    }
}