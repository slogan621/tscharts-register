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

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
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
    private ImageView m_imageView;

    public static RegistrationSummaryFragment newInstance() {
        return new RegistrationSummaryFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        m_activity=(Activity) getActivity();
        m_sess = SessionSingleton.getInstance();
        m_isNewPatient = m_sess.getIsNewPatient();
        m_imageView = (ImageView) m_activity.findViewById(R.id.headshot_image_main);
        if (m_isNewPatient == false) {
            m_patientId = m_sess.getPatientId();
            m_patientData = m_sess.getPatientData(m_patientId);
        } else {
            m_patientData = m_sess.getNewPatientData();
        }

        boolean displayGenderImage = false;

        String imagePath = m_sess.getPhotoPath();
        if (imagePath.length() == 0) {
            displayGenderImage = true;
        }

        if (displayGenderImage == true) {
            if (m_patientData.getGender() == "Female") {
                m_imageView.setImageResource(R.drawable.girlfront);
            } else {
                m_imageView.setImageResource(R.drawable.boyfront);
            }
        } else {
            File file = new File(imagePath);
            Picasso.with(getContext()).load(file).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(m_imageView);
        }

        TextView t = (TextView) getView().findViewById(R.id.value_summary_name);
        String middle = m_patientData.getMiddle();
        String first = m_patientData.getFirst();
        String flast = m_patientData.getFatherLast();
        String mlast = m_patientData.getMotherLast();

        String format = "";

        if (first.length() > 0 || middle.length() > 0 || flast.length() > 0 || mlast.length() > 0) {

            if (first.length() > 0) {
                format = format.concat("%s");
            } else {
                format = format.concat("%s");
            }

            if (middle.length() > 0) {
                format = format.concat(" %s");
            } else {
                format = format.concat("%s");
            }

            if (flast.length() > 0) {
                format = format.concat(" %s");
            } else {
                format = format.concat("%s");
            }

            if (mlast.length() > 0) {
                format = format.concat("-%s");
            } else {
                format = format.concat("%s");
            }

            t.setText(String.format(format,
                    m_patientData.getFirst(),
                    m_patientData.getMiddle(),
                    m_patientData.getFatherLast(),
                    m_patientData.getMotherLast()));
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_name_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }
        t = (TextView) getView().findViewById(R.id.value_summary_dob);
        if (m_patientData.getDob().length() > 0) {
            t.setText(String.format("%s", m_patientData.getDob()));
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_dob_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }
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