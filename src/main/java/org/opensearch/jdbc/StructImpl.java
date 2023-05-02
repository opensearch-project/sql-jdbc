/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc;

import java.sql.SQLException;
import java.sql.Struct;
import java.util.Map;

public class StructImpl implements Struct{
    private final String typeName;
    private final Object[] attributes;

    public StructImpl(String typeName, Object[] attributes) {
        this.typeName = typeName;
        this.attributes = attributes;
    }

    public String getSQLTypeName() throws SQLException {
        return this.typeName;
    }

    public Object[] getAttributes() throws SQLException {
        return attributes;
    }

    public Object[] getAttributes(Map<String,Class<?>> map) throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported yet.");
    }
}
