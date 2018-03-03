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

import org.json.JSONException;
import org.json.JSONObject;

public class ReturnToClinic {
    private int m_id;
    private int m_clinic;
    private int m_station;
    private int m_patient;
    private int m_interval;
    private int m_month;
    private int m_year;
    private String m_comment;

    public ReturnToClinic(JSONObject o) {
        if (o != null) {
            fromJSONObject(o);
        }
    }

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public int getClinic() {
        return m_clinic;
    }

    public void setClinic(int clinic) {
        m_clinic = clinic;
    }

    public int getStation() {
        return m_station;
    }

    public void setStation(int station) {
        m_station = station;
    }

    public int getPatient() {
        return m_patient;
    }

    public void setPatient(int patient) {
        m_patient = patient;
    }

    public int getInterval() {
        return m_interval;
    }

    public void setInterval(int interval) {
        m_interval = interval;
    }

    public int getMonth() {
        return m_month;
    }

    public void setMonth(int month) {
        m_month = month;
    }

    public int getYear() {
        return m_year;
    }

    public void setYear(int year) {
        m_year = year;
    }

    public String getComment() {
        return m_comment;
    }

    public void setComment(String comment) {
        m_comment = comment;
    }

    public int fromJSONObject(JSONObject o)
    {
        int ret = 0;

        try {
            this.setId(o.getInt("id"));
            this.setClinic(o.getInt("clinic"));
            this.setComment(o.getString("comment"));
            this.setInterval(o.getInt("interval"));
            this.setMonth(o.getInt("month"));
            this.setPatient(o.getInt("patient"));
            this.setStation(o.getInt("station"));
            this.setYear(o.getInt("year"));

        } catch (JSONException e) {
            ret = -1;
        }
        return ret;
    }
}
