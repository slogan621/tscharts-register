/*
 * (C) Copyright Syd Logan 2020
 * (C) Copyright Thousand Smiles Foundation 2020
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

import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CURPWebViewClient extends WebViewClient {

    @Override
    public void onPageFinished(WebView view, String url)
    {
        //super.onPageFinished(view, url);
        SessionSingleton sess = SessionSingleton.getInstance();
        String curp = sess.getPatientData(sess.getActivePatientId()).getCURP();
        String js = String.format("javascript:(function f() {document.getElementById(\"curpinput\").value = \"%s\";document.getElementById(\"curpinput\").readOnly=true;})()", curp);
        view.loadUrl(js);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
    {

    }
}
