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
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.File;

public class HeadshotImage implements ImageReadyListener {
    private int m_id;
    private ImageView m_imageView;
    private Context m_context;
    private Activity m_activity;
    private ImageDataReader m_reader = null;
    private Thread m_thread;

    void setImageView(ImageView imageView) {
        m_imageView = imageView;
    }

    Thread getImage(int id) {
        m_id = id;
        if (m_reader == null) {
            m_reader = new ImageDataReader(m_context, m_id);
            m_reader.registerListener(this);
            m_thread = new Thread(){
                public void run() {
                    m_reader.read(m_id);
                }
            };
        }
        return m_thread;
    }

    String getImageFileAbsolutePath()
    {
        String ret = null;

        if (m_reader != null) {
            ret = m_reader.getImageFileAbsolutePath();
        }
        return ret;
    }

    void setActivity(Activity activity) {
        m_activity = activity;
        m_context = activity.getApplicationContext();
    }

    @Override
    public void onImageRead(final File file) {
        m_activity.runOnUiThread(new Runnable() {
            public void run() {
                int maxSize = 1024 * 1024 * 10;
                Picasso p = new Picasso.Builder(m_context).memoryCache(new LruCache(maxSize)).build();
                m_imageView.setBackgroundColor(m_activity.getResources().getColor(R.color.white));
                //Picasso.with(m_context).load(file).fit().centerInside().into(m_imageView);
                p.with(m_context).load(file).fit().centerInside().into(m_imageView);
            }
        });
    }

    @Override
    public void onImageError(int code) {
        SessionSingleton.getInstance().removeHeadShotPath(m_id);
        if (code != 404) {
            m_activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(m_activity, m_activity.getString(R.string.msg_unable_to_get_patient_headshot), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
