/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */


package org.opensearch.jdbc.config;

public class UsePreemptiveAuthProperty extends BoolConnectionProperty {
    public static final String KEY = "usePreemptiveAuth";

    public UsePreemptiveAuthProperty() {
        super(KEY);
    }

    @Override
    public Boolean getDefault() {
        return false;
    }
}
