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

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class CategorySelectorActivity extends AppCompatActivity {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context;
    private RegistrationSummaryFragment m_registrationSummaryFragment;

    public void handleButtonPress(View v)
    {
        this.m_activity.finish();
    }

    public void handleBackButtonPress(View v) {
        onBackPressed();
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
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void LayoutCategoryGrid() {
        TableLayout layout = (TableLayout) findViewById(R.id.namestablelayout);

        layout.removeAllViews();
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
                button.setImageDrawable(getResources().getDrawable(m_sess.getSelector(name)));
                name = m_sess.categoryToSpanish(name);
                txt.setText(String.format("%s", name));
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

    @SuppressLint("SourceLockedOrientationActivity")
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

    @Override
    protected void onResume() {

        final View root = getWindow().getDecorView().getRootView();
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                goImmersive();
            }
        });

        super.onResume();
        LayoutCategoryGrid();
        goImmersive();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CategorySelectorActivity.this, PatientSearchActivity.class));
        finish();
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

