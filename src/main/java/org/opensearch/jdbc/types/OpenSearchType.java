/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc.types;

import java.sql.Array;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Enum for OpenSearch Data Types.
 * <p>
 * Each type encapsulates mapping to its corresponding JDBC Type and
 * associated properties.
 * <p>
 * Where required, an OpenSearch data type is mapped to the least
 * precise {@link JDBCType} that can accurately represent it based on
 * the following:
 *
 * <ol>
 * <li>
 * Precision of a {@link JDBCType} is taken to be the
 * precision of the corresponding Java type mentioned in
 * the JDBC Spec - Table B-1: JDBC Types Mapped to Java Types
 * </li>
 * <li>
 * Precision of OpenSearch types is based on
 * OpenSearch Reference > Mapping > Field datatypes
 * </li>
 * </ol>
 */
public enum OpenSearchType {

    // Precision values based on number of decimal digits supported by Java types
    // Display size values based on precision plus additional buffer for visual representation
    // - Java long is a 64-bit integral value ~ 19 decimal digits
    // - Java double has 53-bit precision ~ 15 decimal digits
    // - Java float has 24-bit precision ~ 7 decimal digits
    // - scaled_float is internally an OpenSearch long, but treated as Java Double here
    // - ISO8601 representation of DateTime values as yyyy-mm-ddThh:mm:ss.mmmZ ~ 24 chars

    // Some Types not fully supported yet: VARBINARY, GEO_POINT, NESTED
    BOOLEAN(JDBCType.BOOLEAN, Boolean.class, 1, 1, false),
    BYTE(JDBCType.TINYINT, Byte.class, 3, 5, true),
    SHORT(JDBCType.SMALLINT, Short.class, 5, 6, true),
    INTEGER(JDBCType.INTEGER, Integer.class, 10, 11, true),
    LONG(JDBCType.BIGINT, Long.class, 19, 20, true),
    HALF_FLOAT(JDBCType.REAL, Float.class, 7, 15, true),
    FLOAT(JDBCType.REAL, Float.class, 7, 15, true),
    DOUBLE(JDBCType.DOUBLE, Double.class, 15, 25, true),
    SCALED_FLOAT(JDBCType.DOUBLE, Double.class, 15, 25, true),
    KEYWORD(JDBCType.VARCHAR, String.class, 256, 0, false),
    TEXT(JDBCType.VARCHAR, String.class, Integer.MAX_VALUE, 0, false),
    STRING(JDBCType.VARCHAR, String.class, Integer.MAX_VALUE, 0, false),
    IP(JDBCType.VARCHAR, String.class, 15, 0, false),
    NESTED(JDBCType.STRUCT, null, 0, 0, false),
    OBJECT(JDBCType.STRUCT, Struct.class, 0, 0, false),
    DATE(JDBCType.DATE, Date.class, 10, 10, false),
    TIME(JDBCType.TIME, Time.class, 8, 8, false),
    DATETIME(JDBCType.TIMESTAMP, Timestamp.class, 29, 29, false),
    TIMESTAMP(JDBCType.TIMESTAMP, Timestamp.class, 29, 29, false),
    BINARY(JDBCType.VARBINARY, String.class, Integer.MAX_VALUE, 0, false),
    NULL(JDBCType.NULL, null, 0, 0, false),
    UNDEFINED(JDBCType.NULL, null, 0, 0, false),
    UNSUPPORTED(JDBCType.OTHER, null, 0, 0, false),
    ARRAY(JDBCType.ARRAY, Array.class, 0, 0, false);


    private static final Map<JDBCType, OpenSearchType> jdbcTypeToOpenSearchTypeMap;

