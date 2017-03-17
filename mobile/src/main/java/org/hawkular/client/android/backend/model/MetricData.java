/**
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
package org.hawkular.client.android.backend.model;

import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

public final class MetricData implements Parcelable {
    @SerializedName("value")
    private String value;

    @SerializedName("timestamp")
    private long timestamp;

    @VisibleForTesting
    public MetricData(@NonNull String value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static Creator<MetricData> CREATOR = new Creator<MetricData>() {
        @Override
        public MetricData createFromParcel(Parcel parcel) {
            return new MetricData(parcel);
        }

        @Override
        public MetricData[] newArray(int size) {
            return new MetricData[size];
        }
    };

    private MetricData(Parcel parcel) {
        this.value = parcel.readString();
        this.timestamp = parcel.readLong();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(value);
        parcel.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
