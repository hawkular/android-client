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

import java.util.Random;
import java.util.UUID;

import android.support.annotation.NonNull;

public final class Randomizer {
    private Randomizer() {
    }

    private static final class Limits {
        private Limits() {
        }

        public static final int NUMBER = 42;
    }

    public static long generateNumber() {
        return new Random().nextInt(Limits.NUMBER);
    }

    @NonNull
    public static String generateString() {
        return UUID.randomUUID().toString();
    }

    public static Boolean generateBoolean() {
        int num = new Random().nextInt(Limits.NUMBER);

        if (num<21) {
            return true;
        } else {
            return false;
        }
    }
}
