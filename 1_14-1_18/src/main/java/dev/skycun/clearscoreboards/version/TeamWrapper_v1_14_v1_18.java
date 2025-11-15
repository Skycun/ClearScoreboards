package dev.skycun.clearscoreboards.version;

import dev.skycun.clearscoreboards.abstraction.InternalTeamWrapper;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

public class TeamWrapper_v1_14_v1_18 extends InternalTeamWrapper {
  @Override
  public void setColor(Team team, ChatColor color) {
    team.setColor(color);
  }
}
