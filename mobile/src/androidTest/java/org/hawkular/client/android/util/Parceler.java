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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public final class Parceler {
    private Parceler() {
    }

    private static final class ParcelFlags {
        private ParcelFlags() {
        }

        public static final int NONE = 0;
    }

    private static final class ParcelPositions {
        private ParcelPositions() {
        }

        public static final int START = 0;
    }

    @NonNull
    public  static <T extends Parcelable> T parcel(@NonNull Parcelable.Creator<T> parcelableCreator,
                                                   @NonNull T parcelable) {
        Parcel parcel = Parcel.obtain();

        parcelable.writeToParcel(parcel, ParcelFlags.NONE);

        parcel.setDataPosition(ParcelPositions.START);

        return parcelableCreator.createFromParcel(parcel);
    }
}
