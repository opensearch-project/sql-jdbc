/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.jdbc.config;

public class TunnelHostConnectionProperty extends StringConnectionProperty {
    public static final String KEY = "tunnelHost";

    public TunnelHostConnectionProperty() {
        super(KEY);
    }

    public String getDefault() {
        return "localhost";
    }

}
