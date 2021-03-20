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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class AppPatientPhotoFragment extends Fragment {
    private Activity m_activity = null;
    private SessionSingleton m_sess = null;
    private boolean m_dirty = false;
    private ImageView m_mainImageView = null;
    private ImageView m_buttonImageView = null;
    private String m_currentPhotoPath = "";
    private PhotoFile m_photo1;
    private PhotoFile m_photo2;
    private PhotoFile m_photo3;
    private PhotoFile m_tmpPhoto;     // used to hold result of camera, copied on success to corresponding m_photo[123]
    static final int REQUEST_TAKE_PHOTO = 1;
    private int m_whichCamera;

    private class PhotoFile {
        private File m_file = null;
        private String m_path = "";
        int m_headshotImage = 0;

        private void activate()
        {
            m_sess.getCommonSessionSingleton().setPhotoPath(m_path);
        }

        public void onPhotoTaken() {
            ImageView v;
            setToCopyOfFile(m_tmpPhoto.getFile());
            v = (ImageView) m_activity.findViewById(m_headshotImage);
            if (v != null) {
                v.setClickable(true);
                Picasso.get().load(m_file).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(m_buttonImageView);
            }
        }

        public void setHeadshotImage(int id) {
            m_headshotImage = id;
        }

        public File getFile()
        {
            return m_file;
        }

        public void setToCopyOfFile(File file)
        {
            try {
                copyInputStreamToFile(new FileInputStream(file), m_file);
                m_file.setLastModified(file.lastModified());
            } catch (IOException e) {
            }
        }

        public void copyInputStreamToFile(final InputStream in, final File dest)
                throws IOException
        {
            copyInputStreamToOutputStream(in, new FileOutputStream(dest));
        }


        public void copyInputStreamToOutputStream(final InputStream in,
                                                  final OutputStream out) throws IOException {
            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int n;
                    while ((n = in.read(buffer)) != -1)
                        out.write(buffer, 0, n);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        }

        private void create()
        {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = m_activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                m_file = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
                );

                // Save a file: path for use with ACTION_VIEW intents
                m_path = m_file.getAbsolutePath();
            }
            catch (java.io.IOException e) {
                m_file = null;
                m_path = "";
            }
        }

        private void displayMainImage()
        {
            Picasso.get().load(m_file).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(m_mainImageView);
        }

        public void selectImage()
        {
            if (m_file != null) {
                activate();
                displayMainImage();
            }
        }

        public void dispatchTakePictureIntent() {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(m_activity.getPackageManager()) != null) {
                if (m_file != null) {
                    Uri photoURI = FileProvider.getUriForFile(m_activity,
                        "org.thousandsmiles.tschartsregister.android.fileprovider",
                        m_file);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    public static AppPatientPhotoFragment newInstance() {
        return new AppPatientPhotoFragment();
    }

    public void restorePhotoPath()
    {
        m_sess.getCommonSessionSingleton().setPhotoPath(m_currentPhotoPath);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            m_activity=(Activity) context;
            m_sess = SessionSingleton.getInstance();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (m_whichCamera == 1) {
                m_photo1.onPhotoTaken();
            } else if (m_whichCamera == 2) {
                m_photo2.onPhotoTaken();
            } else if (m_whichCamera == 3) {
                m_photo3.onPhotoTaken();
            }
        }
    }

    public void handleImageButton1Press(View v) {
        m_buttonImageView = (ImageView)  m_activity.findViewById(R.id.headshot_image_1);
        m_whichCamera = 1;
        m_tmpPhoto.dispatchTakePictureIntent();
    }

    public void handleImageButton2Press(View v) {
        m_buttonImageView = (ImageView)  m_activity.findViewById(R.id.headshot_image_2);
        m_whichCamera = 2;
        m_tmpPhoto.dispatchTakePictureIntent();
    }

    public void handleImageButton3Press(View v) {
        m_buttonImageView = (ImageView) m_activity.findViewById(R.id.headshot_image_3);
        m_whichCamera = 3;
        m_tmpPhoto.dispatchTakePictureIntent();
    }

    public void handleImage1Press(View v) {
        m_photo1.selectImage();
    }

    public void handleImage2Press(View v) {
        m_photo2.selectImage();
    }

    public void handleImage3Press(View v) {
        m_photo3.selectImage();
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
        } else if (m_sess.getCommonSessionSingleton().getPhotoPath() == null || m_sess.getCommonSessionSingleton().getPhotoPath().equals("")) {
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
        m_photo1 = new PhotoFile();
        m_photo1.create();
        m_photo1.setHeadshotImage(R.id.headshot_image_1);
        m_photo2 = new PhotoFile();
        m_photo2.create();
        m_photo2.setHeadshotImage(R.id.headshot_image_2);
        m_photo3 = new PhotoFile();
        m_photo3.create();
        m_photo3.setHeadshotImage(R.id.headshot_image_3);
        m_tmpPhoto = new PhotoFile();
        m_tmpPhoto.create();
        m_currentPhotoPath = m_sess.getCommonSessionSingleton().getPhotoPath();
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