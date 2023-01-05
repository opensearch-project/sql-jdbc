/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc.types;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class TimeType implements TypeHelper<Time>{

  public static final TimeType INSTANCE = new TimeType();

  private TimeType() {

  }

  @Override
  public Time fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
    if (value == null) {
      return null;
    }
    Calendar calendar = conversionParams != null ? (Calendar) conversionParams.get("calendar") : null;
    if (value instanceof Time) {
      return (Time) value;
    } else if (value instanceof String) {
      return asTime((String) value, calendar);
    } else if (value instanceof Number) {
      return asTime((Number) value);
    } else {
      throw objectConversionException(value);
    }
  }

  public Time asTime(String value, Calendar calendar) {
    Time time;
    LocalDateTime localDateTime;

    try {
      TemporalAccessor temporal = DateTimeFormatter
              .ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
              .parse(value);

      localDateTime = LocalDateTime.from(temporal);
      time = Time.valueOf(localDateTime.toLocalTime());
    } catch (DateTimeParseException exception) {
      time = Time.valueOf(value);
    }

    if (calendar == null) {
      return time;
    }

    localDateTime = time.toLocalTime().atDate(LocalDate.now());

    return localDateTimeToTime(localDateTime, calendar);
  }

  public Time asTime(Number value) {
    return new Time(value.longValue());
  }

  @Override
  public String getTypeName() {
    return "Time";
  }

  private Time localDateTimeToTime(LocalDateTime ldt, Calendar calendar) {
    calendar.set(ldt.getYear(), ldt.getMonthValue()-1, ldt.getDayOfMonth(),
            ldt.getHour(), ldt.getMinute(), ldt.getSecond());
    calendar.set(Calendar.MILLISECOND, ldt.getNano()/1000000);

    return new Time(new Timestamp(calendar.getTimeInMillis()).getTime());
  }
}
