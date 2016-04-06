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
package org.hawkular.client.android.adapter;

import java.util.List;

import org.hawkular.client.android.R;
import org.hawkular.client.android.backend.model.Token;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Tokens adapter.
 * <p/>
 * Transforms a list of Tokens to a human-readable interpretation.
 */

public class TokensAdapter extends BindableAdapter<Token> {
    private final List<Token> tokens;

    public TokensAdapter(@NonNull Context context, @NonNull List<Token> tokens) {
        super(context);
        this.tokens = tokens;
    }

    @Override
    public int getCount() {
        return tokens.size();
    }

    @NonNull
    @Override
    public Token getItem(int position) {
        return tokens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    protected View newView(LayoutInflater inflater, ViewGroup viewContainer) {
        View view = inflater.inflate(R.layout.layout_list_item, viewContainer, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    protected void bindView(Token token, int position, View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.nameText.setText(token.getPersona());
    }

    static final class ViewHolder {
        @Bind(R.id.text)
        public TextView nameText;

        public ViewHolder(@NonNull View view) {
            ButterKnife.bind(this, view);
        }
    }
}