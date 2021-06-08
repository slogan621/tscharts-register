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
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.PatientData;

import java.io.File;

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
        m_isNewPatient = CommonSessionSingleton.getInstance().getIsNewPatient();
        m_imageView = (ImageView) m_activity.findViewById(R.id.headshot_image_main);
        if (m_isNewPatient == false) {
            m_patientId = m_sess.getPatientId();
            m_patientData = m_sess.getPatientData(m_patientId);
        } else {
            m_patientData = m_sess.getNewPatientData();
        }

        boolean displayGenderImage = false;

        String imagePath = m_sess.getCommonSessionSingleton().getPhotoPath();
        if (imagePath == null || imagePath.length() == 0) {
            displayGenderImage = true;
        }

        if (displayGenderImage == true) {
            if (m_patientData.getGender().equals("Female")) {
                m_imageView.setImageResource(R.drawable.girlfront);
            } else {
                m_imageView.setImageResource(R.drawable.boyfront);
            }
        } else {
            File file = new File(imagePath);
            Picasso.get().load(file).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(m_imageView);
        }

        TextView t = (TextView) getView().findViewById(R.id.value_summary_curp);

        String curp = m_patientData.getCURP();

        if (curp.length() > 0) {
            t.setText(String.format("CURP: %s", curp));
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_curp);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }

        String middle = m_patientData.getMiddle();
        String first = m_patientData.getFirst();
        String flast = m_patientData.getFatherLast().toUpperCase();
        String mlast = m_patientData.getMotherLast().toUpperCase();

        String format = "";
        t = (TextView) getView().findViewById(R.id.value_summary_father_last_name);

        if (flast.length() > 0) {
          t.setText(flast);
          t.setTypeface(null, Typeface.BOLD_ITALIC);
          t.setBackgroundResource(R.color.pressed_color);
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_father_last_name_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }

        t = (TextView) getView().findViewById(R.id.value_summary_mother_last_name);
        if (mlast.length() > 0) {
              t.setText(mlast);
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_mother_last_name_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }

        t = (TextView) getView().findViewById(R.id.value_summary_first_name);
        if (first.length() > 0) {
            t.setText(m_patientData.getFirst());
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_first_name_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }

        t = (TextView) getView().findViewById(R.id.value_summary_middle_name);
        if (middle.length() > 0) {
            t.setText(m_patientData.getMiddle());
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_middle_name_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }
        t = (TextView) getView().findViewById(R.id.value_summary_dob);
        if (m_patientData.getDobMilitary(m_sess.getContext()).length() > 0) {
            t.setText(String.format("%s", m_patientData.getDobMilitary(m_sess.getContext())));
        } else {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_dob_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        }

        if (m_isNewPatient) {
            TableRow r = (TableRow) getView().findViewById(R.id.summary_id_row);
            if (r != null) {
                r.setVisibility(View.GONE);
            }
        } else {
            t = (TextView) getView().findViewById(R.id.value_summary_id);
            t.setText(String.format("%d", m_patientId));
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
            name = m_sess.categoryToSpanish(name);
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