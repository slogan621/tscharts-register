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
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AppPatientInfoFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private PatientData m_patientData;
    private int m_patientId;
    private boolean m_isNewPatient = true;
    private boolean m_dirty = false;

    public static AppPatientInfoFragment newInstance() {
        return new AppPatientInfoFragment();
    }

    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        ((TextView) m_activity.findViewById(R.id.dob)).setText(dateFormat.format(calendar.getTime()));
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        setDate(c);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
            m_sess = SessionSingleton.getInstance();
            if ((m_isNewPatient = m_sess.getIsNewPatient()) == false) {
                m_patientId = m_sess.getPatientId();
                m_patientData = m_sess.getPatientData(m_patientId);
            }
        }
    }

    public void handleNextButtonPress(View v) {
        //startActivity(new Intent(MedicalHistoryActivity.this, PatientInfoActivity.class));

        final PatientData pd = this.copyPatientDataFromUI();

        if (m_dirty || pd.equals(m_patientData) == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_unsaved_patient_data));
            builder.setMessage(m_activity.getString(R.string.msg_save_patient_data));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    m_sess.updatePatientData(pd);
                    startActivity(new Intent(m_activity, MedicalHistoryActivity.class));
                    m_activity.finish();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(m_activity, MedicalHistoryActivity.class));
                    m_activity.finish();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            startActivity(new Intent(m_activity, MedicalHistoryActivity.class));
            m_activity.finish();
        }
    }

    private void copyPatientDataToUI()
    {
        TextView tx;
        RadioButton rb;

        // Name

        tx = (TextView) m_activity.findViewById(R.id.paternal_last);
        if (tx != null) {
            tx.setText(m_patientData.getFatherLast());
        }

        tx = (TextView) m_activity.findViewById(R.id.maternal_last);
        if (tx != null) {
            tx.setText(m_patientData.getMotherLast());
        }

        tx = (TextView) m_activity.findViewById(R.id.first_name);
        if (tx != null) {
            tx.setText(m_patientData.getFirst());
        }

        tx = (TextView) m_activity.findViewById(R.id.middle_name);
        if (tx != null) {
            tx.setText(m_patientData.getMiddle());
        }

        // Address

        tx = (TextView) m_activity.findViewById(R.id.address_street_1);
        if (tx != null) {
            tx.setText(m_patientData.getStreet1());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_street_2);
        if (tx != null) {
            tx.setText(m_patientData.getStreet2());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_city);
        if (tx != null) {
            tx.setText(m_patientData.getCity());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_colonia);
        if (tx != null) {
            tx.setText(m_patientData.getColonia());
        }

        ArrayList<String> mexicanStates = m_sess.getMexicanStatesList();
        if (mexicanStates != null && mexicanStates.size() > 0) {
            NumberPicker picker = (NumberPicker) m_activity.findViewById(R.id.address_state);
            if (picker != null) {
                picker.setMinValue(0);
                picker.setMaxValue(mexicanStates.size() - 1);
                String[] a = mexicanStates.toArray(new String[0]);
                picker.setDisplayedValues(a);
                for (int i = 0; i < mexicanStates.size(); i++) {
                    if (m_patientData.getState().equals(mexicanStates.get(i))) {
                        picker.setValue(i);
                        break;
                    }
                }
            }
        }

        // Gender and DOB

        String gender = m_patientData.getGender();
        boolean isFemale = true;

        if (gender.equals("Male")) {
            isFemale = false;
        }

        rb = (RadioButton) m_activity.findViewById(R.id.gender_female);
        if (rb != null) {
            rb.setChecked(isFemale);
        }
        rb = (RadioButton) m_activity.findViewById(R.id.gender_male);
        if (rb != null) {
            rb.setChecked(!isFemale);
        }
        /*
        tx = (TextView) m_activity.findViewById(R.id.dob);
        if (tx != null) {
            tx.setText(m_patientData.getDob());
        }
        */

        // Contact Info

        tx = (TextView) m_activity.findViewById(R.id.phone_1);
        if (tx != null) {
            tx.setText(m_patientData.getPhone1());
        }

        tx = (TextView) m_activity.findViewById(R.id.phone_2);
        if (tx != null) {
            tx.setText(m_patientData.getPhone2());
        }

        tx = (TextView) m_activity.findViewById(R.id.e_mail);
        if (tx != null) {
            tx.setText(m_patientData.getEmail());
        }

        // Emergecny Contact Info

        tx = (TextView) m_activity.findViewById(R.id.emergency_phone);
        if (tx != null) {
            tx.setText(m_patientData.getEmergencyPhone());
        }

        tx = (TextView) m_activity.findViewById(R.id.emergency_full_name);
        if (tx != null) {
            tx.setText(m_patientData.getEmergencyFullName());
        }

        tx = (TextView) m_activity.findViewById(R.id.emergency_e_mail);
        if (tx != null) {
            tx.setText(m_patientData.getEmergencyEmail());
        }
    }

    private void setDirty()
    {/*
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.VISIBLE);
        button_bar_item.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            updateMedicalHistory();
            }

        });
        */
        m_dirty = true;
    }

    private void clearDirty()
    {
        /*
        View button_bar_item = m_activity.findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        */
        m_dirty = false;
    }

    private void setViewDirtyListeners()
    {
        Switch sw;
        TextView tx;
        RadioButton rb;

        // name

        tx = (TextView) m_activity.findViewById(R.id.paternal_last);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.maternal_last);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.first_name);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.middle_name);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        // address

        tx = (TextView) m_activity.findViewById(R.id.address_street_1);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.address_street_2);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.address_colonia);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.address_city);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        NumberPicker picker = (NumberPicker) m_activity.findViewById(R.id.address_state);
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                setDirty();
            }
        });

        // gender and dob

        rb = (RadioButton) m_activity.findViewById(R.id.gender_male);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_activity.findViewById(R.id.gender_female);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        final TextView tx1 = (TextView) m_activity.findViewById(R.id.dob);
        if (tx1 != null) {
            tx1.setShowSoftInputOnFocus(false);
            tx1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        DatePickerFragment fragment = new DatePickerFragment();
                        fragment.setListeningActivity(AppPatientInfoFragment.this);
                        fragment.show(m_activity.getFragmentManager(), "date");
                    }
                }
            });
            tx1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerFragment fragment = new DatePickerFragment();
                    fragment.setListeningActivity(AppPatientInfoFragment.this);
                    fragment.show(m_activity.getFragmentManager(), "date");
                }
            });
            tx1.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        // Contact Info

        tx = (TextView) m_activity.findViewById(R.id.phone_1);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
            tx.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }

        tx = (TextView) m_activity.findViewById(R.id.phone_2);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
            tx.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }

        tx = (TextView) m_activity.findViewById(R.id.e_mail);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        // emergency contact info

        tx = (TextView) m_activity.findViewById(R.id.emergency_full_name);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }

        tx = (TextView) m_activity.findViewById(R.id.emergency_phone);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
            tx.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }

        tx = (TextView) m_activity.findViewById(R.id.emergency_e_mail);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {}

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    setDirty();
                }
            });
        }
    }

    private PatientData copyPatientDataFromUI()
    {
        Switch sw;
        TextView tx;
        RadioButton rb;
        boolean bv;
        boolean checked;

        PatientData pd;

        pd = m_patientData;

        // Name

        tx = (TextView) m_activity.findViewById(R.id.paternal_last);
        if (tx != null) {
            pd.setFatherLast(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.maternal_last);
        if (tx != null) {
            pd.setMotherLast(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.first_name);
        if (tx != null) {
            pd.setFirst(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.middle_name);
        if (tx != null) {
            pd.setMiddle(tx.getText().toString());
        }

        // Address

        tx = (TextView) m_activity.findViewById(R.id.address_street_1);
        if (tx != null) {
            pd.setStreet1(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_street_2);
        if (tx != null) {
            pd.setStreet2(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_city);
        if (tx != null) {
            pd.setCity(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_colonia);
        if (tx != null) {
            pd.setColonia(tx.getText().toString());
        }

        NumberPicker picker = (NumberPicker) m_activity.findViewById(R.id.address_state);
        if (picker != null) {
            int value = picker.getValue();
            ArrayList<String> states = m_sess.getMexicanStatesList();
            pd.setState(states.get(value));
        }

        // gender and dob

        rb = (RadioButton) m_activity.findViewById(R.id.gender_female);
        if (rb != null) {
            String gender = "Female";
            if (rb.isChecked() == false) {
                gender = "Male";
            }
            pd.setGender(gender);
        }

        tx = (TextView) m_activity.findViewById(R.id.dob);
        if (tx != null) {
            pd.setDob(tx.getText().toString());
        }

        // contact info

        tx = (TextView) m_activity.findViewById(R.id.address_street_1);
        if (tx != null) {
            pd.setStreet1(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_street_2);
        if (tx != null) {
            pd.setStreet2(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_city);
        if (tx != null) {
            pd.setCity(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.address_colonia);
        if (tx != null) {
            pd.setColonia(tx.getText().toString());
        }

        /*
        tx = (TextView) m_activity.findViewById(R.id.address_state);
        if (tx != null) {
            pd.setState(tx.getText().toString());
        }
        */

        // contact info

        tx = (TextView) m_activity.findViewById(R.id.phone_1);
        if (tx != null) {
            pd.setPhone1(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.phone_2);
        if (tx != null) {
            pd.setPhone1(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.e_mail);
        if (tx != null) {
            pd.setEmail(tx.getText().toString());
        }

        // emergency contact info

        tx = (TextView) m_activity.findViewById(R.id.emergency_full_name);
        if (tx != null) {
            pd.setEmergencyFullName(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.emergency_phone);
        if (tx != null) {
            pd.setEmergencyPhone(tx.getText().toString());
        }

        tx = (TextView) m_activity.findViewById(R.id.emergency_e_mail);
        if (tx != null) {
            pd.setEmergencyEmail(tx.getText().toString());
        }

        return pd;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        copyPatientDataToUI();
        setViewDirtyListeners();
    }

    @Override
    public void onPause() {
        /*
        Activity activity = getActivity();
        if (activity != null) {
            View button_bar_item = activity.findViewById(R.id.save_button);
            if (button_bar_item != null) {
                button_bar_item.setVisibility(View.GONE);
            }
        }
        */

        super.onPause();

        PatientData pd = this.copyPatientDataFromUI();

        if (m_dirty || pd.equals(m_patientData) == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_unsaved_patient_data));
            builder.setMessage(m_activity.getString(R.string.msg_save_patient_data));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
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
/*
        View button_bar_item = getActivity().findViewById(R.id.save_button);
        button_bar_item.setVisibility(View.GONE);
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_patient_info_layout, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}