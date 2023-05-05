/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc;

import java.sql.SQLException;
import java.sql.Struct;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class StructImpl implements Struct {
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Struct)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Struct other = (Struct) obj;
        try {
            if (!typeName.equals(other.getSQLTypeName())) {
                return false;
            }
            List otherAttributes = Arrays.asList(other.getAttributes());
            return otherAttributes.containsAll(Arrays.asList(attributes));
        }
        catch (SQLException e) {
            return false;
        }
    }
}
