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

public class Registration {

    private int m_patientId;
    private int m_category;
    private String m_categoryName;

    public String getCategoryName() {
        return m_categoryName;
    }

    public void setCategoryName(String categoryName) {
        m_categoryName = categoryName;
    }

    public int getPatientId() {
        return m_patientId;
    }

    public void setPatientId(int patientId) {
        m_patientId = patientId;
    }

    public int getCategory() {
        return m_category;
    }

    public void setCategory(int category) {
        m_category = category;
    }
}
