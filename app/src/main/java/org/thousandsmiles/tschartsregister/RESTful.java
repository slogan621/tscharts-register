/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.tschartsregister;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public abstract class RESTful {
    private Context m_context;
    private int m_status;
    private ArrayList<RESTCompletionListener> m_listener = new ArrayList<RESTCompletionListener>();

    public void addListener(RESTCompletionListener o) {
        m_listener.add(o);
    }

    public void removeListener(RESTCompletionListener o) {
        m_listener.remove(o);
    }

    protected void onSuccess(int code, String message)
    {
        for (RESTCompletionListener x : m_listener) {
            x.onSuccess(code, message);
        }
    }

    protected void onFail(int code, String message)
    {
        for (RESTCompletionListener x : m_listener) {
            x.onFail(code, message);
        }
    }

    protected int getStatus() {
        return m_status;
    }

    protected void setStatus(int status) {
        m_status = status;
    }

    protected void setContext(Context context) {
        m_context = context;
    }

    protected Context getContext() {
        return m_context;
    }

    public String getPort() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String val = sharedPref.getString("port", "80");
        return val;
    }

    public String getIP() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(m_context);
        String val = sharedPref.getString("ipAddress", "");
        return val;
    }
}
