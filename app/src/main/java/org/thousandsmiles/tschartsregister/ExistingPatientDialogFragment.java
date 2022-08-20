/*
 * (C) Copyright Syd Logan 2022
 * (C) Copyright Thousand Smiles Foundation 2022
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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.thousandsmiles.tscharts_lib.HeadshotImage;
import org.thousandsmiles.tscharts_lib.ImageDisplayedListener;
import org.thousandsmiles.tscharts_lib.PatientData;

// clone of RegisterDialogFragment
public class ExistingPatientDialogFragment extends DialogFragment implements ImageDisplayedListener {

    private View m_view;
    boolean m_hasCurp = false;
    private int m_patientId;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    public void setPatientId(int id)
    {
        m_patientId = id;
    }

    public void onImageDisplayed(int imageId, String path)
    {
        SessionSingleton sess = SessionSingleton.getInstance();
        sess.getCommonSessionSingleton().addHeadShotPath(imageId, path);
    }

    public void onImageError(int imageId, String path, int errorCode)
    {
        if (errorCode != 404) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.msg_unable_to_get_patient_headshot), Toast.LENGTH_SHORT).show();
                }
            });
        }
        SessionSingleton.getInstance().getCommonSessionSingleton().removeHeadShotPath(imageId);
        SessionSingleton.getInstance().getCommonSessionSingleton().startNextHeadshotJob();
    }

    private void getReturnToClinicData() {
        new Thread(new Runnable() {
            public void run() {
                m_sess.getReturnToClinics();
            };
        }).start();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Dialog ret = null;
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        PatientData o = m_sess.getDuplicatePatientData();
        if (o != null) {
            if (o.getValid() == true) {
                String fatherLast = "";
                String motherLast = "";
                String first = "";
                String middle = "";
                String dob = "";
                String gender = "";

                fatherLast = o.getFatherLast().toUpperCase();
                motherLast = o.getMotherLast().toUpperCase();
                first = o.getFirst();
                middle = o.getMiddle();
                dob = o.getDobMilitary(m_sess.getContext());
                gender = o.getGender();

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                m_view = inflater.inflate(R.layout.existing_patient_dialog, null);
                TextView v = (TextView) m_view.findViewById(R.id.patient_father_last);
                v.setText(fatherLast);
                v.setTypeface(null, Typeface.BOLD_ITALIC);
                v.setBackgroundResource(R.color.pressed_color);
                v = (TextView) m_view.findViewById(R.id.patient_mother_last);
                v.setText(motherLast);
                v = (TextView) m_view.findViewById(R.id.patient_first);
                v.setText(first);
                v = (TextView) m_view.findViewById(R.id.patient_middle);
                v.setText(middle);
                v = (TextView) m_view.findViewById(R.id.patient_id);
                v.setText(String.format("%d", m_patientId));
                v = (TextView) m_view.findViewById(R.id.patient_dob);
                v.setText(dob);
                v = (TextView) m_view.findViewById(R.id.patient_gender);
                String genderString = getResources().getString(R.string.male);
                if (gender.equals("Female")) {
                    genderString = getResources().getString(R.string.female);
                }
                v.setText(genderString);
                v = (TextView) m_view.findViewById(R.id.patient_curp);
                v.setText(o.getCURP());
                if (o.getCURP().length() > 0)  {
                    m_hasCurp = true;
                }
            }
        } else {
            Toast.makeText(getActivity(), R.string.error_unable_to_get_patient_data, Toast.LENGTH_LONG).show();
        }

        final PatientData patientData = o;
        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.continue_with_existing_patient_data, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final Class<?> nextClass;
                        nextClass = VerifyCURPActivity.class;
                        m_sess.setPatientId(m_patientId);
                        m_sess.updatePatientData(patientData);
                        m_sess.getCommonSessionSingleton().setIsNewPatient(false);
                        m_sess.getCommonSessionSingleton().setPhotoPath(m_sess.getCommonSessionSingleton().getHeadShotPath(m_patientId));
                        Intent intent = new Intent(getActivity(), nextClass);
                        Bundle b = new Bundle();
                        b.putBoolean("hasCurp", m_hasCurp);
                        intent.putExtras(b);
                        startActivity(intent);
                        getActivity().finish();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.make_changes_and_try_again, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.return_to_search_screen, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final Class<?> nextClass;
                        nextClass = PatientSearchActivity.class;
                        Intent intent = new Intent(getActivity(), nextClass);
                        startActivity(intent);
                        getActivity().finish();
                        dialog.dismiss();
                    }
                });
        ret = builder.create();

        ret.setTitle(R.string.title_existing_patient_dialog);
        HeadshotImage headshot = new HeadshotImage();
        m_sess.getCommonSessionSingleton().addHeadshotImage(headshot);
        headshot.setActivity(getActivity());
        headshot.setImageView(m_view.findViewById(R.id.duplicate_headshot));
        headshot.registerListener(this);
        Thread t = headshot.getImage(m_patientId);
        headshot.start();

        return ret;
    }
}