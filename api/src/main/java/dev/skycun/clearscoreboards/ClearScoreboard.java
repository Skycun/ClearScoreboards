package dev.skycun.clearscoreboards;

import dev.skycun.clearscoreboards.abstraction.InternalObjectiveWrapper;
import dev.skycun.clearscoreboards.abstraction.InternalTeamWrapper;
import dev.skycun.clearscoreboards.exception.DuplicateTeamCreatedException;
import dev.skycun.clearscoreboards.exception.ScoreboardLineTooLongException;
import dev.skycun.clearscoreboards.exception.ScoreboardTeamNameTooLongException;
import dev.skycun.clearscoreboards.versioning.SpigotAPIVersion;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for all scoreboard implementations.
 *
 * <p><b>Thread Safety:</b> This class uses thread-safe collections (CopyOnWriteArrayList and ConcurrentHashMap)
 * to support concurrent access from multiple threads. However, for best performance, it is recommended
 * to access scoreboards primarily from the main server thread.</p>
 */
public abstract class ClearScoreboard {
  private ClearScoreboardOptions options;

  private final InternalObjectiveWrapper objectiveWrapper;
  private final InternalTeamWrapper teamWrapper;

  private final List<ClearScoreboardTeam> teams = new CopyOnWriteArrayList<>();
  private final List<UUID> activePlayers = new CopyOnWriteArrayList<>();

  private final Map<Scoreboard, List<String>> previousLinesMap = new ConcurrentHashMap<>();

  private final int maxLineLength;

  public ClearScoreboard() {
    try {
      objectiveWrapper = SpigotAPIVersion.getCurrent().makeObjectiveWrapper();
      teamWrapper = SpigotAPIVersion.getCurrent().makeInternalTeamWrapper();
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
      throw new IllegalStateException(
          "Failed to initialize ClearScoreboards for Minecraft version " + SpigotAPIVersion.getCurrent() +
          ". Please report this issue to https://github.com/Skycun/ClearScoreboards with the full stacktrace.", e);
    }

    if (SpigotAPIVersion.getCurrent().lessThan(SpigotAPIVersion.v1_13)) {
      maxLineLength = 32;
    } else {
      maxLineLength = 128;
    }
  }

  // MARK: Public API

  /**
   * Add a player to the scoreboard
   * @param player The player to add
   */
  public void addPlayer(Player player) {
    if (activePlayers.contains(player.getUniqueId())) return;

    this.activePlayers.add(player.getUniqueId());
  }

  /**
   * Remove the player from the ClearScoreboard, and remove any teams they may be a member of.
   * This will reset their scoreboard to the main scoreboard.
   * @param player The player to remove
   */
  public void removePlayer(Player player) {
    this.activePlayers.remove(player.getUniqueId());
    Validate.notNull(Bukkit.getScoreboardManager());
    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

    teams.forEach(team -> {
      if (team.isOnTeam(player.getUniqueId())) {
        team.removePlayer(player);
      }
    });
  }

  /**
   * Find a team using a name
   * @param name The name to search for. Color codes will be stripped from both the team name and this variable.
   * @return The ClearScoreboardPlayerTeam found, if any. Will return null if no team exists
   */
  public Optional<ClearScoreboardTeam> findTeam(String name) {
    return teams.stream()
        .filter(team -> ChatColor.stripColor(team.getName()).equalsIgnoreCase(ChatColor.stripColor(name)))
        .findAny();
  }

  /**
   * Create a team on the scoreboard. ChatColor.WHITE is used as the color for the team.
   * @param name The name for the new team. This name cannot be longer than 16 characters
   * @return The created ClearScoreboardPlayerTeam
   * @throws DuplicateTeamCreatedException If a team with that name already exists
   * @throws ScoreboardTeamNameTooLongException If the team's name is longer than 16 characters
   */
  public ClearScoreboardTeam createTeam(String name, String displayName) throws DuplicateTeamCreatedException, ScoreboardTeamNameTooLongException {
    return createTeam(name, displayName, ChatColor.WHITE);
  }

