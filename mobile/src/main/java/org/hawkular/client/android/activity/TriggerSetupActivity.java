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
package org.hawkular.client.android.activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hawkular.client.android.R;
import org.hawkular.client.android.backend.BackendClient;
import org.hawkular.client.android.backend.model.Error;
import org.hawkular.client.android.backend.model.FullTrigger;
import java.io.IOException;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.Icepick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by pallavi on 19/06/17.
 */


public class TriggerSetupActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.et_tenant_id)
    EditText id;

    @BindView(R.id.et_name)
    EditText textName;

    @BindView(R.id.typeSpinner)
    Spinner spinnerType;

    @BindView(R.id.eventTypeSpinner)
    Spinner spinnerEventType;

    @BindView(R.id.severitySpinner)
    Spinner severitySpinner;

    @BindView(R.id.switch_autoDisable)
    Switch switchAutoDisable;

    @BindView(R.id.switch_autoEnable)
    Switch switchAutoEnable;

    @BindView(R.id.switch_enabled)
    Switch switchEnabled;

    Snackbar snackbar;
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_setup_trigger);

        setUpBindings();

        setUpToolbar();

        setUpState(state);
    }

    private void setUpState(Bundle state) {
        Icepick.restoreInstanceState(this, state);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null) {
            return;
        }



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Trigger Setup");
    }

    private void setUpBindings() {
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_submit)
    void setUpTriggerValues(){

        FullTrigger trigger = null;

        if (textName.getText().toString()!=null) {
            trigger = new FullTrigger(id.getText().toString(),null,textName.getText().toString(),switchEnabled.isChecked(),severitySpinner.getSelectedItem().toString());
        }

        else{
            Toast.makeText(getApplicationContext(),"Trigger NAme cannot be empty",Toast.LENGTH_SHORT);
        }
        trigger.setAutoDisable(switchAutoDisable.isChecked());
        trigger.setType(spinnerType.getSelectedItem().toString());
        trigger.setEventType(spinnerEventType.getSelectedItem().toString());
        trigger.setAutoEnable(switchAutoEnable.isChecked());

        BackendClient.of(this).createTrigger(trigger,new TriggerCreateCallback());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private class TriggerCreateCallback implements Callback {

        @Override
        public void onResponse(Call call, Response response) {

            if (response.code() == 200) {
                finish();
            }
            else {
                Gson gson = new GsonBuilder().create();
                Error mApiError = null;
                try {
                    mApiError = gson.fromJson(response.errorBody().string(), Error.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Snackbar snackbar = Snackbar.make(getCurrentFocus(),mApiError.getErrorMsg(),Snackbar.LENGTH_LONG);
                snackbar.show();
            }

        }
        @Override
        public void onFailure(Call call, Throwable t) {
            Timber.d(t, "Triggers fetching failed.");
        }
    }


}

