/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc.config;

import java.net.URI;

public class HostConnectionProperty extends StringConnectionProperty {
    public static final String KEY = "host";

    public HostConnectionProperty() {
        super(KEY);
    }

    @Override
    protected String parseValue(Object value) throws ConnectionPropertyException {
        String host = value.toString();
        // URI class extracts host from the string. It requires a prefix to be set to parse it properly.
        return URI.create(host.startsWith("http") ? host : "https://" + host).getHost();
    }

    public String getDefault() {
        return "localhost";
    }
}
