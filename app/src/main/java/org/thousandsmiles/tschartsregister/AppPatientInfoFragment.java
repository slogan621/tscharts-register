/*
 * (C) Copyright Syd Logan 2017-2019
 * (C) Copyright Thousand Smiles Foundation 2017-2019
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
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AppPatientInfoFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private Activity m_activity = null;
    private View m_view = null;
    private SessionSingleton m_sess = null;
    private PatientData m_patientData;
    private int m_patientId;
    private boolean m_isNewPatient = true;
    private boolean m_dirty = false;
    private boolean m_hasCurp = true;
    private boolean m_hasAddr = true;
    private boolean m_hasContact = true;
    private boolean m_hasEmerContact = true;

    public static AppPatientInfoFragment newInstance() {
        return new AppPatientInfoFragment();
    }

    private void setDate(final Calendar calendar) {

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String dateString = String.format("%02d-%02d-%d", month, day, year);
        ((TextView) m_view.findViewById(R.id.dob)).setText(dateString);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        setDate(c);
    }

    private boolean isValidPatientBirthDate(String dateStr)
    {
        boolean ret = false;
        Date date, today;

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        sdf.setLenient(true);
        date = sdf.parse(dateStr, new ParsePosition(0));

        if (date == null) {
            sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setLenient(true);
            date = sdf.parse(dateStr, new ParsePosition(0));
        }

        if (date != null) {
            today = new Date();
            if (date.compareTo(today) < 0) {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            m_activity = (Activity) context;
            m_sess = SessionSingleton.getInstance();
            if ((m_isNewPatient = m_sess.getIsNewPatient()) == false) {
                m_patientId = m_sess.getPatientId();
                m_patientData = m_sess.getPatientData(m_patientId);
            } else {
                // create new patient data here.
                m_patientData = m_sess.getNewPatientData();
            }
        }
    }

    public void handleNextButtonPress(View v) {

        final PatientData pd = this.copyPatientDataFromUI();
        boolean valid;

        valid = validateFields();
        if (valid == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_missing_patient_data));
            builder.setMessage(m_activity.getString(R.string.msg_please_enter_required_patient_data));

            builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else if (m_dirty || pd.equals(m_patientData) == false) {

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

    private void copyPatientDataToUI() {
        TextView tx;
        RadioButton rb;

        if (m_patientData == null) {
            return;
        }

        // CURP

        tx = (TextView) m_view.findViewById(R.id.curp);
        if (tx != null) {
            String val = m_patientData.getCURP();
            if (val.equals("") == true) {
                tx.setHint(R.string.please_enter_a_valid_curp);
            }
        }

        // Name

        tx = (TextView) m_view.findViewById(R.id.paternal_last);
        if (tx != null) {
            tx.setText(m_patientData.getFatherLast());
        }

        tx = (TextView) m_view.findViewById(R.id.maternal_last);
        if (tx != null) {
            tx.setText(m_patientData.getMotherLast());
        }

        tx = (TextView) m_view.findViewById(R.id.first_name);
        if (tx != null) {
            tx.setText(m_patientData.getFirst());
        }

        tx = (TextView) m_view.findViewById(R.id.middle_name);
        if (tx != null) {
            tx.setText(m_patientData.getMiddle());
        }

        // Address

        tx = (TextView) m_view.findViewById(R.id.address_street_1);
        if (tx != null) {
            tx.setText(m_patientData.getStreet1());
        }

        tx = (TextView) m_view.findViewById(R.id.address_street_2);
        if (tx != null) {
            tx.setText(m_patientData.getStreet2());
        }

        tx = (TextView) m_view.findViewById(R.id.address_city);
        if (tx != null) {
            tx.setText(m_patientData.getCity());
        }

        tx = (TextView) m_view.findViewById(R.id.address_colonia);
        if (tx != null) {
            tx.setText(m_patientData.getColonia());
        }

        final TextView tx1 = (TextView) m_view.findViewById(R.id.address_state);
        if (tx1 != null) {

            tx1.setText(m_patientData.getState());
            tx1.setShowSoftInputOnFocus(false);
            tx1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        MexicanStateDialogFragment mld = new MexicanStateDialogFragment();
                        mld.setPatientId(m_sess.getActivePatientId());
                        mld.setTextField(tx1);
                        mld.show(getFragmentManager(), m_activity.getString(R.string.title_mexican_state_dialog));
                    }
                }
            });
            tx1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MexicanStateDialogFragment mld = new MexicanStateDialogFragment();
                    mld.setPatientId(m_sess.getActivePatientId());
                    mld.setTextField(tx1);
                    mld.show(getFragmentManager(), m_activity.getString(R.string.title_mexican_state_dialog));
                }
            });
            tx1.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        // Gender and DOB

        String gender = m_patientData.getGender();
        boolean isFemale = true;

        if (gender.equals("Male")) {
            isFemale = false;
        }

        rb = (RadioButton) m_view.findViewById(R.id.gender_female);
        if (rb != null) {
            rb.setChecked(isFemale);
        }
        rb = (RadioButton) m_view.findViewById(R.id.gender_male);
        if (rb != null) {
            rb.setChecked(!isFemale);
        }

        tx = (TextView) m_view.findViewById(R.id.dob);
        if (tx != null) {
            tx.setText(m_patientData.getDob());
        }

        // Contact Info

        tx = (TextView) m_view.findViewById(R.id.phone_1);
        if (tx != null) {
            tx.setText(m_patientData.getPhone1());
        }

        tx = (TextView) m_view.findViewById(R.id.phone_2);
        if (tx != null) {
            tx.setText(m_patientData.getPhone2());
        }

        tx = (TextView) m_view.findViewById(R.id.e_mail);
        if (tx != null) {
            tx.setText(m_patientData.getEmail());
        }

        // Emergency Contact Info

        tx = (TextView) m_view.findViewById(R.id.emergency_phone);
        if (tx != null) {
            tx.setText(m_patientData.getEmergencyPhone());
        }

        tx = (TextView) m_view.findViewById(R.id.emergency_full_name);
        if (tx != null) {
            tx.setText(m_patientData.getEmergencyFullName());
        }

        tx = (TextView) m_view.findViewById(R.id.emergency_e_mail);
        if (tx != null) {
            tx.setText(m_patientData.getEmergencyEmail());
        }
    }

    private void setDirty() {
        m_dirty = true;
    }

    private void clearDirty() {
        m_dirty = false;
    }

    private void enableOrDisableCurpEntry(boolean enable)
    {
        TextView tx = (TextView) m_view.findViewById(R.id.curp);
        if (tx != null) {
           tx.setEnabled(enable);
        }
    }

    private void handleHasCurp(boolean isClicked)
    {
        m_hasCurp = isClicked;
        enableOrDisableCurpEntry(m_hasCurp);
    }

    private void enableOrDisableAddrEntry(boolean enable)
    {
        TextView tx = (TextView) m_view.findViewById(R.id.address_street_1);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.address_street_2);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.address_city);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.address_colonia);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.address_state);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.address_state_picker);
        if (tx != null) {
            tx.setEnabled(enable);
        }
    }

    private void handleHasAddr(boolean isClicked)
    {
        m_hasAddr = isClicked;
        enableOrDisableAddrEntry(m_hasAddr);
    }

    private void enableOrDisableContactEntry(boolean enable)
    {
        TextView tx = (TextView) m_view.findViewById(R.id.phone_1);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.phone_2);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.e_mail);
        if (tx != null) {
            tx.setEnabled(enable);
        }
    }

    private void handleHasContact(boolean isClicked)
    {
        m_hasContact = isClicked;
        enableOrDisableContactEntry(m_hasContact);
    }

    private void enableOrDisableEmergencyContactEntry(boolean enable)
    {
        TextView tx = (TextView) m_view.findViewById(R.id.emergency_full_name);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.emergency_phone);
        if (tx != null) {
            tx.setEnabled(enable);
        }
        tx = (TextView) m_view.findViewById(R.id.emergency_e_mail);
        if (tx != null) {
            tx.setEnabled(enable);
        }
    }

    private void handleHasEmergencyContact(boolean isClicked)
    {
        m_hasEmerContact = isClicked;
        enableOrDisableEmergencyContactEntry(m_hasEmerContact);
    }

    private void setViewDirtyListeners() {
        Switch sw;
        TextView tx;
        RadioButton rb;

        // CURP

        tx = (TextView) m_view.findViewById(R.id.curp);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        // name

        tx = (TextView) m_view.findViewById(R.id.paternal_last);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.maternal_last);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.first_name);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.middle_name);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.address_street_1);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.address_street_2);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.address_colonia);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.address_city);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.address_state);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        // gender and dob

        rb = (RadioButton) m_view.findViewById(R.id.gender_male);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.gender_female);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty();
                }
            });
        }

        final TextView tx1 = (TextView) m_view.findViewById(R.id.dob);
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
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.phone_1);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.phone_2);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.e_mail);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.emergency_full_name);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.emergency_phone);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        tx = (TextView) m_view.findViewById(R.id.emergency_e_mail);
        if (tx != null) {
            tx.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

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

        rb = (RadioButton) m_view.findViewById(R.id.has_curp);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    handleHasCurp(isChecked);
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.has_addr);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    handleHasAddr(isChecked);
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.has_contact);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    handleHasContact(isChecked);
                }
            });
        }

        rb = (RadioButton) m_view.findViewById(R.id.has_emergency_contact);
        if (rb != null) {
            rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    handleHasEmergencyContact(isChecked);
                }
            });
        }
    }

    private PatientData copyPatientDataFromUI() {
        Switch sw;
        TextView tx;
        RadioButton rb;
        boolean bv;
        boolean checked;

        PatientData pd;

        pd = m_patientData;

        // CURP

        tx = (TextView) m_view.findViewById(R.id.curp);
        if (tx != null) {
            pd.setCURP(tx.getText().toString());
        }

        // Name

        tx = (TextView) m_view.findViewById(R.id.paternal_last);
        if (tx != null) {
            pd.setFatherLast(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.maternal_last);
        if (tx != null) {
            pd.setMotherLast(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.first_name);
        if (tx != null) {
            pd.setFirst(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.middle_name);
        if (tx != null) {
            pd.setMiddle(tx.getText().toString());
        }

        // Address

        tx = (TextView) m_view.findViewById(R.id.address_street_1);
        if (tx != null) {
            pd.setStreet1(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_street_2);
        if (tx != null) {
            pd.setStreet2(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_city);
        if (tx != null) {
            pd.setCity(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_colonia);
        if (tx != null) {
            pd.setColonia(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_state);
        if (tx != null) {
            pd.setState(tx.getText().toString());
        }

        // gender and dob

        rb = (RadioButton) m_view.findViewById(R.id.gender_female);
        if (rb != null) {
            String gender = "Female";
            if (rb.isChecked() == false) {
                gender = "Male";
            }
            pd.setGender(gender);
        }

        tx = (TextView) m_view.findViewById(R.id.dob);
        if (tx != null) {
            pd.setDob(tx.getText().toString());
        }

        // contact info

        tx = (TextView) m_view.findViewById(R.id.address_street_1);
        if (tx != null) {
            pd.setStreet1(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_street_2);
        if (tx != null) {
            pd.setStreet2(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_city);
        if (tx != null) {
            pd.setCity(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_colonia);
        if (tx != null) {
            pd.setColonia(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.address_state);
        if (tx != null) {
            pd.setState(tx.getText().toString());
        }

        // contact info

        tx = (TextView) m_view.findViewById(R.id.phone_1);
        if (tx != null) {
            pd.setPhone1(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.phone_2);
        if (tx != null) {
            pd.setPhone2(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.e_mail);
        if (tx != null) {
            pd.setEmail(tx.getText().toString());
        }

        // emergency contact info

        tx = (TextView) m_view.findViewById(R.id.emergency_full_name);
        if (tx != null) {
            pd.setEmergencyFullName(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.emergency_phone);
        if (tx != null) {
            pd.setEmergencyPhone(tx.getText().toString());
        }

        tx = (TextView) m_view.findViewById(R.id.emergency_e_mail);
        if (tx != null) {
            pd.setEmergencyEmail(tx.getText().toString());
        }

        return pd;
    }

    private boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean validateFields()
    {
        boolean ret = true;
        TextView tx1;
        TextView tx2;
        TextView tx3;
        ArrayList<Integer> list = new ArrayList();

        tx1 =(TextView)m_view.findViewById(R.id.curp);
        tx1.setError(null);

        if(tx1.getText().toString().equals("") && m_hasCurp == true)
        {
            ret = false;
            list.add(R.id.curp);
        }

        tx1 = (TextView)m_view.findViewById(R.id.paternal_last);
        tx1.setError(null);
        tx2 = (TextView)m_view.findViewById(R.id.maternal_last);
        tx2.setError(null);

        if(tx1.getText().toString().equals("") && tx2.getText().toString().equals(""))
        {
            ret = false;
            if (tx1.getText().toString().equals("")) {
                list.add(R.id.paternal_last);
            }
            if (tx2.getText().toString().equals("")) {
                list.add(R.id.maternal_last);
            }
        }

        tx1 =(TextView)m_view.findViewById(R.id.first_name);
        tx1.setError(null);
        if (tx1.getText().toString().equals("")) {
            ret = false;
            list.add(R.id.first_name);
        }

        // Address

        tx1 =(TextView)m_view.findViewById(R.id.address_street_1);
        tx1.setError(null);
        tx2 =(TextView)m_view.findViewById(R.id.address_street_2);
        tx2.setError(null);
        if(m_hasAddr == true && (tx1.getText().toString().equals("") && tx2.getText().toString().equals("")))
        {
            ret = false;
            if (tx1.getText().toString().equals("")) {
                list.add(R.id.address_street_1);
            }
            if (tx2.getText().toString().equals("")) {
                list.add(R.id.address_street_2);
            }
        }

        tx1 =(TextView)m_view.findViewById(R.id.address_city);
        tx1.setError(null);
        if(m_hasAddr == true && tx1.getText().toString().equals(""))
        {
            ret = false;
            list.add(R.id.address_city);
        }

        tx1 =(TextView)m_view.findViewById(R.id.address_state);
        tx1.setError(null);
        if(m_hasAddr == true && tx1.getText().toString().equals(""))
        {
            ret = false;
            list.add(R.id.address_state);
        }

        tx1 =(TextView)m_view.findViewById(R.id.dob);
        tx1.setError(null);
        if(tx1.getText().toString().equals("") || isValidPatientBirthDate(tx1.getText().toString()) == false)
        {
            ret = false;
            list.add(R.id.dob);
        }

        // contact info

        tx1 =(TextView)m_view.findViewById(R.id.phone_1);
        tx1.setError(null);
        tx2 =(TextView)m_view.findViewById(R.id.phone_2);
        tx2.setError(null);
        tx3 =(TextView)m_view.findViewById(R.id.e_mail);
        tx3.setError(null);
        if(m_hasContact == true && tx1.getText().toString().equals("") && tx2.getText().toString().equals("") && tx3.getText().toString().equals(""))
        {
            ret = false;
            if (tx1.getText().toString().equals("")) {
                list.add(R.id.phone_1);
            }
            if (tx2.getText().toString().equals("")) {
                list.add(R.id.phone_2);
            }
            if (tx3.getText().toString().equals("")) {
                list.add(R.id.e_mail);
            } else if (isValidEmail(tx3.getText().toString()) == false) {
                list.add(R.id.e_mail);
            }
        }

        tx1 =(TextView)m_view.findViewById(R.id.emergency_full_name);
        tx1.setError(null);
        if(m_hasEmerContact == true && tx1.getText().toString().equals(""))
        {
            ret = false;
            list.add(R.id.emergency_full_name);
        }

        tx1 =(TextView)m_view.findViewById(R.id.emergency_phone);
        tx1.setError(null);
        tx2 =(TextView)m_view.findViewById(R.id.emergency_e_mail);
        tx2.setError(null);
        if(m_hasEmerContact == true && tx1.getText().toString().equals("") && tx2.getText().toString().equals("") && tx3.getText().toString().equals(""))
        {
            ret = false;
            if (tx1.getText().toString().equals("")) {
                list.add(R.id.emergency_phone);
            }
            if (tx2.getText().toString().equals("")) {
                list.add(R.id.emergency_e_mail);
            } else if (isValidEmail(tx3.getText().toString()) == false) {
                list.add(R.id.emergency_e_mail);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            EditText v = (EditText) m_view.findViewById(list.get(i));
            if (v != null) {
                v.setError(m_activity.getString(R.string.msg_this_field_is_required));
            }
        }
        return ret;
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
        if (m_view != null) {
            TextView tx = (TextView) m_view.findViewById(R.id.phone_1);
            tx.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            tx = (TextView) m_view.findViewById(R.id.phone_2);
            tx.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            tx = (TextView) m_view.findViewById(R.id.emergency_phone);
            tx.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_patient_info_layout, container, false);
        m_view = view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}