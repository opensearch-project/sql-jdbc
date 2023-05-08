/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.jdbc.types;

import java.sql.Struct;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Collections;



import org.opensearch.jdbc.StructImpl;
 
public class StructType implements TypeHelper<Struct> {

    public static final StructType INSTANCE = new StructType();

    private StructType() {

    }

    @Override
    public String getTypeName() {
        return "Struct";
    }

    @Override
    public Struct fromValue(Object value, Map<String, Object> conversionParams) {
        if (value == null || !(value instanceof Map<?, ?>)) {
          return null;
        }
        Map<String, Object> structKeyValues = (Map<String, Object>) value;
        return new StructImpl(getTypeName(), structKeyValues.entrySet().toArray());
    }
 }
