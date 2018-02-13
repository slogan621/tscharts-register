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
import org.json.JSONException;
import org.json.JSONObject;

public class PatientData {
    private int m_id;
    private String m_fatherLast = "";
    private String m_motherLast = "";
    private String m_first = "";
    private String m_middle = "";
    private String m_dob = "";
    private String m_gender = "";
    private String m_street1 = "";
    private String m_street2 = "";
    private String m_colonia = "";
    private String m_city = "";
    private String m_state = "";
    private String m_phone1 = "";
    private String m_phone2 = "";
    private String m_email = "";
    private String m_emergencyFullName = "";
    private String m_emergencyPhone = "";
    private String m_emergencyEmail = "";
    private boolean m_valid;

    public PatientData() {
        m_id = -1;
        m_fatherLast = "";
        m_motherLast = "";
        m_first = "";
        m_middle = "";
        m_dob = "";
        m_gender = "Female";
        m_street1 = "";
        m_street2 = "";
        m_colonia = "";
        m_city = "";
        m_state = "Baja California";
        m_phone1 = "";
        m_phone2 = "";
        m_email = "";
        m_emergencyFullName = "";
        m_emergencyPhone = "";
        m_emergencyEmail = "";
        m_valid = true;
    }

    public PatientData(JSONObject o) {
        m_valid = false;
        if (o != null) {
            try {
                m_id = o.getInt("id");
                m_fatherLast = o.getString("paternal_last");
                m_motherLast = o.getString("maternal_last");
                m_first = o.getString("first");
                m_middle = o.getString("middle");
                m_dob = o.getString("dob");
                m_gender = o.getString("gender");
                m_street1 = o.getString("street1");
                m_street2 = o.getString("street2");
                m_colonia = o.getString("colonia");
                m_city = o.getString("city");
                m_state = o.getString("state");
                m_phone1 = o.getString("phone1");
                m_phone2 = o.getString("phone2");
                m_email = o.getString("email");
                m_emergencyFullName = o.getString("emergencyfullname");
                m_emergencyPhone = o.getString("emergencyphone");
                m_emergencyEmail = o.getString("emergencyemail");
                m_valid = true;
            } catch (JSONException e) {
            }
        }
    }

    public int getId() {
        return m_id;
    }

    public void setId(int id) {
        m_id = id;
    }

    public String getFatherLast() {
        return m_fatherLast;
    }

    public void setFatherLast(String fatherLast) {
        m_fatherLast = fatherLast;
    }

    public String getMotherLast() {
        return m_motherLast;
    }

    public void setMotherLast(String motherLast) {
        m_motherLast = motherLast;
    }

    public String getFirst() {
        return m_first;
    }

    public void setFirst(String first) {
        m_first = first;
    }

    public String getMiddle() {
        return m_middle;
    }

    public void setMiddle(String middle) {
        m_middle = middle;
    }

    public String getDob() {
        return m_dob;
    }

    public void setDob(String dob) {
        m_dob = dob;
    }

    public String getGender() {
        return m_gender;
    }

    public void setGender(String gender) {
        m_gender = gender;
    }


    public String getStreet1() {
        return m_street1;
    }

    public void setStreet1(String street1) {
        m_street1 = street1;
    }

    public String getStreet2() {
        return m_street2;
    }

    public void setStreet2(String street2) {
        m_street2 = street2;
    }

    public String getColonia() {
        return m_colonia;
    }

    public void setColonia(String colonia) {
        m_colonia = colonia;
    }

    public String getCity() {
        return m_city;
    }

    public void setCity(String city) {
        m_city = city;
    }

    public String getState() {
        return m_state;
    }

    public void setState(String state) {
        m_state = state;
    }

    public String getPhone1() {
        return m_phone1;
    }

    public void setPhone1(String phone1) {
        m_phone1 = phone1;
    }

    public String getPhone2() {
        return m_phone2;
    }

    public void setPhone2(String phone2) {
        m_phone2 = phone2;
    }

    public String getEmail() {
        return m_email;
    }

    public void setEmail(String email) {
        m_email = email;
    }

    public String getEmergencyFullName() {
        return m_emergencyFullName;
    }

    public void setEmergencyFullName(String emergencyFullName) {
        m_emergencyFullName = emergencyFullName;
    }

    public String getEmergencyPhone() {
        return m_emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        m_emergencyPhone = emergencyPhone;
    }

    public String getEmergencyEmail() {
        return m_emergencyEmail;
    }

    public void setEmergencyEmail(String emergencyEmail) {
        m_emergencyEmail = emergencyEmail;
    }

    public boolean getValid()
    {
        return m_valid;
    }
}

