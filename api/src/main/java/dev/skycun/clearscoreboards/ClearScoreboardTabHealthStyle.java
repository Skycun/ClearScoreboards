package dev.skycun.clearscoreboards;

import dev.skycun.clearscoreboards.abstraction.WrappedHealthStyle;

public enum ClearScoreboardTabHealthStyle {
  NONE,
  HEARTS,
  NUMBER;

  public WrappedHealthStyle toWrapped() {
    switch (this) {
      case HEARTS: return WrappedHealthStyle.HEARTS;
      case NONE: return WrappedHealthStyle.NONE;
      case NUMBER: return WrappedHealthStyle.NUMBER;
    }

    return WrappedHealthStyle.NONE;
  }
}
