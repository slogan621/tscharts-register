/*
 * (C) Copyright Syd Logan 2020
 * (C) Copyright Thousand Smiles Foundation 2020
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
import android.view.ViewTreeObserver;

public class VerifyCURPActivity extends AppCompatActivity {

    private Activity m_activity = this;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context;
    AppVerifyCURPFragment m_fragment;
    RegistrationSummaryFragment m_registrationSummaryFragment;

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
    }

    public void handleBackButtonPress(View v) {
        startActivity(new Intent(m_activity, PatientInfoActivity.class));
        m_activity.finish();
    }

    public void handleNextButtonPress(View v) {
        m_fragment.handleNextButtonPress(v);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(VerifyCURPActivity.this, PatientInfoActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_medical_history);
        m_context = getApplicationContext();
        Bundle arguments = new Bundle();
        m_fragment = new AppVerifyCURPFragment();
        m_fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.app_panel, m_fragment)
                .commit();
        m_registrationSummaryFragment = new RegistrationSummaryFragment();
        arguments = new Bundle();
        m_registrationSummaryFragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.registration_summary_panel, m_registrationSummaryFragment)
                .commit();
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

