/*
 * (C) Copyright Syd Logan 2017-2018
 * (C) Copyright Thousand Smiles Foundation 2017-2018
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
import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.RESTful;
import org.thousandsmiles.tscharts_lib.VolleySingleton;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PatientREST extends RESTful {
    private final Object m_lock = new Object();

    private class ResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {
            synchronized (m_lock) {
                SessionSingleton sess = SessionSingleton.getInstance();
                setStatus(200);
                onSuccess(200, "");
                sess.addPatientData(response);
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
                    int id = response.getInt("id");
                    sess.setPatientId(id);
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

    private class ArrayResponseListener implements Response.Listener<JSONArray> {

        @Override
        public void onResponse(JSONArray response) {

            synchronized (m_lock) {
                SessionSingleton sess = SessionSingleton.getInstance();
                setStatus(200);
                onSuccess(200, "");
                sess.setPatientSearchResults(response);
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

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {

            synchronized (m_lock) {
                if (error.networkResponse == null) {
                    if (error.getCause() instanceof java.net.ConnectException || error.getCause() instanceof java.net.UnknownHostException) {
                        setStatus(101);
                        onFail(101, error.getMessage());
                    } else {
                        setStatus(-1);
                        onFail(-1, error.getMessage());
                    }
                } else {
                    setStatus(error.networkResponse.statusCode);
                    onFail(error.networkResponse.statusCode, error.networkResponse.toString());
                }
                m_lock.notify();
            }
        }
    }

    public class AuthJSONObjectRequest extends JsonObjectRequest {
        public AuthJSONObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener listener, ErrorListener errorListener) {
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

    public PatientREST(Context context) {
        setContext(context);
    }

    public Object getAllPatientData() {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("%s://%s:%s/tscharts/v1/patient/", getProtocol(), getIP(), getPort());

        AuthJSONArrayRequest request = new AuthJSONArrayRequest(url, null, new ArrayResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

        return m_lock;
    }

    public Object createPatient(PatientData pd) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        JSONObject data = pd.toJSONObject();

        String url = String.format("%s://%s:%s/tscharts/v1/patient/", getProtocol(), getIP(), getPort());

        AuthJSONObjectRequest request = new AuthJSONObjectRequest(Request.Method.POST, url, data, new PostResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object getPatientData(int patientid) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("%s://%s:%s/tscharts/v1/patient/%d/", getProtocol(), getIP(), getPort(), patientid);

        AuthJSONObjectRequest request = new AuthJSONObjectRequest(Request.Method.GET, url, null, new ResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }

    public Object findPatientsByDOB(Date d) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String url = String.format("%s://%s:%s/tscharts/v1/patient?dob=%s/%s/%s", getProtocol(), getIP(), getPort(),
                month, day, year);

        AuthJSONArrayRequest request = new AuthJSONArrayRequest(url, null, new ArrayResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

        return m_lock;
    }

    public Object findPatientsByName(String name) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        String url = String.format("%s://%s:%s/tscharts/v1/patient?name=%s", getProtocol(), getIP(), getPort(), name);

        AuthJSONArrayRequest request = new AuthJSONArrayRequest(url, null, new ArrayResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

        return m_lock;
    }

    public Object updatePatient(PatientData pd) {

        VolleySingleton volley = VolleySingleton.getInstance();

        volley.initQueueIf(getContext());

        RequestQueue queue = volley.getQueue();

        JSONObject data = pd.toJSONObject();

        String url = String.format("%s://%s:%s/tscharts/v1/patient/%d/", getProtocol(), getIP(), getPort(), pd.getId());

        AuthJSONObjectRequest request = new AuthJSONObjectRequest(Request.Method.PUT, url, data, new PutResponseListener(), new ErrorListener());
        request.setRetryPolicy(new DefaultRetryPolicy(getTimeoutInMillis(), getRetries(), DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add((JsonObjectRequest) request);

        return m_lock;
    }
}
