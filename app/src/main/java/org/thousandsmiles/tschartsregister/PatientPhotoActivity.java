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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class PatientPhotoActivity extends AppCompatActivity {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    AppPatientPhotoFragment m_patientPhotoFragment;
    RegistrationSummaryFragment m_registrationSummaryFragment;

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void handleNextButtonPress(View v) {
        m_patientPhotoFragment.handleNextButtonPress(v);
    }

    public void handleImageButton1Press(View v) {
        m_patientPhotoFragment.handleImageButton1Press(v);
    }

    public void handleImageButton2Press(View v) {
        m_patientPhotoFragment.handleImageButton2Press(v);
    }

    public void handleImageButton3Press(View v) {
        m_patientPhotoFragment.handleImageButton3Press(v);
    }

    public void handleImage1Press(View v) {
        m_patientPhotoFragment.handleImage1Press(v);
    }

    public void handleImage2Press(View v) {
        m_patientPhotoFragment.handleImage2Press(v);
    }

    public void handleImage3Press(View v) {
        m_patientPhotoFragment.handleImage3Press(v);
    }

    @Override
    public void onBackPressed() {
        m_patientPhotoFragment.restorePhotoPath();
        startActivity(new Intent(PatientPhotoActivity.this, MedicalHistoryActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_patient_photo);
        m_patientPhotoFragment = new AppPatientPhotoFragment();
        Bundle arguments = new Bundle();
        m_patientPhotoFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, m_patientPhotoFragment)
                .commit();
        m_registrationSummaryFragment = new RegistrationSummaryFragment();
        arguments = new Bundle();
        m_registrationSummaryFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.registration_summary_panel, m_registrationSummaryFragment)
                .commit();
    }
}

