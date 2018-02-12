/*
 * (C) Copyright Syd Logan 2017-2018
 * (C) Copyright Thousand Smiles Foundation 2017-2018
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class RegistrationSummaryFragment extends Fragment {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private int m_patientId;
    private boolean m_isNewPatient = true;
    private PatientData m_patientData;

    public static RegistrationSummaryFragment newInstance() {
        return new RegistrationSummaryFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        m_activity=(Activity) getActivity();
        m_sess = SessionSingleton.getInstance();
        m_isNewPatient = m_sess.getIsNewPatient();
        if (m_isNewPatient == false) {
            m_patientId = m_sess.getPatientId();
            m_patientData = m_sess.getPatientData(m_patientId);
            TextView t = (TextView) getView().findViewById(R.id.value_summary_name);
            t.setText(String.format("%s %s-%s", m_patientData.getFirst(),
                    m_patientData.getFatherLast(), m_patientData.getMotherLast()));
            t = (TextView) getView().findViewById(R.id.value_summary_dob);
            t.setText(String.format("%s", m_patientData.getDob()));
            if (m_activity.getClass() == CategorySelectorActivity.class) {
                TableRow r = (TableRow) getView().findViewById(R.id.summary_category_row);
                if (r != null) {
                    r.setVisibility(View.GONE);
                }
            } else {
                    String name = m_sess.getCategoryName();
                    ImageView v  = (ImageView) getView().findViewById(R.id.value_summary_category);
                    v.setImageResource(m_sess.getCategorySelector(name));
                    t = (TextView) getView().findViewById(R.id.value_summary_category_name);
                    t.setText(name);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.registration_summary_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}