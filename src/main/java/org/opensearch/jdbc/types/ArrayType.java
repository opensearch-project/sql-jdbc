/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.jdbc.types;

import java.sql.Array;
import java.sql.JDBCType;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Collections;


import org.opensearch.jdbc.ArrayImpl;
 
public class ArrayType implements TypeHelper<Array> {

    public static final ArrayType INSTANCE = new ArrayType();

    private ArrayType() {

    }

    @Override
    public String getTypeName() {
        return "Array";
    }

    @Override
    public Array fromValue(Object value, Map<String, Object> conversionParams) {
        if (value == null || !(value instanceof ArrayList)) {
          return null;
        }

        JDBCType baseType = conversionParams != null ? (JDBCType) conversionParams.get("baseType") : JDBCType.OTHER;

        return new ArrayImpl((ArrayList) value, baseType);
    }
 }