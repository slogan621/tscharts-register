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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!PatientData.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final PatientData other = (PatientData) obj;

        m_valid = true;

        if (this.m_fatherLast != other.m_fatherLast) {
            return false;
        }

        if (this.m_motherLast != other.m_motherLast) {
            return false;
        }

        if (this.m_first != other.m_first) {
            return false;
        }

        if (this.m_middle != other.m_middle) {
            return false;
        }

        if (this.m_dob != other.m_dob) {
            return false;
        }

        if (this.m_gender != other.m_gender) {
            return false;
        }

        if (this.m_street1 != other.m_street1) {
            return false;
        }

        if (this.m_street2 != other.m_street2) {
            return false;
        }

        if (this.m_colonia != other.m_colonia) {
            return false;
        }

        if (this.m_city != other.m_city) {
            return false;
        }

        if (this.m_state != other.m_state) {
            return false;
        }

        if (this.m_phone1 != other.m_phone1) {
            return false;
        }

        if (this.m_phone2 != other.m_phone2) {
            return false;
        }

        if (this.m_email != other.m_email) {
            return false;
        }

        if (this.m_emergencyFullName != other.m_emergencyFullName) {
            return false;
        }

        if (this.m_emergencyPhone != other.m_emergencyPhone) {
            return false;
        }

        if (this.m_emergencyEmail != other.m_emergencyEmail) {
            return false;
        }

        return true;
    }

    public int fromJSONObject(JSONObject o)
    {
        int ret = 0;

        try {
            setId(o.getInt("id"));
            setFatherLast(o.getString("paternal_last"));
            setMotherLast(o.getString("maternal_last"));
            setFirst(o.getString("first"));
            setMiddle(o.getString("middle"));
            setDob(o.getString("dob"));
            setGender(o.getString("gender"));
            setStreet1(o.getString("street1"));
            setStreet2(o.getString("street2"));
            setColonia(o.getString("colonia"));
            setCity(o.getString("city"));
            setState(o.getString("state"));
            setPhone1(o.getString("phone1"));
            setPhone2(o.getString("phone2"));
            setEmail(o.getString("email"));
            setEmergencyFullName(o.getString("emergencyfullname"));
            setEmergencyPhone(o.getString("emergencyphone"));
            setEmergencyEmail(o.getString("emergencyemail"));
        } catch (JSONException e) {
            ret = -1;
        }
        return ret;
    }

    public JSONObject toJSONObject()
    {
        JSONObject data = new JSONObject();
        try {
            if (SessionSingleton.getInstance().getIsNewPatient() == false) {
                data.put("id", getId());
            }
            data.put("paternal_last", getFatherLast());
            data.put("maternal_last", getMotherLast());
            data.put("first", getFirst());
            data.put("middle", getMiddle());
            data.put("dob", getDob());
            data.put("gender", getGender());
            data.put("street1", getStreet1());
            data.put("street2", getStreet2());
            data.put("colonia", getColonia());
            data.put("city", getCity());
            data.put("state", getState());
            data.put("phone1", getPhone1());
            data.put("phone2", getPhone2());
            data.put("email", getEmail());
            data.put("emergencyfullname", getEmergencyFullName());
            data.put("emergencyphone", getEmergencyPhone());
            data.put("emergencyemail", getEmergencyEmail());

            // we don't current support the following but the backend requires them

            data.put("prefix", "");
            data.put("suffix", "");
        } catch(Exception e) {
            // not sure this would ever happen, ignore. Continue on with the request with the expectation it fails
            // because of the bad JSON sent
            data = null;
        }
        return data;
    }
}

