/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


 package org.opensearch.jdbc.types;
 
 import static org.junit.jupiter.api.Assertions.*;
 import org.junit.jupiter.api.Test;
 import org.opensearch.jdbc.types.StructType;
 import org.opensearch.jdbc.StructImpl;

 import java.sql.Struct;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 
 public class StructTypeTests {

    @Test
    public void testStructTypeFromValue() throws SQLException {
        Map<String, Object> attributes = new HashMap<String, Object>() {{
            put("attribute1", "value1");
            put("attribute2", 2);
            put("attribute3", 15.0);
        }};

        Struct actualStruct = StructType.INSTANCE.fromValue(attributes, null);
        assertTrue(Arrays.equals(actualStruct.getAttributes(), attributes.entrySet().toArray()));
        assertEquals(actualStruct.getAttributes().length, 3);
        assertEquals(actualStruct, new StructImpl(StructType.INSTANCE.getTypeName(), attributes.entrySet().toArray()));

        Map<String, Object> nestedAttributes = new HashMap<String, Object>() {{
            put("struct", attributes);
            put("string", "hello");
            put("int", 1);
        }};

        Struct actualNestedStruct = StructType.INSTANCE.fromValue(nestedAttributes, null);
        assertTrue(Arrays.equals(actualNestedStruct.getAttributes(), nestedAttributes.entrySet().toArray()));
        assertEquals(actualNestedStruct, new StructImpl(StructType.INSTANCE.getTypeName(), nestedAttributes.entrySet().toArray()));
    }
 }
