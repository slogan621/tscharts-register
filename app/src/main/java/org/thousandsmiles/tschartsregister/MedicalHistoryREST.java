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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.MedicalHistory;
import org.thousandsmiles.tscharts_lib.RESTful;
import org.thousandsmiles.tscharts_lib.VolleySingleton;

import java.util.HashMap;
import java.util.Map;

public class MedicalHistoryREST extends RESTful {
    private final Object m_lock = new Object();

    private class ResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {

            synchronized (m_lock) {
                SessionSingleton sess = SessionSingleton.getInstance();
                setStatus(200);
                onSuccess(200, "");
                sess.setPatientMedicalHistory(response);
                m_lock.notify();
            }
        }
    }

    private class PostResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {

            synchronized (m_lock) {
                SessionSingleton sess = SessionSingleton.getInstance();
                try {
                    sess.setMedicalHistoryId(response.getInt("id"));
                    setStatus(200);
                    onSuccess(200, "");
                } catch (JSONException e) {
                    setStatus(500);
                    onFail(500, "");
                }
                m_lock.notify();
            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {

            synchronized (m_lock) {
                 if (error.networkResponse == null) {
                    if (error.getCause() instanceof java.net.ConnectException || error.getCause() instanceof  java.net.UnknownHostException) {
                        setStatus(101);
                        onFail(101, error.getMessage());
                    } else {
                        setStatus(-1);
                        onFail(-1, error.getMessage());
                    }
                } else {
                   setStatus(error.networkResponse.statusCode);
                }
                m_lock.notify();
            }
        }
    }

    private class PutResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {

            synchronized (m_lock) {
                setStatus(200);
                onSuccess(200, "");
                m_lock.notify();
            }
        }
    }

    public class AuthJSONObjectRequest extends JsonObjectRequest
    {
        public AuthJSONObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener listener, ErrorListener errorListener)
        {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        public Map getHeaders() throws AuthFailureError {
            Map headers = new HashMap();
            headers.put("Authorization", CommonSessionSingleton.getInstance().getToken());
            return headers;
        }
    }

    public class AuthJSONArrayRequest extends JsonArrayRequest {

        public AuthJSONArrayRequest(String url, JSONArray jsonRequest,
                                    Response.Listener<JSONArray> listener, ErrorListener errorListener) {
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

    public MedicalHistoryREST(Context context) {
        setContext(context);
    }

    public Object createMedicalHistory(MedicalHistory mh) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        JSONObject data = mh.toJSONObject(false);

        String url = String.format("http://%s:%s/tscharts/v1/medicalhistory/", getIP(), getPort());

        MedicalHistoryREST.AuthJSONObjectRequest request = new MedicalHistoryREST.AuthJSONObjectRequest(Request.Method.POST, url, data, new PostResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object getMedicalHistoryData(int historyid) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("http://%s:%s/tscharts/v1/medicalhistory/%d/", getIP(), getPort(), historyid);

        AuthJSONObjectRequest request = new AuthJSONObjectRequest(Request.Method.GET, url, null, new ResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object getMedicalHistoryData(int clinicid, int patientid) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("http://%s:%s/tscharts/v1/medicalhistory/?clinic=%d&patient=%d",
                                   getIP(), getPort(), clinicid, patientid);

        AuthJSONObjectRequest request = new AuthJSONObjectRequest(Request.Method.GET, url, null, new ResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object updateMedicalHistory(MedicalHistory mh) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        JSONObject data = mh.toJSONObject(true);

        String url = String.format("http://%s:%s/tscharts/v1/medicalhistory/%d/", getIP(), getPort(), mh.getId());

        AuthJSONObjectRequest request = new AuthJSONObjectRequest(Request.Method.PUT, url, data, new PutResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }
}
