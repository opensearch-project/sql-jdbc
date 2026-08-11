/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.jdbc.config;

public class AwsSessionTokenProperty extends StringConnectionProperty {

    public static final String KEY = "awsSessionToken";

    public AwsSessionTokenProperty() {
        super(KEY);
    }
}
