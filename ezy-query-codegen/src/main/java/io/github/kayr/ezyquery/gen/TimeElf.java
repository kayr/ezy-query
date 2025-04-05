package io.github.kayr.ezyquery.gen;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeElf {

  private TimeElf() {}

  private static Clock clock = Clock.systemDefaultZone();

  public static String nowStr() {
    return now().format(DateTimeFormatter.ISO_DATE_TIME);
  }

  public static LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  public static void setClock(Clock clock) {
    TimeElf.clock = clock;
  }
}
