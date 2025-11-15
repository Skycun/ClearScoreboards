package dev.skycun.clearscoreboards;

public class ClearScoreboardOptions {

  private ClearScoreboardTabHealthStyle tabHealthStyle;
  private boolean showHealthUnderName;

  public ClearScoreboardOptions(ClearScoreboardTabHealthStyle tabHealthStyle, boolean showHealthUnderName) {
    this.tabHealthStyle = tabHealthStyle;
    this.showHealthUnderName = showHealthUnderName;
  }

  public static ClearScoreboardOptions defaultOptions = new ClearScoreboardOptions(ClearScoreboardTabHealthStyle.NONE, false);

  public ClearScoreboardTabHealthStyle getTabHealthStyle() {
    return tabHealthStyle;
  }

  public boolean shouldShowHealthUnderName() {
    return showHealthUnderName;
  }

  /**
   * The scoreboard must be updated for this change to take effect.
   * @param showHealthUnderName
   */
  public void setShowHealthUnderName(boolean showHealthUnderName) {
    this.showHealthUnderName = showHealthUnderName;
  }

  /**
   * The scoreboard must be updated for this change to take effect.
   * @param tabHealthStyle
   */
  public void setTabHealthStyle(ClearScoreboardTabHealthStyle tabHealthStyle) {
    this.tabHealthStyle = tabHealthStyle;
  }
}
