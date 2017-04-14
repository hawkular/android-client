/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.client.android.util;

import org.hawkular.client.android.R;

import android.support.annotation.NonNull;

/**
 * Color schemer.
 *
 * Provides a default color scheme, especially useful
 * for {@link android.support.v4.widget.SwipeRefreshLayout} configuration.
 */
public final class ColorSchemer {
    private ColorSchemer() {
    }

    @NonNull
    public static int[] getScheme() {
        return new int[]{
            R.color.background_primary,
            R.color.background_secondary
        };
    }
}
