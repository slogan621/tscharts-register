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

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AppPatientPhotoFragment extends Fragment {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private int m_patientId;
    private boolean m_isNewPatient = true;
    private boolean m_dirty = false;
    private ImageView m_mainImageView = null;
    private ImageView m_buttonImageView = null;
    private String m_currentPhotoPath = "";
    private String m_photo1Path = "";
    private String m_photo2Path = "";
    private String m_photo3Path = "";
    private String m_clickedPhotoPath = "";
    private File m_photo1;
    private File m_photo2;
    private File m_photo3;
    static final int REQUEST_TAKE_PHOTO = 1;
    private int m_whichCamera = 0;

    public static AppPatientPhotoFragment newInstance() {
        return new AppPatientPhotoFragment();
    }

    public void restorePhotoPath()
    {
        m_sess.setPhotoPath(m_currentPhotoPath);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
            m_sess = SessionSingleton.getInstance();
            if ((m_isNewPatient = m_sess.getIsNewPatient()) == false) {
                m_patientId = m_sess.getPatientId();
            }
        }
    }

    private File createImageFile(int which) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = m_activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        if (which == 1) {
            m_photo1 = image;
            m_photo1Path = image.getAbsolutePath();
        } else if (which == 2) {
            m_photo2 = image;
            m_photo2Path = image.getAbsolutePath();
        } else {
            m_photo3 = image;
            m_photo3Path = image.getAbsolutePath();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            try {
                File file = new File(m_clickedPhotoPath);
                ImageView v;
                if (m_whichCamera == 1) {
                    v = (ImageView) m_activity.findViewById(R.id.headshot_image_1);
                    v.setClickable(true);
                } else if (m_whichCamera == 2) {
                    v = (ImageView) m_activity.findViewById(R.id.headshot_image_2);
                    v.setClickable(true);
                } else if (m_whichCamera == 3) {
                    v = (ImageView) m_activity.findViewById(R.id.headshot_image_3);
                    v.setClickable(true);
                }
                Picasso.with(getContext()).load(file).into(m_buttonImageView);
             } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchTakePictureIntent(int which) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(m_activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            String photoPath = null;
            if (which == 1) {
                photoFile = m_photo1;
            } else if (which == 2) {
                photoFile = m_photo2;
            } else {
                photoFile = m_photo3;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(m_activity,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                m_whichCamera = which; // would pass this as an extra but doesn't work
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void handleImageButton1Press(View v) {
        m_buttonImageView = (ImageView)  m_activity.findViewById(R.id.headshot_image_1);
        m_clickedPhotoPath = m_photo1Path;
        dispatchTakePictureIntent(1);
    }

    public void handleImageButton2Press(View v) {
        m_buttonImageView = (ImageView)  m_activity.findViewById(R.id.headshot_image_2);
        m_clickedPhotoPath = m_photo2Path;
        dispatchTakePictureIntent(2);
    }

    public void handleImageButton3Press(View v) {
        m_buttonImageView = (ImageView) m_activity.findViewById(R.id.headshot_image_3);
        m_clickedPhotoPath = m_photo3Path;
        dispatchTakePictureIntent(3);
    }

    private void displayMainImage(int which) {
        File file = null;

        if (which == 1) {
            file = m_photo1;
            m_sess.setPhotoPath(m_photo1Path);
        } else if (which == 2) {
            file = m_photo2;
            m_sess.setPhotoPath(m_photo2Path);
        } else if (which == 3) {
            file = m_photo3;
            m_sess.setPhotoPath(m_photo3Path);
        }

        if (file != null) {
            Picasso.with(getContext()).load(file).into(m_mainImageView);
        }
    }

    public void handleImage1Press(View v) {
        displayMainImage(1);
    }

    public void handleImage2Press(View v) {
        displayMainImage(2);
    }

    public void handleImage3Press(View v) {
        displayMainImage(3);
    }

    public void handleNextButtonPress(View v) {
        if (m_dirty) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_unsaved_patient_photo));
            builder.setMessage(m_activity.getString(R.string.msg_save_patient_photo));

            builder.setPositiveButton(m_activity.getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //m_sess.updatePatientData(pd);
                    startActivity(new Intent(m_activity, WaiverActivity.class));
                    m_activity.finish();
                }
            });

            builder.setNegativeButton(m_activity.getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(m_activity, WaiverActivity.class));
                    m_activity.finish();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else if (m_sess.getPhotoPath().equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(m_activity.getString(R.string.title_missing_photo));
            builder.setMessage(m_activity.getString(R.string.msg_please_take_headshot_of_patient));

            builder.setPositiveButton(m_activity.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        } else {
            startActivity(new Intent(m_activity, WaiverActivity.class));
            m_activity.finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        try {
            createImageFile(1);
            createImageFile(2);
            createImageFile(3);
        } catch (IOException e) {
        }
        m_currentPhotoPath = m_sess.getPhotoPath();
    }

    @Override
    public void onResume() {
        super.onResume();

        m_mainImageView = (ImageView)  m_activity.findViewById(R.id.headshot_image_main);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (m_dirty) {
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
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        ImageView v = (ImageView) m_activity.findViewById(R.id.headshot_image_1);
        if (v != null) {
            v.setClickable(false);
        }
        v = (ImageView) m_activity.findViewById(R.id.headshot_image_2);
        if (v != null) {
            v.setClickable(false);
        }
        v = (ImageView) m_activity.findViewById(R.id.headshot_image_3);
        if (v != null) {
            v.setClickable(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.app_patient_photo_layout, container, false);
        return view;
    }
}