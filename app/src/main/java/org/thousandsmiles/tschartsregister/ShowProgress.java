/*
 * (C) Copyright Syd Logan 2021
 * (C) Copyright Thousand Smiles Foundation 2021
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class ShowProgress {
    public void showProgress(final Activity activity, final View outer, final View progress, final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    int shortAnimTime = activity.getResources().getInteger(android.R.integer.config_shortAnimTime);

                    if (outer != null) {
                        outer.setVisibility(show ? View.GONE : View.VISIBLE);
                        outer.animate().setDuration(shortAnimTime).alpha(
                                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                outer.setVisibility(show ? View.GONE : View.VISIBLE);
                            }
                        });
                    }

                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                    progress.animate().setDuration(shortAnimTime).alpha(
                            show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progress.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });
                } else {
                    // The ViewPropertyAnimator APIs are not available, so simply show
                    // and hide the relevant UI components.
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                    if (outer != null) {
                        outer.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                }
            }
        });
    }
}
