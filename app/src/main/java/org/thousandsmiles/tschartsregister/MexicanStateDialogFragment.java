/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
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
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.thousandsmiles.tscharts_lib.PatientData;

import java.util.ArrayList;

public class MexicanStateDialogFragment extends DialogFragment {

    private int m_patientId;
    private View m_view;
    PatientData m_patientData;
    private TextView m_textView;
    private NumberPicker m_picker;
    private String[] m_stateNames;
    private SessionSingleton m_sess = SessionSingleton.getInstance();

    private void getPatientData(int id) {
        m_patientData = m_sess.getPatientData(id);
    }

    public void setPatientId(int id) {
        m_patientId = id;
        getPatientData(id);
    }

    public void setTextField(TextView view)
    {
        m_textView = view;
    }

    private String getTextField()
    {
        String ret = new String();

        if (m_textView != null) {
            ret = m_textView.getText().toString();
        }
        return ret;
    }

    private String getSelectedState()
    {
        int selected = m_picker.getValue();
        String state = m_stateNames[selected];
        return state;
    }

    private void setTextField(String str)
    {
        if (m_textView != null) {
            m_textView.setText(str);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        m_view = inflater.inflate(R.layout.select_state_dialog, null);
        m_picker = (NumberPicker) m_view.findViewById(R.id.address_state_picker);
        String str = getTextField();

        ArrayList<String> mexicanStates = m_sess.getMexicanStatesList();
        m_stateNames = mexicanStates.toArray(new String[0]);
        if (mexicanStates != null && mexicanStates.size() > 0) {
            if (m_picker != null) {
                String choice;
                if (str.length() > 0) {
                    choice = str;           // aleady has a value
                } else {
                    choice = m_patientData.getState();  // get configured value, if any
                }
                m_picker.setMinValue(0);
                m_picker.setMaxValue(mexicanStates.size() - 1);
                m_picker.setDisplayedValues(m_stateNames);
                for (int i = 0; i < mexicanStates.size(); i++) {
                    if (choice.equals(mexicanStates.get(i))) {
                        m_picker.setValue(i);
                        break;
                    }
                }
            }
        }

        builder.setView(m_view)
                // Add action buttons
                .setPositiveButton(R.string.select_state_select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String state = getSelectedState();
                        setTextField(state);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.select_state_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        Dialog ret = builder.create();
        ret.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ret.setCanceledOnTouchOutside(false);
        ret.setCancelable(false);
        ret.setTitle(R.string.title_select_state_dialog);
        return ret;
    }
}