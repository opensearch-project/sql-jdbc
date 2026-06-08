/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.jdbc.config;

public class AwsAccessKeyProperty extends StringConnectionProperty {

    public static final String KEY = "awsAccessKey";

    public AwsAccessKeyProperty() {
        super(KEY);
    }
}
