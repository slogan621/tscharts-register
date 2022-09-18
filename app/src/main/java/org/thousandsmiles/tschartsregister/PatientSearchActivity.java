/*
 * (C) Copyright Syd Logan 2018-2021
 * (C) Copyright Thousand Smiles Foundation 2018-2021
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

import android.annotation.SuppressLint;
import java.text.DateFormatSymbols;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.ClinicREST;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.DatePickerFragment;
import org.thousandsmiles.tscharts_lib.HeadshotImage;
import org.thousandsmiles.tscharts_lib.ImageDisplayedListener;
import org.thousandsmiles.tscharts_lib.PatientData;
import org.thousandsmiles.tscharts_lib.PatientREST;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;
import org.thousandsmiles.tscharts_lib.ShowProgress;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatientSearchActivity extends AppCompatActivity implements ImageDisplayedListener, DatePickerDialog.OnDateSetListener {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context;
    private View mProgressView = null;

    public void handleButtonPress(View v)
    {
        this.m_activity.finish();
    }

    public void onImageDisplayed(int imageId, String path)
    {
        SessionSingleton sess = SessionSingleton.getInstance();
        sess.getCommonSessionSingleton().addHeadShotPath(imageId, path);
        sess.getCommonSessionSingleton().startNextHeadshotJob();
    }

    public void onImageError(int imageId, String path, int errorCode)
    {
        if (errorCode != 404) {
            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_patient_headshot), Toast.LENGTH_SHORT).show();
                }
            });
        }
        SessionSingleton.getInstance().getCommonSessionSingleton().removeHeadShotPath(imageId);
        SessionSingleton.getInstance().getCommonSessionSingleton().startNextHeadshotJob();
    }

    private void setDate(final Calendar calendar) {

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String dateString = String.format("%02d%s%04d", day, new DateFormatSymbols().getMonths()[month-1].substring(0, 3).toUpperCase(), year);
        ((TextView) findViewById(R.id.patient_search)).setText(dateString);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        setDate(c);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(String.format(getApplicationContext().getString(R.string.msg_are_you_sure_you_want_to_exit)));
        alertDialogBuilder.setPositiveButton(R.string.button_yes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });

        alertDialogBuilder.setNegativeButton(R.string.button_no,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(StationSelectorActivity.this,"Please select another station.",Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final View root = getWindow().getDecorView().getRootView();
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                goImmersive();
            }
        });

        ImageButton button = findViewById(R.id.patient_search_date_picker);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                org.thousandsmiles.tscharts_lib.DatePickerFragment fragment = new DatePickerFragment();
                fragment.setListeningActivity(PatientSearchActivity.this);
                fragment.show(m_activity.getFragmentManager(), "date");
            }
        });
    }

    private void ClearSearchResultTable()
    {
        TableLayout layout = (TableLayout) findViewById(R.id.namestablelayout);

        layout.removeAllViews();
    }

    private void getMexicanStates() {
        new Thread(new Runnable() {
            public void run() {
                m_sess.getMexicanStates();
            };
        }).start();
    }

    private void getStations() {
        new Thread(new Runnable() {
            public void run() {
                StationData sd = new StationData();
                sd.setContext(m_context);
                sd.updateStationData();
            };
        }).start();
    }

    private void HideSearchResultTable()
    {
       View v = (View) findViewById(R.id.namestablelayout);
       if (v != null) {
           v.setVisibility(View.GONE);
       }
    }

    private void ShowSearchResultTable()
    {
        View v = (View) findViewById(R.id.namestablelayout);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void LayoutSearchResults() {
        TableLayout layout = (TableLayout) findViewById(R.id.namestablelayout);
        TableRow row = null;
        int count;

        ClearSearchResultTable();
        ShowSearchResultTable();

        LinearLayout btnLO = new LinearLayout(this);

        LinearLayout.LayoutParams paramsLO = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLO.setOrientation(LinearLayout.VERTICAL);

        TableRow.LayoutParams parms = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

        int leftMargin=10;
        int topMargin=2;
        int rightMargin=10;
        int bottomMargin=2;
        parms.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
        parms.gravity = (Gravity.CENTER_VERTICAL);

        btnLO.setLayoutParams(parms);
        ImageButton button = new ImageButton(getApplicationContext());

        btnLO.setBackgroundColor(getResources().getColor(R.color.lightGray));

        button.setBackgroundColor(getResources().getColor(R.color.lightGray));
        button.setImageDrawable(getResources().getDrawable(R.drawable.headshot_plus));

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_context);
            alertDialogBuilder.setMessage(m_activity.getString(R.string.question_register_new_patient));
            alertDialogBuilder.setPositiveButton(R.string.button_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            CommonSessionSingleton.getInstance().setIsNewPatient(true);
                            m_sess.resetNewPatientObjects();
                            Intent intent = new Intent(m_activity, CategorySelectorActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

            alertDialogBuilder.setNegativeButton(R.string.button_no,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Toast.makeText(PatientSearchActivity.this, R.string.msg_select_another_category,Toast.LENGTH_LONG).show();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            }
        });

        btnLO.addView(button);

        boolean newRow = true;
        row = new TableRow(getApplicationContext());
        row.setWeightSum((float)1.0);

        TextView txt = new TextView(getApplicationContext());
        txt.setText(R.string.button_label_add_new);
        txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        btnLO.addView(txt);

        row.setLayoutParams(parms);

        if (row != null) {
            row.addView(btnLO);
        }

        if (newRow == true) {
            layout.addView(row, new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT));
        }

        HashMap<Integer, PatientData> map = m_sess.getPatientHashMap();

        count = 1;
        int extraCells = (map.size() + 1) % 3;
        if (extraCells != 0) {
            extraCells = 3 - extraCells;
        }

        for (Map.Entry<Integer, PatientData> entry : map.entrySet()) {
            Integer key = entry.getKey();
            PatientData value = entry.getValue();

            newRow = false;
            if ((count % 3) == 0) {
                newRow = true;
                row = new TableRow(getApplicationContext());
                row.setWeightSum((float)1.0);
                row.setLayoutParams(parms);
            }

            btnLO = new LinearLayout(this);

            btnLO.setOrientation(LinearLayout.VERTICAL);

            btnLO.setLayoutParams(parms);

            button = new ImageButton(getApplicationContext());

            Boolean girl = false;
            int id;
            String paternalLast;
            String first;

            girl = value.getGender().equals("Female");
            id = value.getId();
            paternalLast = value.getFatherLast();
            first = value.getFirst();

            float density = getResources().getDisplayMetrics().density;
            int minX = 333, maxX = 333, minY = 333, maxY = 333;

            // return 1.5 if it's HDPI
            // return 2.0 if it's XHDPI
            // return 3.0 if it's XXHDPI
            // return 4.0 if it's XXXHDPI

            if (density == 1.5) {
                minX = minY = maxX = maxY = 500;
            } else if (density == 2.0) {
                minX = minY = maxX = maxY = 666;
            } else if (density == 3.0) {
                minX = minY = maxX = maxY = 1500;
            } else if (density == 3.0) {
                minX = minY = maxX = maxY = 2000;
            }

            button.setMinimumWidth(minX);
            button.setMaxWidth(maxX);
            button.setMinimumHeight(minY);
            button.setMaxHeight(maxY);

            if (count == 0) {
                btnLO.setBackgroundColor(getResources().getColor(R.color.lightGray));
                button.setBackgroundColor(getResources().getColor(R.color.lightGray));
                button.setImageDrawable(getResources().getDrawable(R.drawable.headshot_plus));
            } else {

                if (girl == true) {
                    button.setImageDrawable(getResources().getDrawable(R.drawable.girlfront));
                    button.setBackgroundColor(getResources().getColor(R.color.girlPink));
                } else {
                    button.setImageDrawable(getResources().getDrawable(R.drawable.boyfront));
                    button.setBackgroundColor(getResources().getColor(R.color.boyBlue));
                }
            }
            button.setTag(value);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    RegisterDialogFragment rtc = new RegisterDialogFragment();
                    PatientData o = (PatientData) v.getTag();
                    rtc.setPatientId(o.getId());
                    rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.title_register_dialog));
                }
            });

            btnLO.addView(button);

            ActivityManager.MemoryInfo memoryInfo = m_sess.getCommonSessionSingleton().getAvailableMemory();

            if (!memoryInfo.lowMemory) {
                HeadshotImage headshot = new HeadshotImage();
                m_sess.getCommonSessionSingleton().addHeadshotImage(headshot);
                headshot.setActivity(this);
                headshot.setImageView(button);
                headshot.registerListener(this);
                Thread t = headshot.getImage(id);
                //m_sess.getCommonSessionSingleton().addHeadshotJob(headshot);
            } else {
                PatientSearchActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_memory_is_low_unable_to_get_image, Toast.LENGTH_LONG).show();
                    }
                });
            }

            //m_sess.addHeadShotPath(id, headshot.getImageFileAbsolutePath());
            //t.start();

            txt = new TextView(getApplicationContext());
            txt.setText(String.format("%d %s, %s", id, paternalLast.toUpperCase(), first));
            txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            btnLO.addView(txt);

            if (row != null) {
                row.addView(btnLO);
            }

            if (newRow == true) {
                layout.addView(row, new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT));
            }
            count++;
        }

        for (int i = 0; i < extraCells; i++) {
            btnLO = new LinearLayout(this);

            btnLO.setOrientation(LinearLayout.VERTICAL);

            btnLO.setLayoutParams(parms);
            if (row != null) {
                row.addView(btnLO);
            }
        }
        m_sess.getCommonSessionSingleton().startNextHeadshotJob();
    }

    class GetMatchingPatientsListener implements RESTCompletionListener {

        @Override
        public void onSuccess(int code, String message, JSONArray a) {
            try {
                m_sess.setPatientSearchResults(a);
            } catch (Exception e) {
            }
        }

        @Override
        public void onSuccess(int code, String message, JSONObject a) {

        }

        @Override
        public void onSuccess(int code, String message) {
        }

        @Override
        public void onFail(int code, String message) {
        }
    }

    private void showProgress(final boolean show) {
        ShowProgress progress = new ShowProgress();
        progress.showProgress(this, null, mProgressView, show);
    }

    private boolean validSearchTerm(String s) {
        if (s.length() > 5) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(s);
        boolean found = matcher.find();
        return !found;
    }

    private void getMatchingPatients(final String searchTerm)
    {
        // analyze search term, looking for DOB string, gender, or name. Then, search.

        ArrayList<Integer> ret = new ArrayList<Integer>();

        m_sess.clearPatientSearchResultData();
        CommonSessionSingleton.getInstance().setIsNewPatient(false);
        m_sess.setIsNewMedicalHistory(false);

        PatientSearchActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                showProgress(true);
                Toast.makeText(getApplicationContext(), R.string.msg_please_wait_while_patients_are_searched_for, Toast.LENGTH_SHORT).show();
            }});

        //CommonSessionSingleton.getInstance().clearStorageDir();

        final Date d = CommonSessionSingleton.getInstance().isDateString(searchTerm);
        new Thread(new Runnable() {
            public void run() {

                // sess.setPatientSearchResults(response);
            final PatientREST x = new PatientREST(getApplicationContext());
            x.addListener(new GetMatchingPatientsListener());

            final Object lock;

            if (d != null) {
                lock = x.findPatientsByDOB(d);
            } else if (searchTerm.length() < 2) {
                lock = x.findPatientsByName("impossible_patient_name");
            } else {
                if (validSearchTerm(searchTerm)) {
                    CheckBox cb = m_activity.findViewById(R.id.curp_checkbox);
                    if (cb != null && cb.isChecked()) {
                        JSONObject o = new JSONObject();

                        try {
                            o.put("curp", searchTerm);
                            lock = x.findPatientsBySearchTerms(o);
                        } catch (Exception e) {
                            m_activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(m_context, R.string.error_unable_to_search_by_curp, Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                    } else {
                        lock = x.findPatientsByName(searchTerm);
                    }
                } else {
                    PatientSearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(m_context);
                            alertDialogBuilder.setMessage(String.format(getApplicationContext().getString(R.string.msg_invalid_search_term)));

                            alertDialogBuilder.setNegativeButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Button button = (Button) findViewById(R.id.patient_search_button);
                                    button.setEnabled(true);
                                    showProgress(false);
                                }
                            });
                            alertDialogBuilder.show();
                        }
                    });
                    return;
                }
            }

            Thread thread = new Thread(){
                public void run() {
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

                showProgress(false);
                if (x.getStatus() == 200) {
                    m_sess.getPatientSearchResultData();
                    PatientSearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showProgress(false);
                            LayoutSearchResults();
                            Button button = (Button) findViewById(R.id.patient_search_button);
                            button.setEnabled(true);
                        }
                    });
                    return;
                }

                PatientSearchActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        showProgress(false);
                    }
                });

                if (x.getStatus() == 101) {
                    PatientSearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (x.getStatus() == 400) {
                    PatientSearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (x.getStatus() == 500) {
                    PatientSearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (x.getStatus() == 404) {
                    PatientSearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        LayoutSearchResults();
                        Toast.makeText(getApplicationContext(), R.string.error_no_matching_patients_found, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    PatientSearchActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                PatientSearchActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Button button = (Button) findViewById(R.id.patient_search_button);
                        button.setEnabled(true);
                    }
                });
                }
            };
            thread.start();
            }
        }).start();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_patient_search);

        final Button button = (Button) findViewById(R.id.patient_search_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText t = (EditText) findViewById(R.id.patient_search);
                String searchTerm = t.getText().toString();
                button.setEnabled(false);
                m_sess.getCommonSessionSingleton().cancelHeadshotImages();
                HideSearchResultTable();
                getMatchingPatients(searchTerm);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                goImmersive();
            }

        });
        m_context = this;
        m_activity = this;
        m_sess.getCommonSessionSingleton().clearHeadShotCache();
        m_sess.getCommonSessionSingleton().setPhotoPath("");
        mProgressView = findViewById(R.id.login_progress);
        CommonSessionSingleton.getInstance().setIsNewPatient(false);
        m_sess.setIsNewMedicalHistory(false);
        m_sess.getCommonSessionSingleton().resetPatientMedicalHistory();
        m_sess.getCommonSessionSingleton().setIsNewVaccination(false);
        m_sess.getCommonSessionSingleton().resetPatientVaccination();

        if (m_sess.getCommonSessionSingleton().getClinicId() == -1) {
            final ClinicREST clinicREST = new ClinicREST(m_context);
            final Object lock;

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            lock = clinicREST.getClinicData(year, month, day);

            final Thread thread = new Thread() {
                public void run() {
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

                    getMexicanStates();
                    getStations();
                    if (m_sess.updateCategoryData() == false) {
                        PatientSearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_unable_to_get_category_data, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        m_sess.initCategoryNameToSelectorMap();
                        m_sess.initCategoryNameToSpanishMap();
                    }

                    SessionSingleton data = SessionSingleton.getInstance();
                    int status = clinicREST.getStatus();
                    if (status == 101) {
                        PatientSearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                            }
                        });

                    } else if (status == 400) {
                        PatientSearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status == 404) {
                        PatientSearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                SelectClinicDialogFragment rtc = new SelectClinicDialogFragment();
                                rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.title_register_dialog));
                            }
                        });
                    } else if (status == 500) {
                        PatientSearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (status != 200) {
                        PatientSearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            };
            thread.start();
        }
    }

    /* see also  https://stackoverflow.com/questions/24187728/sticky-immersive-mode-disabled-after-soft-keyboard-shown */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            goImmersive();
        }
    }

    public void goImmersive() {
        View v1 = getWindow().getDecorView().getRootView();
        v1.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}

