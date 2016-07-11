/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.client.android.auth;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

import org.hawkular.client.android.backend.model.Token;
import org.jboss.aerogear.android.authorization.AuthzModule;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.pipe.http.HeaderAndBody;
import org.jboss.aerogear.android.pipe.http.HttpException;
import org.jboss.aerogear.android.pipe.http.HttpProvider;
import org.jboss.aerogear.android.pipe.http.HttpRestProvider;
import org.jboss.aerogear.android.pipe.module.AuthorizationFields;
import org.jboss.aerogear.android.pipe.module.ModuleFields;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.generator.IdGenerator;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import timber.log.Timber;

/**
 * SecretStoreAuthzModule.
 * <p/>
 * Performs all related work of generating new key and secret pair, Storing them as account,
 * Retrieving stored account, Deleting stored account.
 */

public class SecretStoreAuthzModule implements AuthzModule {
    private final Context context;
    private final SQLStore<Session> sessionStore;
    private final SQLStore<Token> tokenStore;

    public SecretStoreAuthzModule(Context context) {
        this.context = context.getApplicationContext();
        this.sessionStore = openSessionStore();
        sessionStore.openSync();
        this.tokenStore = openTokenStore();
        tokenStore.openSync();
    }

    private SQLStore<Session> openSessionStore() {
        DataManager.config(AuthData.STORE, SQLStoreConfiguration.class)
                .withContext(context)
                .withIdGenerator(new IdGenerator() {
                    @Override
                    public String generate() {
                        return UUID.randomUUID().toString();
                    }
                }).store(Session.class);
        return (SQLStore<Session>) DataManager.getStore(AuthData.STORE);
    }

    private SQLStore<Token> openTokenStore() {
        DataManager.config("store", SQLStoreConfiguration.class)
                .withContext(context)
                .withIdGenerator(new IdGenerator() {
                    @Override
                    public String generate() {
                        return UUID.randomUUID().toString();
                    }
                }).store(Token.class);
        return (SQLStore<Token>) DataManager.getStore("store");
    }

    @Override
    public boolean isAuthorized() {
        return true;
    }

    @Override
    public boolean hasCredentials() {
        return sessionStore.read(AuthData.NAME) != null;
    }

    @Override
    public void requestAccess(final Activity activity, final Callback<String> callback) {
        if (activity != null && activity.getIntent() != null) {
            Intent intent = activity.getIntent();
            if (intent.getStringExtra(AuthData.Credentials.CONTAIN) == null) {
                Session session = sessionStore.read(AuthData.NAME);
                callback.onSuccess(session.getUsername() + ":" + session.getPassword());
            } else if (intent.getStringExtra(AuthData.Credentials.CONTAIN).equals("true")) {
                doRequestAccess(activity, callback);
            }
        }
    }

    private void doRequestAccess(final Activity activity, final Callback<String> callback) {
        Intent intent = activity.getIntent();
        final String username = intent.getStringExtra(AuthData.Credentials.USERNAME);
        final String password = intent.getStringExtra(AuthData.Credentials.PASSWORD);
        final String url = intent.getStringExtra(AuthData.Credentials.URL);

        AsyncTask<Void, Void, HeaderAndBody> task = new AsyncTask<Void, Void, HeaderAndBody>() {
            private Exception exception;

            @Override
            protected HeaderAndBody doInBackground(Void... params) {
                HeaderAndBody result = null;
                try {
                    HttpProvider provider =
                            new HttpRestProvider(new URL(url.toString() + AuthData.Endpoints.ACCESS));
                    provider.setDefaultHeader("Accept", "application/json");
                    provider.setDefaultHeader("Content-Type", "application/json");
                    provider.setDefaultHeader("Authorization", "Basic "
                            + buildLoginData(username, password));
                    result = provider.get();
                    addAccount(username, password);
                } catch (MalformedURLException e) {
                    Timber.d(SecretStoreAuthzModule.class.getSimpleName(), "Error with URL", e);
                    exception = e;
                } catch (HttpException e) {
                    Timber.d(SecretStoreAuthzModule.class.getSimpleName(), "HTTP error", e);
                    exception = e;
                } catch (RuntimeException e) {
                    Timber.d(SecretStoreAuthzModule.class.getSimpleName(), "Connection Exception", e);
                    exception = e;
                }
                return result;
            }

            @Override
            protected void onPostExecute(HeaderAndBody headerAndBody) {
                if (exception == null) {
                    callback.onSuccess(new String(headerAndBody.getBody()));
                } else {
                    callback.onFailure(exception);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    private String buildLoginData(String username, String password) {
        String cred = username + ":" + password;
        return new String(Base64.encode(cred.getBytes(), Base64.NO_WRAP));
    }

    @Override
    public boolean refreshAccess() {
        return true;
    }

    @Override
    public void deleteAccount() {
        sessionStore.remove(AuthData.NAME);
        tokenStore.reset();
    }

    @Override
    public AuthorizationFields getAuthorizationFields(URI requestUri, String method, byte[] requestBody) {
        AuthorizationFields fields = new AuthorizationFields();
        Session storedAccount = sessionStore.read(AuthData.NAME);
        String cred = new String(
                Base64.encode((storedAccount.getUsername() + ":" +
                        storedAccount.getPassword()).getBytes(), Base64.NO_WRAP));
        fields.addHeader("Authorization", "Basic " + cred);
        return fields;
    }

    @Override
    public ModuleFields loadModule(URI relativeURI, String httpMethod, byte[] requestBody) {
        AuthorizationFields authzFields = getAuthorizationFields(relativeURI, httpMethod, requestBody);
        ModuleFields moduleFields = new ModuleFields();
        moduleFields.setHeaders(authzFields.getHeaders());
        moduleFields.setQueryParameters(authzFields.getQueryParameters());
        return moduleFields;
    }

    @Override
    public boolean handleError(HttpException exception) {
        return false;
    }

    private void addAccount(String username, String password){
        Session session = new Session();
        session.setAccountId(AuthData.NAME);
        session.setUsername(username);
        session.setPassword(password);

        sessionStore.save(session);
    }
}