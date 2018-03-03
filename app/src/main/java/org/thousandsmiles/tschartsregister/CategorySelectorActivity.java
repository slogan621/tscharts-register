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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class CategorySelectorActivity extends AppCompatActivity {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context;
    private RegistrationSummaryFragment m_registrationSummaryFragment;

    public void handleButtonPress(View v)
    {
        this.m_activity.finish();
    }

    private void confirmCategorySelection(final JSONObject cs) {
        final int id;
        final String name;
        try {
            name = cs.getString("name");
            id = cs.getInt("id");
        } catch (JSONException e) {
            Toast.makeText(CategorySelectorActivity.this, R.string.msg_unable_process_category,Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(String.format(m_activity.getString(R.string.question_select_category), name));
                alertDialogBuilder.setPositiveButton(R.string.button_yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            m_sess.setCategory(id);
                            m_sess.setCategoryName(name);
                            Intent intent = new Intent(m_activity, PatientInfoActivity.class);
                            startActivity(intent);
                            finish();
                        }
                });

        alertDialogBuilder.setNegativeButton(R.string.button_no,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(CategorySelectorActivity.this, R.string.msg_select_another_category,Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void LayoutCategoryGrid() {
        TableLayout layout = (TableLayout) findViewById(R.id.namestablelayout);

        int numCategories = m_sess.getCategoryCount();

        TableRow row = null;
        for (int count = 0; count < numCategories; count++) {
            boolean newRow = false;
            if ((count % m_sess.getSelectorNumColumns()) == 0) {
                newRow = true;
                row = new TableRow(getApplicationContext());
                row.setWeightSum((float)1.0);
                TableRow.LayoutParams parms = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                int leftMargin=10;
                int topMargin=2;
                int rightMargin=10;
                int bottomMargin=2;

                parms.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

                row.setLayoutParams(parms);
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

            btnLO.setLayoutParams(parms);

            ImageButton button = new ImageButton(getApplicationContext());

            button.setBackgroundColor(getResources().getColor(R.color.lightGray));
            JSONObject o = m_sess.getCategoryData(count);
            button.setTag(o);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                JSONObject tag = (JSONObject) v.getTag();
                confirmCategorySelection(tag);
                }
            });

            btnLO.addView(button);

            TextView txt = new TextView(getApplicationContext());

            try {
                String name = o.getString("name");
                txt.setText(String.format("%s", name));  // XXX translate
                button.setImageDrawable(getResources().getDrawable(m_sess.getSelector(name)));
            } catch (JSONException e) {
            }
            txt.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            btnLO.addView(txt);

            if (row != null) {
                row.addView(btnLO);
            }
            if (newRow == true) {
                layout.addView(row, new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_category_selector);
        m_context = getApplicationContext();
        Bundle arguments;
        m_registrationSummaryFragment = new RegistrationSummaryFragment();
        arguments = new Bundle();
        m_registrationSummaryFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.registration_summary_panel, m_registrationSummaryFragment)
                .commit();
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
                m_sess.updateStationData();
            };
        }).start();
    }

    private void getReturnToClinicData() {
        new Thread(new Runnable() {
            public void run() {
                m_sess.getReturnToClinics();
            };
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
            if (status == 200) {
                if (m_sess.updateCategoryData() == false) {
                    CategorySelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.error_unable_to_get_category_data, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    m_sess.initCategoryNameToSelectorMap();
                    CategorySelectorActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            LayoutCategoryGrid();
                        }
                    });
                    /* go get the list of Mexican States for later use */
                    getMexicanStates();
                    /* and clinic stations too */
                    getStations();
                    if (m_sess.getIsNewPatient() == false) {
                        getReturnToClinicData();
                    }
                }

                return;
            } else if (status == 101) {
                CategorySelectorActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error_unable_to_connect, Toast.LENGTH_LONG).show();
                    }
                });

            } else if (status == 400) {
                CategorySelectorActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error_internal_bad_request, Toast.LENGTH_LONG).show();
                    }
                });
            } else if (status == 404) {
                CategorySelectorActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error_clinic_not_found_date, Toast.LENGTH_LONG).show();
                    }
                });
            } else if (status == 500) {
                CategorySelectorActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error_internal_error, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                CategorySelectorActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                    Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_LONG).show();
                    }
                });
            }
            }
        };
        thread.start();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CategorySelectorActivity.this, PatientSearchActivity.class));
        finish();
    }

}

