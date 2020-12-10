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

import android.content.Context;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.thousandsmiles.tscharts_lib.CommonSessionSingleton;
import org.thousandsmiles.tscharts_lib.PatientData;

import java.util.ArrayList;

public class CURPWebViewClient extends WebViewClient {

    private boolean m_hasCurp = false;

    public void setHasCurp(boolean val) {
        m_hasCurp = val;
    }

    @Override
    public void onPageFinished(WebView view, String url)
    {
        super.onPageFinished(view, url);
        SessionSingleton sess = SessionSingleton.getInstance();
        String curp = "";
        String first = "";
        String motherLast = "";
        String fatherLast = "";
        String birthDay = "";
        String birthMonth = "";
        String birthYear = "";
        String gender = "H";

        String js;
        PatientData data = sess.getPatientData(sess.getActivePatientId());

        if (m_hasCurp == true) {
            curp = data.getCURP();
        } else {
            first = data.getFirst();
            motherLast = data.getMotherLast();
            fatherLast = data.getFatherLast();
            String dob = data.getDob();
            dob = data.fromDobMilitary(sess.getContext(), dob);
            String delims = "[/]";

            String[] tokens = dob.split(delims);
            birthMonth = tokens[0];
            birthDay = tokens[1];
            birthYear = tokens[2];
        }

        if (m_hasCurp == true && curp.equals("") == false) {
            js = String.format("javascript:(function f() {document.getElementById(\"curpinput\").value = \"%s\";document.getElementById(\"curpinput\").readOnly=true;})()", curp);
        } else {
            js = String.format("javascript:(function f() {document.querySelector('[data-ember-action-270]').click(); document.getElementById(\"nombre\").value = \"%s\";document.getElementById(\"primerApellido\").value = \"%s\";document.getElementById(\"segundoApellido\").value = \"%s\";document.getElementById(\"diaNacimiento\").value = \"%s\";document.getElementById(\"mesNacimiento\").value = \"%s\";document.getElementById(\"selectedYear\").value = \"%s\";document.getElementById(\"sexo\").value = \"%s\";})()", first, fatherLast, motherLast, birthDay, birthMonth, birthYear, gender);
        }

        view.loadUrl(js);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
    {
        super.onReceivedError(view, request, error);
    }
}
