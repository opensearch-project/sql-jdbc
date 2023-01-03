/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.opensearch.jdbc.test.UTCTimeZoneTestExtension;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Calendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(UTCTimeZoneTestExtension.class)
public class TimeTypeTest {

  @ParameterizedTest
  @CsvSource(value = {
      "00:00:00, 00:00:00",
      "01:01:01, 01:01:01",
      "23:59:59, 23:59:59",
      "1880-12-22 00:00:00, 00:00:00",
      "2000-01-10 01:01:01, 01:01:01",
      "1998-08-17 23:59:59, 23:59:59"
  })
  void testTimeFromString(String inputString, String resultString) {
    Time time = Assertions.assertDoesNotThrow(
        () -> TimeType.INSTANCE.fromValue(inputString, null));
    assertEquals(resultString, time.toString());
  }

  @ParameterizedTest
  @CsvSource(value = {
      "00:00:00, 00:00:00",
      "01:01:01, 01:01:01",
      "23:59:59, 23:59:59",
      "1880-12-22 00:00:00, 00:00:00",
      "2000-01-10 01:01:01, 01:01:01",
      "1998-08-17 23:59:59, 23:59:59"
  })
  void testTimeFromStringWithCalendar(String inputString, String resultString) {
    Time time = Assertions.assertDoesNotThrow(
            () -> TimeType.INSTANCE.fromValue(
                    inputString, ImmutableMap.of("calendar", Calendar.getInstance())));
    assertEquals(resultString, time.toString());
  }

  @Test
  void testTimeFromTime() {
    Time timeNow = Time.valueOf(LocalTime.now());
    Time time = Assertions.assertDoesNotThrow(
            () -> TimeType.INSTANCE.fromValue(timeNow, null));
    assertEquals(timeNow, time);
  }

  @Test
  void testTimeFromTimeWithCalendar() {
    Time timeNow = Time.valueOf(LocalTime.now());
    Time time = Assertions.assertDoesNotThrow(
            () -> TimeType.INSTANCE.fromValue(
                    timeNow, ImmutableMap.of("calendar", Calendar.getInstance())));
    assertEquals(timeNow, time);
  }

  @Test
  void testTimeFromNumber() {
    long currentTimestamp = System.currentTimeMillis();
    Time time = Assertions.assertDoesNotThrow(
            () -> TimeType.INSTANCE.fromValue(currentTimestamp, null));
    assertEquals(currentTimestamp, time.getTime());
  }

  @Test
  void testTimeFromNumberWithCalendar() {
    long currentTimestamp = System.currentTimeMillis();
    Time time = Assertions.assertDoesNotThrow(
            () -> TimeType.INSTANCE.fromValue(
                    currentTimestamp, ImmutableMap.of("calendar", Calendar.getInstance())));
    assertEquals(currentTimestamp, time.getTime());
  }
}
