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


/**
 * This class implements the {@link java.sql.Struct} interface.
 * <p>
 * {@code StructImpl} provides a simple implementation of a struct data type.
 * </p>
 */
public class StructImpl implements Struct {
    private final String typeName;
    private final Object[] attributes;

    /**
     * Constructs a new {@code StructImpl} object with the specified parameter values.
     *
     * @param typeName the SQL type name of the struct
     * @param attributes the attributes of the struct, each attribute is a {@code Map.Entry<K, V>}(key-value pair)
     */
    public StructImpl(String typeName, Object[] attributes) {
        this.typeName = typeName;
        this.attributes = attributes;
    }

    /**
     * Returns the SQL type name of the struct.
     *
     * @return the SQL type name of the struct
     * @throws SQLException if a database access error occurs
     */
    @Override
    public String getSQLTypeName() throws SQLException {
        return this.typeName;
    }

    /**
     * Returns an array containing the attributes of the struct.
     *
     * @return an array containing the attribute values of the struct
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Object[] getAttributes() throws SQLException {
        return attributes;
    }

    /**
     * @throws java.lang.UnsupportedOperationException because functionality is not supported yet
     */
    @Override
    public Object[] getAttributes(Map<String,Class<?>> map) throws SQLException {
        throw new java.lang.UnsupportedOperationException("Not supported yet.");
    }

    /**
    * Compares this StructImpl object with the specified object for equality.
    *
    * <p>
    * Two StructImpl objects are considered equal if they have the same typeName, same number of attributes,
    * and contain the same attributes.
    * </p>
    *
    * @param obj the object to compare with this StructImpl object for equality.
    * @return {@code true} if the specified object is equal to this StructImpl object, {@code false} otherwise.
    */
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
            if (!typeName.equals(other.getSQLTypeName()) || attributes.length != other.getAttributes().length) { 
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
