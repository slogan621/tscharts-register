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

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SessionSingleton {
    private static SessionSingleton m_instance;
    private static String m_token = "";
    private static Context m_ctx;
    private JSONObject m_patientRoutingSlip = null;
    private MedicalHistory m_patientMedicalHistory = null;
    private JSONObject m_routingSlipEntryResponse = null;
    private JSONArray m_patientSearchResults = null;
    private int m_clinicId;
    private PatientData m_newPatientData = null; // only if m_isNewPatient is true
    private HashMap<Integer, PatientData> m_patientData = new HashMap<Integer, PatientData>();
    private static HashMap<String, Integer> m_categoryToSelector = new HashMap<String, Integer>();
    private static ArrayList<JSONObject> m_categoryData = new ArrayList<JSONObject>();
    private int m_selectorNumColumns;
    private int m_width = -1;
    private int m_height = -1;
    private ArrayList<String> m_medicationsList = new ArrayList<String>();
    private ArrayList<String> m_mexicanStates = new ArrayList<String>();
    private Registration m_registration = new Registration();
    private int m_patientId;
    private boolean m_isNewPatient = false;
    private String m_photoPath = "";
    private File m_storageDir = null;
    private ConcurrentHashMap<Integer, String> m_headshotIdToPath = new ConcurrentHashMap<Integer, String>();

    void setStorageDir(Activity activity) {
        m_storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    void clearHeadShotCache() {
        m_headshotIdToPath.clear();
    }

    void addHeadShotPath(int id, String path) {
        m_headshotIdToPath.put(id, path);
    }

    void removeHeadShotPath(int id) {
        m_headshotIdToPath.remove(id);
    }

    String getHeadShotPath(int id) {
        String ret = null;

        try {
            ret = m_headshotIdToPath.get(id);
        } catch(Exception e) {
        }
        return ret;
    }

    File getStorageDir() {
        return m_storageDir;
    }

    public void setPhotoPath(String path)
    {
        m_photoPath = path;
    }

    public String getPhotoPath()
    {
        return m_photoPath;
    }

    public void resetNewPatientObjects()
    {
        m_newPatientData = null;
        m_patientMedicalHistory = null;
    }

    public PatientData getNewPatientData()
    {
        if (m_newPatientData == null) {
            m_newPatientData = new PatientData();
        }
        return m_newPatientData;
    }

    public int getPatientId()
    {
        return m_patientId;
    }

    public void setPatientId(int id)
    {
        m_patientId = id;
    }

    public int getActivePatientId()
    {
        return getPatientId();
    }

    public int getDisplayPatientId()
    {
        return getPatientId();
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

    public void clearSearchResultData()
    {
        m_patientSearchResults = null;
        m_patientData.clear();
    }

    public MedicalHistory getPatientMedicalHistory()
    {
        return m_patientMedicalHistory;
    }

    public MedicalHistory getNewPatientMedicalHistory()
    {
        if (m_patientMedicalHistory == null) {
            m_patientMedicalHistory = new MedicalHistory();
        }
        return m_patientMedicalHistory;
    }

    public JSONObject getPatientRoutingSlip()
    {
        return m_patientRoutingSlip;
    }

    public void setPatientSearchResults(JSONArray results)
    {
        m_patientSearchResults = results;
    }

    public void setToken(String token) {
        m_token = String.format("Token %s", token);
    }

    public String getToken() {
        return m_token;
    }

    public void setClinicId(int clinicId) {
        m_clinicId = clinicId;
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

    public void setPatientMedicalHistory(JSONObject o)
    {
        if (m_patientMedicalHistory == null) {
            m_patientMedicalHistory = new MedicalHistory();
        }
        m_patientMedicalHistory.fromJSONObject(o);
    }

    public void updatePatientMedicalHistory(MedicalHistory mh) {
        m_patientMedicalHistory = mh;
    }

    public void updatePatientData(PatientData pd) {
        if (m_isNewPatient == true) {
            m_newPatientData = pd;
        } else {
            int id = m_patientId;
            m_patientData.put(id, pd);
        }
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

    public PatientData getPatientData(final int id) {

        PatientData o = null;

        if (m_patientData != null) {
            o = m_patientData.get(id);
        }
        if (o == null && Looper.myLooper() != Looper.getMainLooper()) {
            final PatientREST patientData = new PatientREST(getContext());
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

    void updatePatientData(final int patientId)
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                PatientREST rest = new PatientREST(getContext());
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

    void createNewPatient(final RESTCompletionListener listener) {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
            // note we use session context because this may be called after onPause()
            PatientREST rest = new PatientREST(getContext());
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

    public int getClinicId() {
        return m_clinicId;
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

    void createMedicalHistory(final RESTCompletionListener listener) {
        boolean ret = false;

        Thread thread = new Thread() {
            public void run() {
                // note we use session context because this may be called after onPause()
            MedicalHistoryREST rest = new MedicalHistoryREST(getContext());
            rest.addListener(listener);
            Object lock;
            int status;

            m_patientMedicalHistory.setPatient(getPatientId());
            lock = rest.createMedicalHistory(m_patientMedicalHistory);

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
                mh = getPatientMedicalHistory();
            }
        }
        return mh;
    }

    void updateMedicalHistory()
    {
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                MedicalHistoryREST rest = new MedicalHistoryREST(getContext());
                Object lock;
                int status;

                lock = rest.updateMedicalHistory(m_patientMedicalHistory);

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
            getScreenResolution(m_ctx);
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

    public void setMedicationsList(JSONArray a)
    {
        m_medicationsList.clear();
        for (int i = 0; i < a.length(); i++) {
            try {
                m_medicationsList.add(a.getString(i));
            } catch (JSONException e) {
            }
        }
    }

    public ArrayList<String> getMedicationsList()
    {
        return m_medicationsList;
    }

    public String[] getMedicationsListStringArray()
    {
        return m_medicationsList.toArray(new String[0]);
    }

    public static SessionSingleton getInstance() {
        if (m_instance == null) {
            m_instance = new SessionSingleton();
        }
        return m_instance;
    }

    public void setContext(Context ctx) {
        m_ctx = ctx;
    }
    public Context getContext() {
        return m_ctx;
    }
}