  /**
   * Create a team on the scoreboard.
   * @param name The name for the new team. This name cannot be longer than 16 characters
   * @return The created ClearScoreboardPlayerTeam
   * @throws DuplicateTeamCreatedException If a team with that name already exists
   * @throws ScoreboardTeamNameTooLongException If the team's name is longer than 16 characters
   */
  public ClearScoreboardTeam createTeam(String name, String displayName, ChatColor teamColor) throws DuplicateTeamCreatedException, ScoreboardTeamNameTooLongException {
    for (ClearScoreboardTeam team : this.teams) {
      if (ChatColor.stripColor(team.getName()).equalsIgnoreCase(ChatColor.stripColor(name))) {
        throw new DuplicateTeamCreatedException(name);
      }
    }

    if (name.length() > 16) {
      throw new ScoreboardTeamNameTooLongException(name);
    }

    ClearScoreboardTeam team = new ClearScoreboardTeam(name, displayName, teamColor,this);
    team.refresh();
    this.teams.add(team);
    return team;
  }

  /**
   * Remove a team from the scoreboard
   * @param team The team to remove from the scoreboard
   */
  public void removeTeam(ClearScoreboardTeam team) {
    if (team.getScoreboard() != this) return;

    team.destroy();
    this.teams.remove(team);
  }

  /**
   * Destroy the scoreboard. This will reset all players to the server's main scoreboard, and clear all teams.
   * You should call this method inside of your plugin's onDisable method.
   */
  public void destroy() {
    for (UUID playerUUID : activePlayers) {
      Player player = Bukkit.getPlayer(playerUUID);

      if (player != null) {
        Validate.notNull(Bukkit.getScoreboardManager());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
      }
    }

    for (ClearScoreboardTeam team : teams) {
      team.destroy();
    }

    this.activePlayers.clear();
    this.teams.clear();
    this.previousLinesMap.clear();
  }

  /**
   * Get the teams registered on the Scoreboard
   * @return An unmodifiable view of the teams registered on the scoreboard
   */
  public List<ClearScoreboardTeam> getTeams() {
    return Collections.unmodifiableList(teams);
  }

  /**
   * Get the options for the Scoreboard.
   * If changed directly, updateScoreboard() must be called manually.
   * @return The options for the scoreboard
   */
  public ClearScoreboardOptions getOptions() {
    return options;
  }

  // MARK: Private API

