package dev.skycun.clearscoreboards;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ClearPerPlayerMethodBasedScoreboard extends ClearPerPlayerScoreboard {
  private final Map<UUID, String> playerToTitleMap = new ConcurrentHashMap<>();
  private final Map<UUID, List<String>> playerToLinesMap = new ConcurrentHashMap<>();

  public ClearPerPlayerMethodBasedScoreboard(ClearScoreboardOptions options) {
    super(options);

    setGenerateTitleFunction(this::getTitle);
    setGenerateLinesFunction(this::getLines);
  }

  public ClearPerPlayerMethodBasedScoreboard() {
    this(ClearScoreboardOptions.defaultOptions);
  }

  private String getTitle(Player player) {
    if (player == null) return "";
    return playerToTitleMap.getOrDefault(player.getUniqueId(), "");
  }

  public void setTitle(Player player, String title) {
    playerToTitleMap.put(player.getUniqueId(), title);
    updateScoreboard();
  }

  private List<String> getLines(Player player) {
    if (player == null) return Collections.emptyList();
    return playerToLinesMap.get(player.getUniqueId());
  }

  public void setLines(Player player, List<String> lines) {
    playerToLinesMap.put(player.getUniqueId(), lines);
    updateScoreboard();
  }

  public void setLines(Player player, String... lines) {
    playerToLinesMap.put(player.getUniqueId(), Arrays.asList(lines));
    updateScoreboard();
  }

  /**
   * Remove a player from the scoreboard and clean up associated data to prevent memory leaks.
   * @param player The player to remove
   */
  @Override
  public void removePlayer(Player player) {
    super.removePlayer(player);
    playerToTitleMap.remove(player.getUniqueId());
    playerToLinesMap.remove(player.getUniqueId());
  }

  /**
   * Destroy the scoreboard and clean up all associated data.
   */
  @Override
  public void destroy() {
    super.destroy();
    playerToTitleMap.clear();
    playerToLinesMap.clear();
  }
}
