# Discord Counter Bot

This is a simple yet powerful Discord bot for tracking counts of anything you can imagine! Whether you're counting wins, memes, or mistakes, this bot has you covered.

## Features

*   **User-friendly commands:** Easily add, remove, and view counts for users.
*   **Leaderboard:** See who's at the top with a paginated leaderboard.
*   **Customizable Leaderboard:** Set a custom title for your leaderboard to match your server's theme.
*   **Role-Based Permissions:** Control who can manage counts with `Counter Mod` and `Counter Mod Master` roles.
*   **Data Persistence:** All data is saved to a `data.json` file, so you don't have to worry about losing your counts.
*   **Daily Count Reset:** The bot automatically resets the daily count for all users every day.
*   **Count Ratio:** See how a user's count compares to the total server count.

## Commands

*   `/addcount <user> <count> [reason]`: Adds a specified count to a user.
    *   Requires `Counter Mod` or `Counter Mod Master` role.
*   `/removecount <user> <count>`: Removes a specified count from a user.
    *   Requires `Counter Mod` or `Counter Mod Master` role.
*   `/countleaderboard [page]`: Displays a paginated leaderboard of users with the highest counts.
*   `/setleaderboardtitle <prefix>`: Sets a custom prefix for the leaderboard title.
    *   Requires `Counter Mod Master` role.
*   `/resetleaderboardtitle`: Resets the leaderboard title to the default.
    *   Requires `Counter Mod Master` role.
*   `/counterbothelp`: Displays a help message with all the commands.
*   `/fullresetcounterbot`: Resets all bot data for the server.
    *   Requires `Counter Mod Master` role.
*   `/counterratio <user>`: Calculates the ratio of a user's count to the total server count.

## Roles and Permissions

*   **Counter Mod:**
    *   Can use the `/addcount` and `/removecount` commands.
*   **Counter Mod Master:**
    *   Has all the permissions of a `Counter Mod`.
    *   Can use the `/setleaderboardtitle`, `/resetleaderboardtitle`, and `/fullresetcounterbot` commands.
*   **Administrator:**
    *   Has all the permissions of a `Counter Mod Master`.

## Installation and Setup

1.  **Clone the repository**

2.  **Set up your Discord bot:**
    *   Go to the [Discord Developer Portal](https://discord.com/developers/applications) and create a new application.
    *   Go to the "Bot" tab and click "Add Bot".
    *   Copy the bot's token.
3.  **Set up your environment variables:**
    *   Create a `.env` file in the root of the project.
    *   Add the following line to the `.env` file:
        ```
        DISCORD_TOKEN=your-bot-token
        ```
4.  **Build the project:**
    ```bash
    mvn clean package
    ```
5.  **Run the bot:**
    ```bash
    java -jar target/discord-counter-bot-1.0.0.jar
    ```

## Dependencies

*   [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)
*   [Gson](https://github.com/google/gson)
