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

import android.content.Context;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageDataReader {
    private File m_storageDir = null;
    private SessionSingleton m_sess = SessionSingleton.getInstance();
    private Context m_context = null;
    private File m_file = null;
    private String m_imageFileName = null;
    private int m_id;                               // id of resource in DB, e.g., patient ID
    private ArrayList<ImageReadyListener> m_listener = new ArrayList<ImageReadyListener>();   // callback on success or error
    private boolean m_isCached = false;             // image data is already cached in file
    private ImageREST m_imageData;

    public ImageDataReader(Context context, int id) {
        m_context = context;
        m_id = id;
    }

    public void cancelPendingRequest(int tag)
    {
        if (m_imageData != null)
        {
            m_imageData.cancelPendingRequest(tag);
        }
    }

    public String getImageFileAbsolutePath() {
        String ret = null;
        if (m_file == null) {
            try {
                m_file = createFile();

            } catch (IOException e) {
            }
        }
        if (m_file != null) {
            ret = m_file.getAbsolutePath();
        }
        return ret;
    }

    synchronized private File createFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String idString = String.format("%d", m_id);
        m_imageFileName = "JPEG_" + idString + "_" + timeStamp + "_";

        m_file = File.createTempFile(
                m_imageFileName,                /* prefix */
                ".jpg",                         /* suffix */
                m_sess.getStorageDir()          /* directory */
        );
        return m_file;
    }

    private void onImageRead(File file) {
        for (int i = 0; i < m_listener.size(); i++) {
            m_listener.get(i).onImageRead(file);
        }
    }

    private void onImageError(int code) {
        for (int i = 0; i < m_listener.size(); i++) {
            m_listener.get(i).onImageError(code);
        }
    }

    public void read(int id)
    {
        if (m_isCached && m_file != null) {
            // notify the listener, if registered
            onImageRead(m_file);
        } else {
            if (m_context == null) {
                onImageError(500);
            }
            if (m_file == null) {
                try {
                    createFile();
                } catch(IOException e) {
                }
            }
            if (m_file == null) {
                onImageError(500);
            } else if (Looper.myLooper() != Looper.getMainLooper()) {
                m_imageData = new ImageREST(m_context);
                Object lock = m_imageData.getMostRecentPatientImageData(id, m_file);

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

                int status = m_imageData.getStatus();
                if (status == 200) {
                    onImageRead(m_file);
                } else {
                    onImageError(status);
                }
            } else {
                onImageError(500);
            }
        }
    }

    public void registerListener(ImageReadyListener listener)
    {
        m_listener.add(listener);
    }
}
