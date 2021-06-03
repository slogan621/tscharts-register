/*
 * (C) Copyright Syd Logan 2021
 * (C) Copyright Thousand Smiles Foundation 2021
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
package org.thousandsmiles.tschartsregister

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.thousandsmiles.tscharts_common_ui.AppVaccineFragment

class VaccinationActivity : AppCompatActivity() {
    private val m_activity: Activity = this
    private val m_sess = SessionSingleton.getInstance()
    private var m_context: Context? = null
    var m_fragment: AppVaccineFragment? = null
    var m_registrationSummaryFragment: RegistrationSummaryFragment? = null
    override fun onResume() {
        super.onResume()
        val root = window.decorView.rootView
        root.viewTreeObserver.addOnGlobalLayoutListener { goImmersive() }
    }

    fun handleBackButtonPress(v: View?) {
        startActivity(Intent(m_activity, MedicalHistoryActivity::class.java))
        m_activity.finish()
    }

    fun handleNextButtonPress(v: View?) {
        m_fragment!!.handleNextButtonPress(v)
    }

    override fun onBackPressed() {
        startActivity(Intent(this@VaccinationActivity, PatientInfoActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_vaccination)
        m_context = applicationContext
        var arguments = Bundle()
        m_fragment = AppVaccineFragment()
        m_fragment!!.arguments = arguments
        supportFragmentManager.beginTransaction()
            .replace(R.id.app_panel, m_fragment!!)
            .commit()
        m_registrationSummaryFragment = RegistrationSummaryFragment()
        arguments = Bundle()
        m_registrationSummaryFragment!!.arguments = arguments
        supportFragmentManager.beginTransaction()
            .replace(R.id.registration_summary_panel, m_registrationSummaryFragment!!)
            .commit()
    }

    /* see also  https://stackoverflow.com/questions/24187728/sticky-immersive-mode-disabled-after-soft-keyboard-shown */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            goImmersive()
        }
    }

    fun goImmersive() {
        val v1 = window.decorView.rootView
        v1.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}