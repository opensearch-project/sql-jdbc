/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc.test;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NetworkTrafficServerConnector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.opensearch.jdbc.internal.util.UrlParser;
import org.opensearch.jdbc.test.mocks.MockOpenSearch;

import java.nio.ByteBuffer;

public class TLSServer {

    public static final String TRUST_SERVER_JKS_RESOURCE = "mock/jks/truststore_with_server_cert.jks";
    public static final String TRUST_CLIENT_JKS_RESOURCE = "mock/jks/truststore_with_client_cert.jks";

    public static final String SERVER_KEY_JKS_RESOURCE = "mock/jks/keystore_with_server_key.jks";
    public static final String SERVER_KEY_JKS_RESOURCE_NON_LOCALHOST = "mock/jks/keystore_with_non_localhost_server_key.jks";
    public static final String CLIENT_KEY_JKS_RESOURCE = "mock/jks/keystore_with_client_key.jks";

    public static Server startSecureServer(
            String host,
            String keyStorePath,
            String keyStorePassword,
            String keyStoreType,
            Handler handler) throws Exception {

        return startSecureServer(
                host,
                keyStorePath,
                keyStorePassword,
                keyStoreType,
                null,
                null,
                null,
                false,
                handler
        );
    }

    public static Server startSecureServer(
            String host,
            String keyStorePath,
            String keyStorePassword,
            String keyStoreType,
            String trustStorePath,
            String trustStorePassword,
            String trustStoreType,
            boolean needClientAuth,
            Handler handler) throws Exception {
        Server jettyServer = new Server();
        jettyServer.setStopTimeout(0);

        ServerConnector httpsConnector = null;

        // setup ssl
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyStoreType(keyStoreType);

        if (trustStorePath != null) {
            sslContextFactory.setTrustStorePath(trustStorePath);
            sslContextFactory.setTrustStorePassword(trustStorePassword);
            sslContextFactory.setTrustStoreType(trustStoreType);
        }
        sslContextFactory.setNeedClientAuth(needClientAuth);

        HttpConfiguration httpConfig = new HttpConfiguration();
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setSniHostCheck(false);
        httpConfig.addCustomizer(src);

        httpsConnector = createServerConnector(
                jettyServer,
                host,
                0,
                new SslConnectionFactory(
                        sslContextFactory,
                        "http/1.1"
                ),
                new HttpConnectionFactory(httpConfig)
        );

        jettyServer.addConnector(httpsConnector);
        jettyServer.setHandler(handler);
        jettyServer.start();

        return jettyServer;
    }

    public static class MockOpenSearchConnectionHandler extends AbstractHandler {
        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            response.getHeaders().add("Content-Type", "application/json");
            response.setStatus(200);

            // Write the response content
            byte[] content = MockOpenSearch.INSTANCE.getConnectionResponse().getBytes();
            response.write(true, ByteBuffer.wrap(content), callback);

            return true;
        }
    }

    private static ServerConnector createServerConnector(
            Server jettyServer,
            String bindAddress,
            int port,
            ConnectionFactory... connectionFactories) {
        NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(
                jettyServer,
                null,
                null,
                null,
                2,
                2,
                connectionFactories
        );
        connector.setPort(port);
        connector.setHost(bindAddress);

        return connector;
    }

    public static String getBaseURLForConnect(Server jettyServer) {
        int port = -1;
        String host = null;

        for (Connector c : jettyServer.getConnectors()) {
            if (c instanceof ServerConnector) {
                port = ((ServerConnector) c).getLocalPort();
                host = ((ServerConnector) c).getHost();
            }
        }

        return UrlParser.URL_PREFIX + "https://" + host + ":" + port;
    }
}
