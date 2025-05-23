/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc;

import org.opensearch.jdbc.internal.exceptions.ObjectClosedException;
import org.opensearch.jdbc.logging.NoOpLogger;
import org.opensearch.jdbc.protocol.QueryResponse;
import org.opensearch.jdbc.protocol.http.JsonHttpProtocol;
import org.opensearch.jdbc.test.TestResources;
import org.opensearch.jdbc.test.mocks.MockOpenSearch;
import org.opensearch.jdbc.types.OpenSearchType;
import org.opensearch.jdbc.types.ArrayType;
import org.opensearch.jdbc.types.StructType;
import org.opensearch.jdbc.test.PerTestWireMockServerExtension;
import org.opensearch.jdbc.test.WireMockServerHelpers;
import org.opensearch.jdbc.test.mocks.MockResultSet;
import org.opensearch.jdbc.test.mocks.MockResultSetRows;
import org.opensearch.jdbc.test.mocks.MockResultSetMetaData;
import org.opensearch.jdbc.test.mocks.QueryMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(PerTestWireMockServerExtension.class)
public class ResultSetTests implements WireMockServerHelpers {

    @ParameterizedTest
    @MethodSource("queryMockProvider")
    void testQueryResultSet(QueryMock queryMock, WireMockServer mockServer) throws SQLException, IOException {
        queryMock.setupMockServerStub(mockServer);

        Connection con = new Driver().connect(getBaseURLForMockServer(mockServer), null);
        Statement st = con.createStatement();
        ResultSet rs = assertDoesNotThrow(() -> st.executeQuery(queryMock.getSql()));

        assertNotNull(rs);

        // prior to ResultSet iteration
        assertTrue(rs.isBeforeFirst(), "isBeforeFirst not True for non-empty ResultSet before the first next()");
        SQLException ex = assertThrows(SQLException.class, () -> rs.getObject(1));
        assertTrue(ex.getMessage().contains("Illegal operation before start of ResultSet"));

        // this will consume the resultSet
        queryMock.getMockResultSet().assertMatches(rs);

        // post ResultSet iteration
        assertTrue(rs.isAfterLast(), "isAfterLast not True after end of ResultSet.");
        assertFalse(rs.isBeforeFirst(), "isBeforeFirst True when isAfterLast is True.");
        ex = assertThrows(SQLException.class, () -> rs.getObject(1));
        assertTrue(ex.getMessage().contains("Illegal operation after end of ResultSet"));

        rs.close();

        // post ResultSet close
        assertTrue(rs.isClosed(), "ResultSet isClosed returns False after call to close it.");
        assertThrows(ObjectClosedException.class, rs::next);
        assertThrows(ObjectClosedException.class, rs::isAfterLast);
        assertThrows(ObjectClosedException.class, rs::isBeforeFirst);
        assertThrows(ObjectClosedException.class, () -> rs.getObject(1));

        st.close();
        con.close();
    }

    private static Stream<Arguments> queryMockProvider() {
        return Stream.of(
                Arguments.of(new QueryMock.NycTaxisQueryMock()),
                Arguments.of(new QueryMock.NycTaxisQueryWithAliasMock())
        );
    }



