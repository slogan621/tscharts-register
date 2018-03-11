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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PatientSearchActivity extends AppCompatActivity {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context;

    public void handleButtonPress(View v)
    {
        this.m_activity.finish();
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

        m_sess.clearHeadShotCache();
        m_sess.setPhotoPath("");
        final ClinicREST clinicREST = new ClinicREST(m_context);
        final Object lock;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
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
                        Toast.makeText(getApplicationContext(), R.string.error_clinic_not_found_date, Toast.LENGTH_LONG).show();
                    }
                });
            } else if (status == 500) {
                PatientSearchActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                    }
                });
            } else if (status != 200){
                PatientSearchActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                    }
                });
            }
            if (status != 200) {
                finish();
            }
            }
        };
        thread.start();

    }

    private void LayoutSearchResults() {
        TableLayout layout = (TableLayout) findViewById(R.id.namestablelayout);
        TableRow row = null;

        int count = layout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = layout.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        LinearLayout btnLO = new LinearLayout(this);

        LinearLayout.LayoutParams paramsLO = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLO.setOrientation(LinearLayout.VERTICAL);

        TableRow.LayoutParams parms = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT);

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
                                m_sess.setIsNewPatient(true);
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

            HeadshotImage headshot  = new HeadshotImage();
            headshot.setActivity(this);
            headshot.setImageView(button);
            Thread t = headshot.getImage(id);
            m_sess.addHeadShotPath(id, headshot.getImageFileAbsolutePath());
            t.start();

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                RegisterDialogFragment rtc = new RegisterDialogFragment();
                PatientData o = (PatientData) v.getTag();
                rtc.setPatientId(o.getId());
                rtc.show(getSupportFragmentManager(), getApplicationContext().getString(R.string.title_register_dialog));
                  }
            });

            btnLO.addView(button);

            txt = new TextView(getApplicationContext());
            txt.setText(String.format("%d %s, %s", id, paternalLast, first));
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
    }

    private Date isDateString(String s) {
        // supports variations where year is yyyy or yy day is dd and month is MM

        Date ret = null;

        String[] formats = {
                "MM-dd-yy",
                "MM/dd/yy",
                "MM dd yy",
                "MM-dd-yyyy",
                "MM/dd/yyyy",
                "MM dd yyyy"
        };

        for (int i = 0; i < formats.length; i++){
            try {
                DateFormat df = new SimpleDateFormat(formats[i], Locale.ENGLISH);
                Date date = df.parse(s);
                ret = date;
                break;
            } catch (ParseException pe) {
            }
        }
        return ret;
    }

    private void getMatchingPatients(final String searchTerm)
    {
        // analyze search term, looking for DOB string, gender, or name. Then, search on
        // each until a non-zero match is returned

        ArrayList<Integer> ret = new ArrayList<Integer>();

        m_sess.clearSearchResultData();

        final Date d = isDateString(searchTerm);
        new Thread(new Runnable() {
            public void run() {
                final PatientREST x = new PatientREST(getApplicationContext());

                final Object lock;

                if (d != null) {
                    lock = x.findPatientsByDOB(d);
                } else if (searchTerm.length() > 0) {
                    lock = x.findPatientsByName(searchTerm);
                } else {
                    lock = x.getAllPatientData();
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

                    if (x.getStatus() == 200) {
                        m_sess.getPatientSearchResultData();
                        PatientSearchActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                LayoutSearchResults();
                            }
                        });
                        return;
                    } else if (x.getStatus() == 101) {
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
                    }
                };
                thread.start();
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_patient_search);

        Button button = (Button) findViewById(R.id.patient_search_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            EditText t = (EditText) findViewById(R.id.patient_search);
            String searchTerm = t.getText().toString();
            getMatchingPatients(searchTerm);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }

        });
        m_context = this;
        m_activity = this;
    }
}

