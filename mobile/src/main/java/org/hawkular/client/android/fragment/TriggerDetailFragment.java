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
package org.hawkular.client.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.squareup.leakcanary.RefWatcher;

import org.hawkular.client.android.HawkularApplication;
import org.hawkular.client.android.R;
import org.hawkular.client.android.animation.MyBounceInterpolator;
import org.hawkular.client.android.backend.BackendClient;
import org.hawkular.client.android.backend.model.Trigger;
import org.hawkular.client.android.util.Fragments;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.generator.IdGenerator;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class TriggerDetailFragment extends android.support.v4.app.Fragment {
            @State
            @Nullable
            Trigger trigger;

    @BindView(R.id.favourite_button)
    ImageButton addButton;

    @BindView(R.id.trigger_delete)
    ImageButton deleteButton;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trigger_detail,container,false);
    }

    @OnClick(R.id.favourite_button)
    public void toggleTriggerFavouriteStatus(){
        Context context = getActivity();
        final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);


        addButton.startAnimation(myAnim);
        SQLStore<Trigger> store = openStore(context);

        if (store.read(trigger.getId()) == null) {
            store.save(trigger);
            Snackbar snackbar = Snackbar.make(getView(),R.string.favourites_added, Snackbar.LENGTH_SHORT);
            snackbar.show();
            addButton.setColorFilter(getResources().getColor(R.color.background_primary));
            addButton.startAnimation(myAnim);
        } else {
            store.remove(trigger.getId());
            Snackbar snackbar = Snackbar.make(getView(),R.string.favourites_removed, Snackbar.LENGTH_SHORT);
            snackbar.show();
            addButton.setColorFilter(getResources().getColor(R.color.background_secondary));
            addButton.startAnimation(myAnim);
        }

        store.close();
    }

    @OnClick(R.id.trigger_delete)
    public void trigger_delete(){
        Context context = getActivity();
        final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);

        deleteButton.startAnimation(myAnim);
        BackendClient.of(this).deleteTriggers(new DeleteTriggerCallback(this) ,trigger.getId());

    }


    private SQLStore<Trigger> openStore(Context context) {
        DataManager.config("FavouriteTriggers", SQLStoreConfiguration.class)
                .withContext(context)
                .withIdGenerator(new IdGenerator() {
                    @Override
                    public String generate() {
                        return UUID.randomUUID().toString();
                    }
                }).store(Trigger.class);
        return (SQLStore<Trigger>) DataManager.getStore("FavouriteTriggers");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detectLeaks();
    }

    private void detectLeaks() {
        RefWatcher refWatcher = HawkularApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }



    private static final class DeleteTriggerCallback implements Callback<Void>{

        private TriggerDetailFragment triggerDetailFragment;

        public DeleteTriggerCallback(TriggerDetailFragment triggerDetailFragment) {
            this.triggerDetailFragment = triggerDetailFragment;
        }
        public TriggerDetailFragment getTriggerDetailFragment(){
            return this.triggerDetailFragment;
        }

        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {

            Snackbar snackbar = Snackbar.make(getTriggerDetailFragment().getView(),R.string.trigger_deleted,Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            Timber.d("on Failure",t.getMessage());
        }
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ButterKnife.bind(this, getView());

        trigger = getArguments().getParcelable(Fragments.Arguments.TRIGGER);
        SQLStore<Trigger> store = openStore(getContext());
               store.openSync();

        if(store.read(trigger.getId()) == null) {
            addButton.setColorFilter(getResources().getColor(R.color.background_secondary));
        } else {
            addButton.setColorFilter(getResources().getColor(R.color.background_primary));
        }

        store.close();
    }
}
