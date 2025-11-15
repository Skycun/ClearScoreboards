package dev.skycun.clearscoreboards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClearGlobalMethodBasedScoreboard extends ClearGlobalScoreboard {
  private String title = "";
  private List<String> lines = new ArrayList<>();

  public ClearGlobalMethodBasedScoreboard(ClearScoreboardOptions options) {
    super(options);

    setTitleSupplier(() -> title);
    setLinesSupplier(() -> lines);
  }

  public ClearGlobalMethodBasedScoreboard() {
    this(ClearScoreboardOptions.defaultOptions);
  }

  public void setTitle(String title) {
    this.title = title;
    updateScoreboard();
  }

  public void setLines(List<String> lines) {
    this.lines = lines;
    updateScoreboard();
  }

  public void setLines(String... lines) {
    setLines(Arrays.asList(lines));
  }
}
