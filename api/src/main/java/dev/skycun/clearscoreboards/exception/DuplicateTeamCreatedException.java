package dev.skycun.clearscoreboards.exception;

public class DuplicateTeamCreatedException extends RuntimeException {
  public DuplicateTeamCreatedException(String name) {
    super("A team named " + name + " already exists on this scoreboard.");
  }
}
