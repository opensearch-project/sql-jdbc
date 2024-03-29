/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc.internal.util;

import org.opensearch.jdbc.config.ConnectionProperty;
import org.opensearch.jdbc.config.HostConnectionProperty;
import org.opensearch.jdbc.config.PathConnectionProperty;
import org.opensearch.jdbc.config.PortConnectionProperty;
import org.opensearch.jdbc.config.UseSSLConnectionProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.StringTokenizer;

public class UrlParser {

    public static final String URL_PREFIX = "jdbc:opensearch://";
    private static final int URL_PREFIX_LEN = URL_PREFIX.length();

    private static final String SCHEME_DELIM = "://";

    public static boolean isAcceptable(String url) {
        return url != null && url.startsWith(URL_PREFIX);
    }

    /**
     * Parses a JDBC url and returns the url's components as a set of
     * properties.
     *
     * URL format expected is:
     *
     * <code>[driver prefix]://[scheme]://[host:[port]]/[path][?[propertyKey=value]&]</code>
     *
     * scheme, host, port and path are extracted from the url and returned
     * against the same property keys as their corresponding
     * {@link ConnectionProperty}
     * instances.
     *
     * Properties specified in the url query string are returned against
     * the exact property key as is used in the url.
     *
     * If any property that is derived from the url format - such as scheme,
     * host, port etc is also specified in url query string, then the value in
     * the query string overrides the derived value.
     *
     *
     * @param inputUrl
     * @return scheme, host, port, path are returned using the same
     * property key as corresponding ConnectionProperty instance.
     */
    public static Properties parseProperties(final String inputUrl) throws URISyntaxException {

        // TODO - support percent encoding for URL reserved characters
        if (inputUrl == null || inputUrl.indexOf(URL_PREFIX) != 0) {
            throw new URISyntaxException(inputUrl,
                    String.format("URL does not begin with the mandatory prefix %s.", URL_PREFIX));
        }

        final String trimmedUrl = inputUrl.trim();
        int schemeDelimIdx = trimmedUrl.indexOf(SCHEME_DELIM, URL_PREFIX_LEN);

        URI uri = null;

        if (schemeDelimIdx != -1){
            // user provided a scheme
            uri = toURI(extractTargetUrl(trimmedUrl));
        } else if (URL_PREFIX_LEN < trimmedUrl.length()) {
            // no scheme provided, but URL has more than just URL_PREFIX,
            // so assume http:// scheme.
            uri = toURI("http://"+extractTargetUrl(trimmedUrl));
        }

        Properties props = new Properties();

        if (uri != null) {
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath();

            if (host != null)
                props.setProperty(HostConnectionProperty.KEY.toLowerCase(), host);

            if (port != -1)
                props.setProperty(PortConnectionProperty.KEY.toLowerCase(), Integer.toString(port));

            if (path != null && path.length() > 0)
                props.setProperty(PathConnectionProperty.KEY.toLowerCase(), path);

            if ("https".equalsIgnoreCase(scheme)) {
                props.setProperty(UseSSLConnectionProperty.KEY.toLowerCase(), "true");
            } else if ("http".equalsIgnoreCase(scheme)) {
                props.setProperty(UseSSLConnectionProperty.KEY.toLowerCase(), "false");
            } else {
                throw new URISyntaxException(inputUrl, "Invalid scheme:"+scheme+". Only http and https are supported.");
            }

            String query = uri.getRawQuery();
            if (query != null) {
                StringTokenizer tokenizer = new StringTokenizer(query, "&");
                while(tokenizer.hasMoreElements()) {
                    String kvp = tokenizer.nextToken();

                    String[] kv = kvp.split("=");

                    if (kv.length != 2)  {
                        throw new URISyntaxException(inputUrl,
                                "QueryString format of URL invalid. Found unexpected format at "
                                        + kv[0]
                                        + ". Expected key=value pairs");
                    } else {
                        props.setProperty(kv[0].toLowerCase(), kv[1]);
                    }
                }
            }
        }

        return props;
    }

    private static String extractTargetUrl(final String url) {
        return url.substring(URL_PREFIX_LEN);
    }

    private static URI toURI(final String str) throws URISyntaxException {
       return new URI(str);
    }
}
