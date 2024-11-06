/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


 package org.opensearch.jdbc;

 import java.sql.JDBCType;
 import java.sql.SQLException;
 import java.sql.SQLFeatureNotSupportedException;
 import java.sql.ResultSet;
 import java.sql.Struct;
 import java.sql.Array;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;


 /**
  * This class implements the {@link java.sql.Struct} interface.
  * <p>
  * {@code StructImpl} provides a simple implementation of a struct data type.
  * </p>
  */
 public class ArrayImpl implements Array {
    private ArrayList<Object> arrayData;
    private JDBCType baseTypeName;

    public ArrayImpl(ArrayList<Object> arrayData, JDBCType baseTypeName) {
        this.arrayData = arrayData;
        this.baseTypeName = baseTypeName;
    }

    @Override
    public String getBaseTypeName() throws SQLException {
        return this.baseTypeName.toString();
    }

    @Override
    public int getBaseType() throws SQLException {
        throw new SQLFeatureNotSupportedException("getBaseType() is not supported");
    }

    @Override
    public Object getArray() throws SQLException {
        return arrayData.toArray();
    }

    @Override
    public Object getArray(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getArray(map) is not supported");
    }

    @Override
    public Object getArray(long index, int count) throws SQLException {
        if (index < 1 || index > arrayData.size() || index + count - 1 > arrayData.size() || count <= 0) {
            throw new SQLException("Invalid index or count");
        }
        int fromIndex = (int) index - 1;
        int toIndex = fromIndex + count;
        return arrayData.subList(fromIndex, toIndex).toArray();
    }

    @Override
    public Object getArray(long index, int count, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getArray(index, count, map) is not supported");
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw new SQLFeatureNotSupportedException("getResultSet() is not supported");
    }

    @Override
    public ResultSet getResultSet(Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getResultSet(map) is not supported");
    }

    @Override
    public ResultSet getResultSet(long index, int count) throws SQLException {
        throw new SQLFeatureNotSupportedException("getResultSet(index, count) is not supported");
    }

    @Override
    public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SQLException {
        throw new SQLFeatureNotSupportedException("getResultSet(index, count, map) is not supported");
    }

    @Override
    public void free() throws SQLException {
        arrayData = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Array)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Array other = (Array) obj;
        try {
            Object[] myArray = (Object[]) this.getArray();
            Object[] otherArray = (Object[]) this.getArray();

            if (!(this.getBaseTypeName().equals(other.getBaseTypeName())) || myArray.length != otherArray.length) {
                return false;
            }
            return Arrays.equals(myArray, otherArray);
        }
        catch (SQLException e) {
            return false;
        }
    }
}