    static {
        // Map JDBCType to corresponding ElasticsearchType
        jdbcTypeToOpenSearchTypeMap = new HashMap<>();
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.NULL, UNDEFINED);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.BOOLEAN, BOOLEAN);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.TINYINT, BYTE);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.SMALLINT, SHORT);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.INTEGER, INTEGER);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.BIGINT, LONG);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.DOUBLE, DOUBLE);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.REAL, FLOAT);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.FLOAT, DOUBLE);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.VARCHAR, KEYWORD);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.TIMESTAMP, TIMESTAMP);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.TIME, TIME);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.DATE, DATE);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.VARBINARY, BINARY);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.STRUCT, OBJECT);
        jdbcTypeToOpenSearchTypeMap.put(JDBCType.ARRAY, ARRAY);
    }

    /**
     * OpenSearch designated type name
     */
    private final String typeName;

    /**
     * {@link JDBCType} that this type maps to
     */
    private final JDBCType jdbcType;

    /**
     * Java class that the type maps to
     */
    private final String javaClassName;

    /**
     * Maximum number of characters that may be needed to represent the
     * values contained by this type. The value is determined based on
     * description provided in {@link java.sql.ResultSetMetaData#getPrecision(int)}.
     *
     * <ul>
     * <li>
     * For numeric types the value indicates the max number of
     * decimal digits that are possible with the corresponding
     * Java type
     * </li>
     * <li>
     * For character types the value indicates the length of
     * potential character data in the type
     * </li>
     * <li>
     * Fot Date-Time types the value indicates the characters
     * needed to represent the value in zero offset (UTC) ISO8601
     * format with millisecond fractional time i.e.
     * yyyy-mm-ddThh:mm:ss.mmmZ
     * </li>
     *
     * </ul>
     */
    private final int precision;

    /**
     * Display Size as per {@link java.sql.ResultSetMetaData#getColumnDisplaySize(int)}
     */
    private final int displaySize;

    /**
     * True if the type holds signed numerical values
     */
    private final boolean isSigned;

    OpenSearchType(JDBCType jdbcType, Class<?> javaClass, int precision,
                   int displaySize, boolean isSigned) {
        this.typeName = name().toLowerCase(Locale.ROOT);
        this.jdbcType = jdbcType;
        this.javaClassName = javaClass == null ? null : javaClass.getName();
        this.precision = precision;
        this.displaySize = displaySize;
        this.isSigned = isSigned;
    }

    public static OpenSearchType fromJdbcType(JDBCType jdbcType) {
        if (!jdbcTypeToOpenSearchTypeMap.containsKey(jdbcType)) {
            throw new IllegalArgumentException("Unsupported JDBC type \"" + jdbcType + "\"");
        }
        return jdbcTypeToOpenSearchTypeMap.get(jdbcType);
    }

    /**
     * Returns the {@link OpenSearchType} for the specified OpenSearch
     * data type name.
     */
    public static OpenSearchType fromTypeName(String typeName) {
        return fromTypeName(typeName, false);
    }

    /**
     * Parses a specified OpenSearch type name to determine
     * the corresponding {@link OpenSearchType}
     *
     * @param typeName The OpenSearch Type name to parse
     * @param errorOnUnknownType If true, the method throws an
     *         {@link UnrecognizedOpenSearchTypeException}
     *         if the type name specified is not recognized.
     *
     * @return the {@link OpenSearchType} value corresponding to the
     *         specified type name
     */
    public static OpenSearchType fromTypeName(String typeName, boolean errorOnUnknownType) {
        try {
            return OpenSearchType.valueOf(typeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException iae) {
            if (errorOnUnknownType)
                throw new UnrecognizedOpenSearchTypeException("Unknown Type: \"" + typeName + "\"", iae);
            else
                return UNSUPPORTED;
        }
    }

    public int sqlTypeNumber() {
        return jdbcType.getVendorTypeNumber();
    }

    public boolean isSigned() {
        return isSigned;
    }

    public String getTypeName() {
        return typeName;
    }

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public int getPrecision() {
        return precision;
    }

    public int getDisplaySize() {
        return displaySize;
    }
}
