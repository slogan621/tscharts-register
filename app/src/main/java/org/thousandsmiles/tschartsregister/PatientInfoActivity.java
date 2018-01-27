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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class PatientInfoActivity extends AppCompatActivity {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context;

    @Override
    protected void onResume() {
        super.onResume();
        Bundle arguments = new Bundle();
        AppPatientInfoFragment fragment = new AppPatientInfoFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, fragment)
                .commit();
    }

    public void handleNextButtonPress(View v) {
        startActivity(new Intent(PatientInfoActivity.this, MedicalHistoryActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PatientInfoActivity.this, MedicalHistoryActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_patient_info);
        m_context = getApplicationContext();
    }
}

