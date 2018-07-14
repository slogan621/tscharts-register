/*
 * (C) Copyright Syd Logan 2018
 * (C) Copyright Thousand Smiles Foundation 2018
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.RESTful;
import org.thousandsmiles.tscharts_lib.VolleySingleton;

import java.util.HashMap;
import java.util.Map;

public class RegisterREST extends RESTful {
    private final Object m_lock = new Object();

    private class GetArrayResponseListener implements Response.Listener<JSONArray> {

        @Override
        public void onResponse(JSONArray response) {

            synchronized (m_lock) {
                SessionSingleton sess = SessionSingleton.getInstance();
                setStatus(200);
                onSuccess(200, "");
                sess.setRegistrationSearchResults(response);
                m_lock.notify();
            }
        }
    }

    private class RegisterResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            synchronized (m_lock) {
                setStatus(200);
                SessionSingleton sess = SessionSingleton.getInstance();
                sess.setRegistrationId(response);
                onSuccess(200, "");
                m_lock.notify();
            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            synchronized (m_lock) {
                int code;
                if (error.networkResponse == null) {
                    if (error.getCause() instanceof java.net.ConnectException || error.getCause() instanceof  java.net.UnknownHostException) {
                        code = 101;
                    } else {
                        code = -1;
                    }
                } else {
                    code = error.networkResponse.statusCode;
                }
                setStatus(code);
                onFail(code, "");
                m_lock.notify();
            }
        }
    }

    public RegisterREST(Context context)  {
        setContext(context);
    }

    public class AuthJSONArrayRequest extends JsonArrayRequest {

        public AuthJSONArrayRequest(String url, JSONArray jsonRequest,
                                    Response.Listener<JSONArray> listener, RegisterREST.ErrorListener errorListener) {
            super(url, listener, errorListener);
        }

        public AuthJSONArrayRequest(String url, Response.Listener<JSONArray> listener,
                                    Response.ErrorListener errorListener, String username, String password) {
            super(url, listener, errorListener);

        }

        private Map<String, String> headers = new HashMap<String, String>();

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            //return headers;
            Map headers = new HashMap();
            headers.put("Authorization", CommonSessionSingleton.getInstance().getToken());
            return headers;
        }

    }

    public class AuthJSONObjectRequest extends JsonObjectRequest {
        public AuthJSONObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener listener, RegisterREST.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        public Map getHeaders() throws AuthFailureError {
            Map headers = new HashMap();
            headers.put("Authorization", CommonSessionSingleton.getInstance().getToken());
            return headers;
        }
    }

    public Object register(int patient, int clinic) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("%s://%s:%s/tscharts/v1/register/", getProtocol(), getIP(), getPort());

        JSONObject data = new JSONObject();

        try {
            data.put("clinic", clinic);
            data.put("patient", patient);
        } catch(Exception e) {
            // not sure this would ever happen, ignore. Continue on with the request with the expectation it fails
            // because of the bad JSON sent
        }

        AuthJSONObjectRequest request = new AuthJSONObjectRequest(Request.Method.POST, url, data, new RegisterResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object findRegistrations(int clinic, int patient) {

        SessionSingleton.getInstance().clearRegistrationSearchResultData();
        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("%s://%s:%s/tscharts/v1/register?clinic=%d&patient=%d", getProtocol(), getIP(), getPort(),
                clinic, patient);

        AuthJSONArrayRequest request = new AuthJSONArrayRequest(url, null, new GetArrayResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

        return m_lock;
    }
}
