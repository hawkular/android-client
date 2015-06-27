/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.client.android.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.hawkular.client.android.R;
import org.hawkular.client.android.adapter.MetricsAdapter;
import org.hawkular.client.android.backend.BackendClient;
import org.hawkular.client.android.backend.model.Environment;
import org.hawkular.client.android.backend.model.Metric;
import org.hawkular.client.android.backend.model.Resource;
import org.hawkular.client.android.backend.model.Tenant;
import org.hawkular.client.android.util.Fragments;
import org.hawkular.client.android.util.Intents;
import org.hawkular.client.android.util.ViewDirector;
import org.jboss.aerogear.android.pipe.callback.AbstractFragmentCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;
import icepick.Icicle;
import timber.log.Timber;

public final class MetricsFragment extends Fragment implements AdapterView.OnItemClickListener {
    @InjectView(R.id.list)
    ListView list;

    @Icicle
    @Nullable
    ArrayList<Metric> metrics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

        setUpState(state);

        setUpBindings();

        setUpList();

        setUpMetrics();
    }

    private void setUpState(Bundle state) {
        Icepick.restoreInstanceState(this, state);
    }

    private void setUpBindings() {
        ButterKnife.inject(this, getView());
    }

    private void setUpList() {
        list.setOnItemClickListener(this);
    }

    private void setUpMetrics() {
        if (metrics == null) {
            showProgress();

            BackendClient.of(this).getMetrics(getTenant(), getEnvironment(), getResource(), new MetricsCallback());
        } else {
            setUpMetrics(metrics);
        }
    }

    private void showProgress() {
        ViewDirector.of(this).using(R.id.animator).show(R.id.progress);
    }

    private Tenant getTenant() {
        return getArguments().getParcelable(Fragments.Arguments.TENANT);
    }

    private Environment getEnvironment() {
        return getArguments().getParcelable(Fragments.Arguments.ENVIRONMENT);
    }

    private Resource getResource() {
        return getArguments().getParcelable(Fragments.Arguments.RESOURCE);
    }

    private void setUpMetrics(List<Metric> metrics) {
        this.metrics = new ArrayList<>(metrics);

        sortMetrics(metrics);

        list.setAdapter(new MetricsAdapter(getActivity(), metrics));

        hideProgress();
    }

    private void sortMetrics(List<Metric> metrics) {
        Collections.sort(metrics, new MetricsComparator());
    }

    private void hideProgress() {
        ViewDirector.of(this).using(R.id.animator).show(R.id.list);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Metric metric = getMetricsAdapter().getItem(position);

        startMetricDataActivity(metric);
    }

    private MetricsAdapter getMetricsAdapter() {
        return (MetricsAdapter) list.getAdapter();
    }

    private void startMetricDataActivity(Metric metric) {
        Intent intent = Intents.Builder.of(getActivity()).buildMetricDataIntent(getTenant(), metric);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        tearDownState(state);
    }

    private void tearDownState(Bundle state) {
        Icepick.saveInstanceState(this, state);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        tearDownBindings();
    }

    private void tearDownBindings() {
        ButterKnife.reset(this);
    }

    private static final class MetricsCallback extends AbstractFragmentCallback<List<Metric>> {
        @Override
        public void onSuccess(List<Metric> metrics) {
            MetricsFragment activity = (MetricsFragment) getFragment();

            activity.setUpMetrics(metrics);
        }

        @Override
        public void onFailure(Exception e) {
            Timber.d(e, "Metrics fetching failed.");
        }
    }

    private static final class MetricsComparator implements Comparator<Metric> {
        @Override
        public int compare(Metric leftMetric, Metric rightMetric) {
            String leftMetricDescription = leftMetric.getProperties().getDescription();
            String rightMetricDescription = rightMetric.getProperties().getDescription();

            return leftMetricDescription.compareTo(rightMetricDescription);
        }
    }
}
