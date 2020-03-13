/*
 * (C) Copyright Syd Logan 2017-2020
 * (C) Copyright Thousand Smiles Foundation 2017-2020
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

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.ConsentREST;
import org.thousandsmiles.tscharts_lib.ImageREST;
import org.thousandsmiles.tscharts_lib.MedicalHistory;
import org.thousandsmiles.tscharts_lib.MedicalHistoryREST;
import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.PatientREST;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.RoutingSlipREST;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class SessionSingleton {
    private static SessionSingleton m_instance;
    private JSONObject m_routingSlipEntryResponse = null;
    private JSONArray m_patientSearchResults = null;
    private JSONArray m_registrationSearchResults = null;
    private PatientData m_newPatientData = null; // only if m_isNewPatient is true
    private HashMap<Integer, PatientData> m_patientData = new HashMap<Integer, PatientData>();
    private HashMap<Integer, ReturnToClinic> m_returnToClinicData = new HashMap<Integer, ReturnToClinic>();
    private static HashMap<String, Integer> m_categoryToSelector = new HashMap<String, Integer>();
    private static HashMap<String, String> m_categoryToSpanish = new HashMap<String, String>();
    private static ArrayList<JSONObject> m_categoryData = new ArrayList<JSONObject>();
    private static HashMap<Integer, String> m_stationIdToName = new HashMap<Integer, String>();
    private static HashMap<String, Integer> m_stationNameToId = new HashMap<String, Integer>();
    private int m_selectorNumColumns;
    private int m_width = -1;
    private int m_height = -1;
    private ArrayList<String> m_mexicanStates = new ArrayList<String>();
    private ArrayList<Integer> m_returnToClinics = new ArrayList<Integer>();
    private Registration m_registration = new Registration();
    private int m_registrationId;
    private boolean m_isNewPatient = false;
    private boolean m_isNewMedicalHistory = false; /* old patient, new clinic so new medical history */
    private CommonSessionSingleton m_commonSessionSingleton = null;


    public CommonSessionSingleton getCommonSessionSingleton()
    {
        if (m_commonSessionSingleton == null) {
            m_commonSessionSingleton = CommonSessionSingleton.getInstance();
        }
        return m_commonSessionSingleton;
    }

    public Context getContext() {
        return getCommonSessionSingleton().getContext();
    }

    public void resetNewPatientObjects()
    {
        m_newPatientData = null;
        getCommonSessionSingleton().resetPatientMedicalHistory();
    }

    public PatientData getNewPatientData()
    {
        if (m_newPatientData == null) {
            m_newPatientData = new PatientData();
        }
        return m_newPatientData;
    }

    public int getClinicId() { return getCommonSessionSingleton().getClinicId(); }

    public int getPatientId()
    {
        return getCommonSessionSingleton().getPatientId();
    }

    public void setPatientId(int id)
    {
        getCommonSessionSingleton().setPatientId(id);
    }

    public int getActivePatientId()
    {
        return getCommonSessionSingleton().getPatientId();
    }

    public int getDisplayPatientId()
    {
        return getCommonSessionSingleton().getPatientId();
    }

    public HashMap<Integer, PatientData> getPatientHashMap()
    {
        return m_patientData;
    }

    public void setIsNewPatient(boolean isNew) {
        m_isNewPatient = isNew;
    }

    public boolean getIsNewPatient() {
        return m_isNewPatient;
    }

    public void setIsNewMedicalHistory(boolean isNew) {
        m_isNewMedicalHistory = isNew;
    }

    public boolean getIsNewMedicalHistory() {
        return m_isNewMedicalHistory;
    }

    public void clearPatientSearchResultData()
    {
        m_patientSearchResults = null;
        m_patientData.clear();
    }

    public void clearRegistrationSearchResultData()
    {
        m_registrationSearchResults = null;
    }

    public void setPatientSearchResults(JSONArray results)
    {
        m_patientSearchResults = results;
    }

    public void setCategory(int categoryId) {
        m_registration.setCategory(categoryId);
    }

    public int getCategorySelector(String name)
    {
        return m_categoryToSelector.get(name);
    }

    public int getCategory() {
        return m_registration.getCategory();
    }

    public void setCategoryName(String name) {
        m_registration.setCategoryName(name);
    }

    public String getCategoryName() {
        return m_registration.getCategoryName();
    }

    public void updatePatientData(PatientData pd) {
        if (m_isNewPatient == true) {
            m_newPatientData = pd;
        } else {
            int id = getPatientId();
            m_patientData.put(id, pd);
        }
    }

    public void setRegistrationSearchResults(JSONArray response) {
        m_registrationSearchResults = response;
    }

    public void register(final RESTCompletionListener listener, final int patientId, final int clinicId)
    {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
                // note we use session context because this may be called after onPause()
                RegisterREST rest = new RegisterREST(getContext());
                rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.register(patientId, clinicId);

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_register_patient_record), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_registered_patient_record), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public void setRegistrationId(JSONObject response) {
        try {
            String m;
            m_registrationId = response.getInt("id");
            m = String.format("setRegistrationId: %d", m_registrationId);
            Log.e("setRegistrationId", m);
        } catch (JSONException e) {
            Log.e("setRegistrationId", "FAILED");
        }
    }

    public int getRegistrationId() {
        return m_registrationId;
    }

    public void createConsentRecord(final RESTCompletionListener listener,
                             final int patient, final int clinic, final int registration,
                             final boolean waiver, final boolean photo) {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
            // note we use session context because this may be called after onPause()
            ConsentREST rest = new ConsentREST(getContext());
            rest.addListener(listener);
            Object lock;
            int status;

            lock = rest.createConsent(patient, clinic, registration, photo, waiver);

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
            status = rest.getStatus();
            if (status != 200) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_create_patient_record), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_created_patient_record), Toast.LENGTH_LONG).show();
                    }
                });
            }
            }
        };
        thread.start();
    }

    public void getPatientSearchResultData()
    {
        for (int i = 0; m_patientSearchResults != null && i < m_patientSearchResults.length(); i++) {
            try {
                getPatientData(m_patientSearchResults.getInt(i));
            } catch (JSONException e) {
            }
        }
    }

    class GetPatientDataListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                addPatientData(a);
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    public PatientData getPatientData(final int id) {

        PatientData o = null;

        if (m_patientData != null) {
            o = m_patientData.get(id);
        }
        if (o == null && Looper.myLooper() != Looper.getMainLooper()) {
            GetPatientDataListener listener = new GetPatientDataListener();
            final PatientREST patientData = new PatientREST(getContext());
            patientData.addListener(listener);
            Object lock = patientData.getPatientData(id);

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }

            int status = patientData.getStatus();
            if (status == 200) {
                o = m_patientData.get(id);
            }
        }
        if (o == null) {
            return o;
        }
        return o;
    }

    void updatePatientData(final RESTCompletionListener listener, final int patientId)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
            // note we use session context because this may be called after onPause()
            PatientREST rest = new PatientREST(getContext());
            rest.addListener(listener);
            Object lock;
            int status;

            lock = rest.updatePatient(m_patientData.get(patientId));

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
            status = rest.getStatus();
            if (status != 200) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_medical_history), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_medical_history), Toast.LENGTH_LONG).show();
                    }
                });
            }
            }
        };
        thread.start();
    }

    class CreateNewPatientListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {
            try {
                int id = a.getInt("id");
                setPatientId(id);
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    void createNewPatient(final RESTCompletionListener listener) {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
            // note we use session context because this may be called after onPause()
            PatientREST rest = new PatientREST(getContext());
            rest.addListener(new CreateNewPatientListener());
            rest.addListener(listener);
            Object lock;
            int status;

            lock = rest.createPatient(m_newPatientData);

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
            status = rest.getStatus();
            if (status != 200) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_create_patient_record), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_created_patient_record), Toast.LENGTH_LONG).show();
                    }
                });
            }
            }
        };
        thread.start();
    }

    public void addPatientData(JSONObject data) {
        int id;

        try {
            id = data.getInt("id");
        } catch (JSONException e) {
            return;
        }
        m_patientData.put(id, new PatientData(data));
    }

    public void addReturnToClinic(JSONObject data) {
        int id;

        try {
            id = data.getInt("id");
        } catch (JSONException e) {
            return;
        }
        m_returnToClinicData.put(id, new ReturnToClinic(data));
    }

    void createMedicalHistory(final RESTCompletionListener listener) {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
                // note we use session context because this may be called after onPause()
            MedicalHistoryREST rest = new MedicalHistoryREST(getContext());
            rest.addListener(listener);
            Object lock;
            int status;

            getCommonSessionSingleton().getPatientMedicalHistory().setPatient(getPatientId());
            lock = rest.createMedicalHistory(getCommonSessionSingleton().getPatientMedicalHistory());

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
            status = rest.getStatus();
            if (status != 200) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_medical_history), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                setIsNewMedicalHistory(false);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_medical_history), Toast.LENGTH_LONG).show();
                    }
                });
            }
            }
        };
        thread.start();
    }

    void createRoutingSlip(final RESTCompletionListener listener) {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
                // note we use session context because this may be called after onPause()
                RoutingSlipREST rest = new RoutingSlipREST(getContext());
                rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.createRoutingSlip(getPatientId(), getCommonSessionSingleton().getClinicId(), getCategoryName());

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_create_routing_slip), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_created_routing_slip), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    void createRoutingSlipEntry(final RESTCompletionListener listener, final int station) {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
                // note we use session context because this may be called after onPause()
                RoutingSlipEntryREST rest = new RoutingSlipEntryREST(getContext());
                rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.createRoutingSlipEntry(m_commonSessionSingleton.getPatientRoutingSlipId(), station);

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_create_routing_slip_entry), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_created_routing_slip_entry), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    ArrayList<Integer> getStationsForCategory(String category)
    {
        ArrayList<Integer> ret = new ArrayList<Integer>();

        if (category.equals("New Cleft")) {
            ret.add(m_stationNameToId.get("ENT"));
            ret.add(m_stationNameToId.get("Surgery Screening"));
        } else if (category.equals("Returning Cleft")) {
            ret.add(m_stationNameToId.get("ENT"));
            ret.add(m_stationNameToId.get("Surgery Screening"));
        } else if (category.equals("Dental")) {
            ret.add(m_stationNameToId.get("X-Ray"));
            ret.add(m_stationNameToId.get("Dental"));
        } else if (category.equals("Ortho")) {
            ret.add(m_stationNameToId.get("Ortho"));
        } else if (category.equals("Other")) {
            ret.add(m_stationNameToId.get("Surgery Screening"));
            ret.add(m_stationNameToId.get("ENT"));
        }
        return ret;
    }

    public void addStationData(JSONArray data) {
        int i;
        JSONObject stationdata;

        for (i = 0; i < data.length(); i++)  {
            try {
                stationdata = data.getJSONObject(i);
                m_stationIdToName.put(stationdata.getInt("id"), stationdata.getString("name"));
                m_stationNameToId.put(stationdata.getString("name"), stationdata.getInt("id"));
            } catch (JSONException e) {
                return;
            }
        }
    }

    public MedicalHistory getMedicalHistory(int clinicid, int patientid)
    {
        boolean ret = false;
        MedicalHistory mh = null;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            final MedicalHistoryREST mhData = new MedicalHistoryREST(getContext());
            Object lock = mhData.getMedicalHistoryData(clinicid, patientid);

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }

            int status = mhData.getStatus();
            if (status == 200) {
                mh = getCommonSessionSingleton().getPatientMedicalHistory();
            }
        }
        return mh;
    }

    void updateMedicalHistory(final RESTCompletionListener listener)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                MedicalHistoryREST rest = new MedicalHistoryREST(getContext());
                rest.addListener(listener);
                Object lock;
                int status;

                lock = rest.updateMedicalHistory(getCommonSessionSingleton().getPatientMedicalHistory());

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_unable_to_save_medical_history), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(), getContext().getString(R.string.msg_successfully_saved_medical_history), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }

    public void initCategoryNameToSelectorMap()
    {
        m_categoryToSelector.clear();
        m_categoryToSelector.put("Dental", R.drawable.category_dental_selector);
        m_categoryToSelector.put("Ortho", R.drawable.category_ortho_selector);
        m_categoryToSelector.put("New Cleft", R.drawable.category_new_cleft_selector);
        m_categoryToSelector.put("Returning Cleft", R.drawable.category_returning_cleft_selector);
        m_categoryToSelector.put("Other", R.drawable.category_other_selector);
    }

    public void initCategoryNameToSpanishMap()
    {
        m_categoryToSpanish.clear();
        m_categoryToSpanish.put("Dental", getContext().getString(R.string.category_dental_es));
        m_categoryToSpanish.put("Ortho",  getContext().getString(R.string.category_ortho_es));
        m_categoryToSpanish.put("New Cleft",  getContext().getString(R.string.category_new_cleft_es));
        m_categoryToSpanish.put("Returning Cleft",  getContext().getString(R.string.category_returning_cleft_es));
        m_categoryToSpanish.put("Other",  getContext().getString(R.string.category_other_es));
    }

    public String categoryToSpanish(String name)
    {
        Locale current = getContext().getResources().getConfiguration().locale;
        if (current.getLanguage().equals("es")) {
            String spName = categoryNameToSpanish(name);
            if (spName != null) {
                name = spName;
            }
        }
        return name;
    }

    public String categoryNameToSpanish(String name)
    {
        String ret;

        ret = m_categoryToSpanish.get(name);
        return ret;
    }

    private void getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        m_width = (int) (metrics.widthPixels / metrics.density);
        m_height = (int) (metrics.heightPixels / metrics.density);
    }

    public int getSelectorNumColumns()
    {
        if (m_width == -1 && m_height == -1) {
            getScreenResolution(getContext());
        }
        m_selectorNumColumns = m_width / 250;
        return m_selectorNumColumns;
    }

    public int getSelector(String name) {
        int ret = R.drawable.category_other_selector;

        if (m_categoryToSelector.containsKey(name)) {
            ret = m_categoryToSelector.get(name);
        }
        return ret;
    }

    public void addCategoryData(JSONArray data) {
        int i;
        JSONObject categorydata;

        for (i = 0; i < data.length(); i++)  {
            try {
                categorydata = data.getJSONObject(i);
                m_categoryData.add(categorydata);
            } catch (JSONException e) {
                return;
            }
        }
    }

    public JSONObject getCategoryData(int i) {
        JSONObject ret = null;

        ret = m_categoryData.get(i);
        return ret;
    }

    public int getCategoryCount() {
        return m_categoryData.size();
    }

    public boolean updateCategoryData() {
        boolean ret = false;

        m_categoryData.clear();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final CategoryREST categoryData = new CategoryREST(getContext());
            Object lock = categoryData.getCategoryData();

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }

            int status = categoryData.getStatus();
            if (status == 200) {
                ret = true;
            }
        }
        return ret;
    }

    public void addMexicanStates(JSONArray response) {
        m_mexicanStates.clear();
       for (int i = 0; i < response.length(); i++) {
           try {
               m_mexicanStates.add(response.get(i).toString());
           } catch (JSONException e) {
           }
       }
    }

    public ArrayList<String> getMexicanStatesList()
    {
        return m_mexicanStates;
    }

    public boolean getMexicanStates() {
        boolean ret = false;

        if (m_mexicanStates.size() > 0) {
            ret = true;
        } else {
            m_mexicanStates.clear();
            if (Looper.myLooper() != Looper.getMainLooper()) {
                final MexicanStatesREST mexicanStatesData = new MexicanStatesREST(getContext());
                Object lock = mexicanStatesData.getMexicanStates();

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }

                int status = mexicanStatesData.getStatus();
                if (status == 200) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    public void addReturnToClinics(JSONArray response) {
        m_returnToClinics.clear();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject o = response.getJSONObject(i);
                m_returnToClinics.add(o.getInt("id"));
            } catch (JSONException e) {
            }
        }
    }

    public ArrayList<Integer> getReturnToClinicStations()
    {
        ArrayList<Integer> ret = new ArrayList<Integer>();

        for (int i = 0; i < m_returnToClinics.size(); i++) {
            int clinicDataId = m_returnToClinics.get(i);
            ReturnToClinic rtc = m_returnToClinicData.get(clinicDataId);
            ret.add(rtc.getStation());
        }
        return ret;
    }

    public boolean getReturnToClinics() {
        boolean ret = false;

        m_returnToClinics .clear();
        if (m_isNewPatient == true) {
            ret = true;
        } else {
            int patientId = getPatientId();
            if (Looper.myLooper() != Looper.getMainLooper()) {
                final ReturnToClinicREST rtc = new ReturnToClinicREST(getContext());
                Object lock = rtc.getReturnToClinicsForPatient(patientId);

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }

                int status = rtc.getStatus();
                if (status == 200) {
                    getReturnToClinicObjects();
                    ret = true;
                }
            }
        }
        return ret;
    }

    private void getReturnToClinicObjects() {
        for (int i = 0; i < m_returnToClinics.size(); i++) {
            int item = m_returnToClinics.get(i);
            final ReturnToClinicREST rtc = new ReturnToClinicREST(getContext());
            Object lock = rtc.getReturnToClinic(item);

            synchronized (lock) {
                // we loop here in case of race conditions or spurious interrupts
                while (true) {
                    try {
                        lock.wait();
                        break;
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
        }
    }

    public static SessionSingleton getInstance() {
        if (m_instance == null) {
            m_instance = new SessionSingleton();
        }
        return m_instance;
    }
}


