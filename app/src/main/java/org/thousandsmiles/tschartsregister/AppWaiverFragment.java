/*
 * (C) Copyright Syd Logan 2018
 * (C) Copyright Thousand Smiles Foundation 2018
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;

public class AppWaiverFragment extends Fragment implements RESTCompletionListener, OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private boolean m_dirty = false;
    public static final String SAMPLE_FILE = "sample.pdf";
    String pdfFileName;
    PDFView pdfView;
    int pageNumber;
    AppWaiverFragment m_this = this;
    Boolean m_creating = false;

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
        showFailure(code, msg);
    }


    @Override
    public void onSuccess(int code, String msg)
    {
        if (m_creating == true) {
            m_creating = false;
            m_sess.createMedicalHistory(this);
        } else {
            showSuccess();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(m_activity.getString(R.string.title_successful_registration));
        builder.setMessage(m_activity.getString(R.string.msg_successfully_registered_patient));

        builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(m_activity, PatientSearchActivity.class));
                m_activity.finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showFailure(int code, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(m_activity.getString(R.string.title_failed_registration));
        String msgStr = String.format("%s\ncode: %d msg: %s", m_activity.getString(R.string.msg_failed_to_register_patient), code, msg);
        builder.setMessage(msgStr);

        builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void handleRegisterButtonPress(View v) {
        if (true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_confirm_registration));
            builder.setMessage(m_activity.getString(R.string.msg_register_patient));

            m_this = this;

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (m_sess.getIsNewPatient() == true) {
                        m_creating = true;
                        m_sess.createNewPatient(m_this);
                    } else {
                        m_sess.updatePatientData(m_sess.getPatientId()) ;
                        m_sess.updateMedicalHistory();
                    }
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

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView = (PDFView) getActivity().findViewById(R.id.pdfView);

        if (pdfView != (PDFView) null) {

            pdfView.fromAsset(SAMPLE_FILE)
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
            enable = true;
        }
        setRegisterButtonEnabled(enable);
    }

    public void onWaiverPhotoClicked(View v) {
        // TBD
    }

    private void setRegisterButtonEnabled(boolean enable)
    {
        Button b = (Button) m_activity.findViewById(R.id.register_button);
        b.setEnabled(enable);
    }

    void updatePhoto()
    {
        /*
        boolean ret = false;

        Thread thread = new Thread(){
            public void run() {
                // note we use session context because this may be called after onPause()
                MedicalHistoryREST rest = new MedicalHistoryREST(m_sess.getContext());
                Object lock = null;
                int status;

                lock = rest.updateMedicalHistory(copyMedicalHistoryDataFromUI());

                synchronized (lock) {
                    // we loop here in case of race conditions or spurious interrupts
                    while (true) {
                        try {
                            lock.wait();
                            break;
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
                status = rest.getStatus();
                if (status != 200) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_save_medical_history), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(m_activity, m_activity.getString(R.string.msg_successfully_saved_medical_history), Toast.LENGTH_LONG).show();
                        }
                    });
                }
           }
        };
        thread.start();
        */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        displayFromAsset(SAMPLE_FILE);
        CheckBox cb = (CheckBox) m_activity.findViewById(R.id.waiver_acknowledgement);
        cb.setChecked(false);
        setRegisterButtonEnabled(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
   }
}