    @Test
    void testResultSetOnPaginatedResponse(WireMockServer mockServer) throws SQLException, IOException {

        String queryUrl = JsonHttpProtocol.DEFAULT_SQL_CONTEXT_PATH+"?format=jdbc";
        final String sql = "SELECT firstname, age FROM accounts LIMIT 12";

        // get Connection stub
        setupStubForConnect(mockServer, "/");

        // query response stub for initial page
        mockServer.stubFor(post(urlEqualTo(queryUrl))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.query", equalTo(sql)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResponseBodyFromPath("mock/protocol/json/cursor/queryresponse_accounts_00.json"))));

        // query response stub for second page
        mockServer.stubFor(post(urlEqualTo(queryUrl))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.cursor", equalTo("abcde_1")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResponseBodyFromPath("mock/protocol/json/cursor/queryresponse_accounts_01.json"))));

        // query response stub for third page
        mockServer.stubFor(post(urlEqualTo(queryUrl))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.cursor", equalTo("abcde_2")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResponseBodyFromPath("mock/protocol/json/cursor/queryresponse_accounts_02.json"))));

        // query response stub for last page
        mockServer.stubFor(post(urlEqualTo(queryUrl))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.cursor", equalTo("abcde_3")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(getResponseBodyFromPath("mock/protocol/json/cursor/queryresponse_accounts_03.json"))));


        Connection con = new Driver().connect(getBaseURLForMockServer(mockServer), null);
        Statement st = con.createStatement();
        st.setFetchSize(3);
        ResultSet rs = assertDoesNotThrow(() -> st.executeQuery(sql));
        int cursorRowCount = 0;

        while(rs.next()) {
            cursorRowCount++;
        }
        assertEquals(12, cursorRowCount, "Unexpected number of rows retrieved from cursor.");

        // test for execute method, mostly used by BI tools like Tableau for example.
        con = new Driver().connect(getBaseURLForMockServer(mockServer), null);
        Statement statement = con.createStatement();
        st.setFetchSize(3);
        boolean executed = assertDoesNotThrow(() -> statement.execute(sql));
        assertTrue(executed);
        rs = statement.getResultSet();
        cursorRowCount = 0;

        while(rs.next()) {
            cursorRowCount++;
        }
        assertEquals(12, cursorRowCount, "Unexpected number of rows retrieved from cursor.");
    }


    @Test
    void testNullableFieldsQuery(WireMockServer mockServer) throws SQLException, IOException {
        QueryMock.NullableFieldsQueryMock queryMock = new QueryMock.NullableFieldsQueryMock();

        queryMock.setupMockServerStub(mockServer);

        Connection con = new Driver().connect(getBaseURLForMockServer(mockServer), null);
        Statement st = con.createStatement();
        ResultSet rs = assertDoesNotThrow(() -> st.executeQuery(queryMock.getSql()));

        Map<String, Object> simpleAttributes = new HashMap<String, Object>() {{
                put("attribute1", "value1");
                put("attribute2", 2);
                put("attribute3", 15.0);
        }};

        Map<String, Object> simpleAttributes2 = new HashMap<String, Object>() {{
                put("attribute1", "value2");
                put("attribute2", 100);
                put("attribute3", 75.5);
        }};

        Map<String, Object> nestedAttributes = new HashMap<String, Object>() {{
              put("struct", simpleAttributes);
              put("string", "hello");
              put("int", 1);
          }};

        ArrayList<String> elements = new ArrayList<String>( Arrays.asList("item1", "item2", "item3") );
        ArrayList<Object> elementsComplex = new ArrayList<Object>( Arrays.asList(simpleAttributes, simpleAttributes2));


        assertNotNull(rs);

        MockResultSetMetaData mockResultSetMetaData = MockResultSetMetaData.builder()
                .column("testBoolean", OpenSearchType.BOOLEAN)
                .column("docId", OpenSearchType.TEXT)
                .column("testByte", OpenSearchType.BYTE)
                .column("testFloat", OpenSearchType.FLOAT)
                .column("testLong", OpenSearchType.LONG)
                .column("testShort", OpenSearchType.SHORT)
                .column("testHalfFloat", OpenSearchType.HALF_FLOAT)
                .column("testTimeStamp", OpenSearchType.TIMESTAMP)
                .column("testScaledFloat", OpenSearchType.SCALED_FLOAT)
                .column("testKeyword", OpenSearchType.KEYWORD)
                .column("testText", OpenSearchType.TEXT)
                .column("testDouble", OpenSearchType.DOUBLE)
                .column("testStruct", OpenSearchType.OBJECT)
                .column("testArray", OpenSearchType.ARRAY)
                .build();

        MockResultSetRows mockResultSetRows = MockResultSetRows.builder()
                .row()
                .column(false, true)
                .column("2", false)
                .column((byte) 0, true)
                .column((float) 22.145135459218345, false)
                .column((long) 0, true)
                .column((short) 0, true)
                .column((float) 24.324234543532153, false)
                .column(Timestamp.valueOf("2015-01-01 12:10:30"), false)
                .column((double) 24.324234543532153, false)
                .column("Test String", false)
                .column("document3", false)
                .column((double) 0, true)
                .column(StructType.INSTANCE.fromValue(simpleAttributes, null), false)
                .column(ArrayType.INSTANCE.fromValue(elements, null), false)
                .row()
                .column(true, false)
                .column("1", false)
                .column((byte) 126, false)
                .column((float) 0, true)
                .column((long) 32000320003200030L, false)
                .column((short) 29000, false)
                .column((float) 0, true)
                .column(null, true)
                .column((double) 0, true)
                .column(null, true)
                .column(null, true)
                .column((double) 22.312423148903218, false)
                .column(null, true)
                .column(null, true)
                .row()
                .column(true, false)
                .column("1", false)
                .column((byte) 126, false)
                .column((float) 0, true)
                .column((long) 32000320003200030L, false)
                .column((short) 29000, false)
                .column((float) 0, true)
                .column(null, true)
                .column((double) 0, true)
                .column(null, true)
                .column(null, true)
                .column((double) 22.312423148903218, false)
                .column(StructType.INSTANCE.fromValue(nestedAttributes, null), false)
                .column(ArrayType.INSTANCE.fromValue(elementsComplex, null), false)
                .build();

        MockResultSet mockResultSet = new MockResultSet(mockResultSetMetaData, mockResultSetRows);

        mockResultSet.assertMatches(rs);

        rs.close();
        con.close();
    }

    @Test
    void testResultSetWrapper() throws SQLException {
        ResultSetImpl rsImpl = new ResultSetImpl(mock(StatementImpl.class), mock(QueryResponse.class), NoOpLogger.INSTANCE);

        assertTrue(rsImpl.isWrapperFor(ResultSet.class),
                "ResultSet impl returns False for isWrapperFor(ResultSet.class)");

        ResultSet unwrapped = assertDoesNotThrow(() -> rsImpl.unwrap(ResultSet.class),
                "Unexpected exception when unwrapping ResultSet");

        assertNotNull(unwrapped, "Unwrapped ResultSet null");

        assertFalse(rsImpl.isWrapperFor(mock(ResultSet.class).getClass()),
                "ResultSet impl returns True for isWrapperFor(mockClass)");

        assertFalse(rsImpl.isWrapperFor(null),
                "ResultSet impl returns True for isWrapperFor(null)");

        SQLException ex = assertThrows(SQLException.class, () -> rsImpl.unwrap(mock(ResultSet.class).getClass()));
        assertTrue(ex.getMessage().contains("Unable to unwrap"));
    }


    public String getResponseBodyFromPath(String path) throws IOException {
        return TestResources.readResourceAsString(path);
    }

    public void setupStubForConnect(final WireMockServer mockServer, final String contextPath) {
        // get Connection stub
        mockServer.stubFor(get(urlEqualTo(contextPath))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(MockOpenSearch.INSTANCE.getConnectionResponse())));
    }

}
