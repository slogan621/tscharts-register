/*
 * (C) Copyright Syd Logan 2018-2019
 * (C) Copyright Thousand Smiles Foundation 2018-2019
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
//import com.github.barteksc.pdfviewer.util.FitPolicy;

import org.json.JSONArray;
import org.json.JSONObject;
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.RESTCompletionListener;

import java.util.ArrayList;
import java.util.Locale;

import static org.thousandsmiles.tschartsregister.AppWaiverFragment.RegistrationState.REGISTRATION_FAILED;

public class AppWaiverFragment extends Fragment implements RESTCompletionListener, OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private boolean m_dirty = false;
    PDFView pdfView;
    int pageNumber;
    int m_count;
    private AppWaiverFragment m_this = this;
    ArrayList<Integer> m_stations = null;
    private boolean m_showSuccess = false;
    private View m_view;
    private View m_progressView;
    private boolean m_photoChecked = false;
    private boolean m_waiverChecked = false;
    CommonSessionSingleton m_common = CommonSessionSingleton.getInstance();

    public enum RegistrationState {
        UPDATED_NOTHING,
        UPDATED_PATIENT,
        UPDATED_MEDICAL_HISTORY,
        UPDATED_VACCINATIONS,
        UPDATED_PHOTO,
        UPDATED_ROUTING_SLIP,
        UPDATED_ROUTING_SLIP_ENTRIES,
        CREATED_CONSENT,
        CREATED_REGISTRATION,
        REGISTRATION_FAILED,
    };

    private RegistrationState m_state = RegistrationState.UPDATED_NOTHING;

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }

    @Override
    public void loadComplete(int nbPages) {
    }

    @Override
    public void onFail(int code, String msg)
    {
        String logMsg;
        logMsg = String.format("onFail state: %s", getStateString());
        Log.e("AppWaiverFragment", logMsg);
        m_state = REGISTRATION_FAILED;
        showFailure(code, msg);
    }

    private ArrayList<Integer> mergeStations(ArrayList<Integer> list1, ArrayList<Integer> list2)
    {
        ArrayList<Integer> ret = list1;

        for (int i = 0; i < list2.size(); i++) {
            if (ret.contains(list2.get(i)) == false) {
                ret.add(list2.get(i));
            }
        }

        return ret;
    }

    @Override
    public void onSuccess(int code, String msg, JSONObject o)
    {
        // XXX - route to the generic handler
        onSuccess(code, msg);
    }

    @Override
    public void onSuccess(int code, String msg, JSONArray a)
    {
        // XXX - route to the generic handler
        onSuccess(code, msg);
    }

    private String getStateString()
    {
        String ret = "Unknown state";
        switch (m_state) {
            case UPDATED_NOTHING:
                ret = "UPDATED_NOTHING";
                break;
            case UPDATED_PATIENT:
                ret = "UPDATED_PATIENT";
                break;
            case UPDATED_MEDICAL_HISTORY:
                ret = "UPDATED_HISTORY";
                break;
            case UPDATED_VACCINATIONS:
                ret = "UPDATED_VACCINATIONS";
                break;
            case UPDATED_PHOTO:
                ret = "UPDATED_PHOTO";
                break;
            case UPDATED_ROUTING_SLIP:
                ret = "UPDATED_ROUTING_SLIP";
                break;
            case UPDATED_ROUTING_SLIP_ENTRIES:
                ret = "UPDATED_ROUTING_SLIP_ENTRIES";
                break;
            case CREATED_CONSENT:
                ret = "CREATED_CONSENT";
                break;
            case CREATED_REGISTRATION:
                ret = "CREATED_REGISTRATION";
                break;
            case REGISTRATION_FAILED:
                ret = "REGISTRATION_FAILED";
                break;
        }
        return ret;
    }

    /* patient --> medical history --> photo --> routing slip --> routing slip entries
       --> register --> consent */
    @Override
    public void onSuccess(int code, String msg)
    {
        String logMsg;
        logMsg = String.format("onSuccess state: %s", getStateString());
        Log.e("AppWaiverFragment", logMsg);
        if (m_state == RegistrationState.UPDATED_NOTHING) {
            m_state = RegistrationState.UPDATED_PATIENT;
            if (CommonSessionSingleton.getInstance().getIsNewPatient() || m_sess.getIsNewMedicalHistory()) {
                m_sess.createMedicalHistory(this);
            } else {
                m_sess.updateMedicalHistory(this);
            }
        } else if (m_state == RegistrationState.UPDATED_PATIENT) {
            m_state = RegistrationState.UPDATED_MEDICAL_HISTORY;
            // if they did not take a photo, no photopath
            String photoPath = CommonSessionSingleton.getInstance().getPhotoPath();
            if (photoPath != null && photoPath.equals("") == false) {
                m_sess.getCommonSessionSingleton().createImage(m_sess.getClinicId(), m_sess.getActivePatientId(),
                        "Headshot", this);
            } else {
                m_state = RegistrationState.UPDATED_MEDICAL_HISTORY;
                m_sess.createRoutingSlip(this);
            }
        } else if (m_state == RegistrationState.UPDATED_MEDICAL_HISTORY) {
            m_state = RegistrationState.UPDATED_PHOTO;

            if (CommonSessionSingleton.getInstance().getIsNewPatient() || m_common.getIsNewVaccination()) {
                m_common.createVaccination(this);
            } else {
                m_common.updateVaccination(this);
            }
        } else if (m_state == RegistrationState.UPDATED_PHOTO) {
            m_state = RegistrationState.UPDATED_VACCINATIONS;
            m_sess.createRoutingSlip(this);
        } else if (m_state == RegistrationState.UPDATED_VACCINATIONS) {
            m_state = RegistrationState.UPDATED_ROUTING_SLIP;
            ArrayList<Integer> catStations = m_sess.getStationsForCategory(m_sess.getCategoryName());
            ArrayList<Integer> rtcStations = m_sess.getReturnToClinicStations();
            ArrayList<Integer> stations = mergeStations(catStations, rtcStations);
            m_stations = stations;
            m_count = m_stations.size();
            // XXX what if size is 0?
            while (m_stations.size() > 0) {
                m_sess.createRoutingSlipEntry(this, m_stations.get(0));
                m_stations.remove(m_stations.get(0));
            }
        } else if (m_state == RegistrationState.UPDATED_ROUTING_SLIP) {
            m_count--;
            if (m_count <= 0) {
                m_state = RegistrationState.UPDATED_ROUTING_SLIP_ENTRIES;
                m_sess.register(this, m_sess.getPatientId(), m_sess.getClinicId());
            }
        } else if (m_state == RegistrationState.UPDATED_ROUTING_SLIP_ENTRIES) {
            m_state = RegistrationState.CREATED_REGISTRATION;
            String m;
            m = String.format("calling createConsentRecord reg ID is %d", m_sess.getRegistrationId());
            Log.e("AppWaiverFragment", m);
            m_sess.createConsentRecord(this, m_sess.getPatientId(), m_sess.getClinicId(), m_sess.getRegistrationId(), m_waiverChecked, m_photoChecked);
        } else if (m_state == RegistrationState.CREATED_REGISTRATION){
            m_state = RegistrationState.CREATED_CONSENT;
            if (m_showSuccess == false) {
                showSuccess();
                m_showSuccess = true;
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            m_activity = (Activity) context;
        }
    }

    private void showSuccess() {

        showProgress(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(m_activity.getString(R.string.title_successful_registration));
        builder.setMessage(m_activity.getString(R.string.msg_successfully_registered_patient));

        builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(m_activity, PatientSearchActivity.class));
                m_activity.finish();
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            m_progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                }
            });

            m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            m_progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                }
            });
        }
    }

    private void showFailure(final int code, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(m_activity.getString(R.string.title_failed_registration));
        String msgStr;
        if (code == 409) {
            msgStr = String.format("%s\ncode: %d msg: %s", m_activity.getString(R.string.msg_patient_already_in_database), code, msg);
        } else {
            msgStr = String.format("%s\ncode: %d msg: %s", m_activity.getString(R.string.msg_failed_to_register_patient), code, msg);
         }
        builder.setMessage(msgStr);

        builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (code == 409) {
                    startActivity(new Intent(m_activity, PatientSearchActivity.class));
                    m_activity.finish();
                    dialog.dismiss();
                } else {
                    setRegisterButtonEnabled(true);
                    setBackButtonEnabled(true);
                }
            }
        });

        showProgress(false);
        AlertDialog alert = builder.create();
        alert.show();

    }

    public void handleRegisterButtonPress(View v) {
        m_view = v;
        m_progressView = (View) getActivity().findViewById(R.id.progress_bar);
        //m_waiverContainer = (View) getActivity().findViewById(R.id.waiver_container);
        if (true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_confirm_registration));
            builder.setMessage(m_activity.getString(R.string.msg_register_patient));

            m_this = this;

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setRegisterButtonEnabled(false);
                    setBackButtonEnabled(false);
                    m_state = RegistrationState.UPDATED_NOTHING;
                    if (CommonSessionSingleton.getInstance().getIsNewPatient() == true) {
                        m_sess.createNewPatient(m_this);
                    } else {
                        m_sess.updatePatientData(m_this, m_sess.getPatientId());
                    }
                    showProgress(true);
                    dialog.dismiss();

                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void displayFromAsset() {
        String pdfFileName;
        pdfView = (PDFView) getActivity().findViewById(R.id.pdfView);

        if (pdfView != (PDFView) null) {

            Locale current = getContext().getResources().getConfiguration().locale;
            if (current.getLanguage().equals("es")) {
                pdfFileName="terms_es.pdf";
            } else {
                pdfFileName="terms.pdf";
            }

            pdfView.fromAsset(pdfFileName)
                    .defaultPage(0)
                    .onPageChange(this)
                    //.enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this.getContext()))
                    .spacing(10) // in dp
                    .onPageError(this)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load();
        }
    }

    public void onWaiverCheckboxClicked(View v) {
        CheckBox cb = (CheckBox) v;
        boolean enable = false;

        if (cb.isChecked()) {
            m_waiverChecked = true;
            enable = true;
        } else {
            m_waiverChecked = false;
        }
        setRegisterButtonEnabled(enable);
    }

    public void onWaiverPhotoClicked(View v) {
        CheckBox cb = (CheckBox) v;
        boolean enable = false;

        if (cb.isChecked()) {
            m_photoChecked = true;
            enable = true;
        } else {
            m_photoChecked = false;
        }
    }

    private void setRegisterButtonEnabled(boolean enable)
    {
        Button b = (Button) m_activity.findViewById(R.id.register_button);
        b.setEnabled(enable);
    }

    private void setBackButtonEnabled(boolean enable)
    {
        Button b = (Button) m_activity.findViewById(R.id.back_button);
        b.setEnabled(enable);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRegisterButtonEnabled(false);
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
         super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_waiver_layout, container, false);
        m_sess = SessionSingleton.getInstance();
        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        displayFromAsset();
        CheckBox cb = (CheckBox) m_activity.findViewById(R.id.waiver_acknowledgement);
        cb.setChecked(false);
        setRegisterButtonEnabled(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}