  /**
   * Update a scoreboard with a list of lines
   * These lines must be in reverse order!
   * @throws ScoreboardLineTooLongException If a String within the lines array is over 64 characters, this exception is thrown.
   */
  protected void updateScoreboard(Scoreboard scoreboard, List<String> lines) throws ScoreboardLineTooLongException {
    Objective objective = objectiveWrapper.getDummyObjective(scoreboard);

    Validate.notNull(objective);

    String title = getTitle(scoreboard);
    if (title == null) {
      title = "";
    }

    objective.setDisplayName(color(title));

    if (lines == null) {
      lines = new ArrayList<>();
    }

    if (previousLinesMap.containsKey(scoreboard)) {
      if (previousLinesMap.get(scoreboard).equals(lines)) { // Are the lines the same? Don't take up server resources to change absolutely nothing
        updateTeams(scoreboard); // Update the teams anyway
        return;
      }

      // Size difference means unregister objective to reset and re-register teams correctly
      if (previousLinesMap.get(scoreboard).size() != lines.size()) {
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        scoreboard.getEntries().forEach(scoreboard::resetScores);
        scoreboard.getTeams().forEach(team -> {
          if (team.getName().contains("line")) {
            team.unregister();
          }
        });
      }
    }

    // This is a copy instead of reference to prevent previousLinesMap equality check from unexpectedly failing
    previousLinesMap.put(scoreboard, new ArrayList<>(lines));

    List<String> reversedLines = new ArrayList<>(lines);
    Collections.reverse(reversedLines);

    objective.setDisplaySlot(DisplaySlot.SIDEBAR);

    Objective healthObjective;

    if (options.getTabHealthStyle() != ClearScoreboardTabHealthStyle.NONE) {
      healthObjective = objectiveWrapper.getTabHealthObjective(options.getTabHealthStyle().toWrapped(), scoreboard);
      healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    } else {
      healthObjective = objectiveWrapper.getTabHealthObjective(options.getTabHealthStyle().toWrapped(), scoreboard);
      if (healthObjective != null) {
        healthObjective.unregister();
      }
    }

    if (options.shouldShowHealthUnderName()) {
      healthObjective = objectiveWrapper.getNameHealthObjective(scoreboard);
      healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    } else {
      healthObjective = objectiveWrapper.getNameHealthObjective(scoreboard);
      if (healthObjective != null) {
        healthObjective.unregister();
      }
    }

    List<String> colorCodeOptions = colorOptions(reversedLines.size());

    int score = 1;

    for (String entry : reversedLines) {
      if (entry.length() > maxLineLength) {
        throw new ScoreboardLineTooLongException(entry, maxLineLength);
      }

      entry = color(entry);

      Team team = scoreboard.getTeam("line" + score);

      String prefix;
      String suffix = "";

      int cutoff = SpigotAPIVersion.getCurrent().lessThan(SpigotAPIVersion.v1_13) ? 16 : 64;
      if (entry.length() <= cutoff) {
        prefix = entry;
      } else {
        prefix = entry.substring(0, cutoff);

        if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
          prefix = prefix.substring(0, prefix.length() - 1);
          suffix = ChatColor.COLOR_CHAR + suffix;
        }

        suffix = StringUtils.left(ChatColor.getLastColors(prefix) + suffix + entry.substring(cutoff), cutoff);
      }

      if (team != null) {
        team.getEntries().forEach(team::removeEntry);
        team.addEntry(colorCodeOptions.get(score));
      } else {
        team = scoreboard.registerNewTeam("line" + score);
        team.addEntry(colorCodeOptions.get(score));
        objective.getScore(colorCodeOptions.get(score)).setScore(score);
      }

      team.setPrefix(prefix);
      team.setSuffix(suffix);

      score += 1;
    }

    updateTeams(scoreboard);
  }

  /**
   * Update the teams on the scoreboard. Loops over all teams and calls refresh(Scoreboard)
   * @param scoreboard The Bukkit scoreboard to use
   */
  private void updateTeams(Scoreboard scoreboard) {
    this.teams.forEach(team -> team.refresh(scoreboard));
  }

  /**
   * Generate a list of unique color code combinations to use as scoreboard entries.
   * This is done to ensure that...
   * 1. Duplicate lines can be created
   * 2. The content of a scoreboard line is stored in the team prefix + suffix, rather than the entry itself
   * @param amountOfLines The amount of lines, and by proxy the amount of color combinations, to be generated
   * @return The list of unique color combinations
   */
  private List<String> colorOptions(int amountOfLines) {
    List<String> colorCodeOptions = new ArrayList<>();
    for (ChatColor color : ChatColor.values()) {
      if (color.isFormat()) {
        continue;
      }

      for (ChatColor secondColor : ChatColor.values()) {
        if (secondColor.isFormat()) {
          continue;
        }

        String option = color + "" + secondColor;

        if (color != secondColor && !colorCodeOptions.contains(option)) {
          colorCodeOptions.add(option);

          if (colorCodeOptions.size() == amountOfLines) break;
        }
      }
    }

    return colorCodeOptions;
  }

  protected List<UUID> getActivePlayers() {
    return activePlayers;
  }

  protected InternalObjectiveWrapper getObjectiveWrapper() {
    return objectiveWrapper;
  }

  protected InternalTeamWrapper getTeamWrapper() {
    return teamWrapper;
  }

  /**
   * Set the options of the scoreboard
   * @param options The options
   */
  protected void setOptions(ClearScoreboardOptions options) {
    this.options = options;
  }

  /**
   * Translate a String into a color formatted String. Uses & as the special color char.
   * @param string The string to translate
   * @return The formatted String
   */
  protected String color(String string) {
    return ChatColor.translateAlternateColorCodes('&', string);
  }

  /**
   * Get the title for a particular Scoreboard
   * @param scoreboard The Bukkit scoreboard that requires a title
   * @return The title String
   */
  protected abstract String getTitle(Scoreboard scoreboard);
}
