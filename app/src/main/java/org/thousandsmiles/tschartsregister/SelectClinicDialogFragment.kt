/*
 * (C) Copyright Syd Logan 2022
 * (C) Copyright Thousand Smiles Foundation 2022
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

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.thousandsmiles.tscharts_lib.CommonSessionSingleton
import org.thousandsmiles.tscharts_lib.RESTCompletionListener
import java.util.*

class SelectClinicDialogFragment : DialogFragment(), RESTCompletionListener {
    private val m_sess = SessionSingleton.getInstance()
    private var m_view: View? = null

    private fun confirmClinicSelection(cs: JSONObject) {
        val id: Int
        val name: String
        try {
            name = cs.getString("name")
            id = cs.getInt("id")
        } catch (e: JSONException) {
            Toast.makeText(getActivity(),
                R.string.msg_unable_process_category,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val alertDialogBuilder = getContext()?.let { AlertDialog.Builder(it) }
        if (alertDialogBuilder != null) {
            alertDialogBuilder.setMessage(
                String.format(
                    getString(R.string.question_select_category),
                    name
                )
            )
        }
        alertDialogBuilder?.setPositiveButton(
            R.string.button_yes
        ) { arg0, arg1 ->
            m_sess.category = id
            m_sess.categoryName = name
            val intent = Intent(m_sess.getContext(), PatientInfoActivity::class.java)
            startActivity(intent)
        }
        alertDialogBuilder?.setNegativeButton(R.string.button_no) { dialog, which ->
            Toast.makeText(
                m_sess.getContext(),
                R.string.msg_select_another_category,
                Toast.LENGTH_LONG
            ).show()
        }
        val alertDialog = alertDialogBuilder?.create()
        alertDialog?.show()
    }

    class CustomSimpleAdapter(
        private val mContext: Context,
        data: ArrayList<Map<String, Any>>,
        @LayoutRes
        res: Int,
        from: Array<String>,
        @IdRes
        to: IntArray
    ) :
    // Passing these params to SimpleAdapter
        SimpleAdapter(mContext, data, res, from, to) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            // Get the view in our case list_item.xml
            val view = super.getView(position, convertView, parent)

            // Getting reference of ImageView that we
            // have used in our list_item.xml file
            // so that we can add user defined code
            val checkboxView = view.findViewById<CheckBox>(R.id.select_clinic_check)

            // Reference of TextView which is treated a title
            //val titleTextView = view.findViewById<TextView>(R.id.titleTextView)

            // Adding an clickEvent to the ImageView, as soon as we click this
            // ImageView we will see a Toast which will display a message
            // Note: this event will only fire when ImageView is pressed and
            //       not when whole list_item is pressed
            checkboxView.setOnClickListener {
                Toast.makeText(
                    mContext,
                    "Image with title is pressed",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Finally returning our view
            return view
        }
    }

    private fun LayoutClinicList(list: MutableList<ClinicData>) {
        // https://android.examples.directory/simpleadapter/

        val keyList: Array<String> = arrayOf(
            "Selected", "Clinic Info"
        )
        val viewList: IntArray = intArrayOf(R.id.select_clinic_check, R.id.clinic_info)

        val adapterData: ArrayList<Map<String, Any>> = ArrayList(list.size)
        var mutableMap: MutableMap<String, Any>
        for (i in list.indices) {
            mutableMap = HashMap()
            mutableMap["Selected"] = false
            mutableMap["Clinic Info"] = list[i].start + ", " + list[i].location
            adapterData.add(mutableMap)
        }

        val simpleAdapter = context?.let {
            CustomSimpleAdapter(
                it, adapterData, R.layout.select_clinic_list_row,
                keyList, viewList
            )
        }
        var mListView = m_view?.findViewById<ListView>(R.id.clinic_list)
        mListView!!.adapter = simpleAdapter
        mListView.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
            val selectedItemText = parent.getItemAtPosition(position)

        }
    }
    object ClinicListSerializer : JsonTransformingSerializer<List<ClinicData>>(ListSerializer(ClinicData.serializer())) {
        // If response is not an array, then it is a single object that should be wrapped into the array
        override fun transformDeserialize(element: JsonElement): JsonElement =
            if (element !is JsonArray) JsonArray(listOf(element)) else element
    }

    @Serializable
    data class ClinicList (
        @Serializable(with=ClinicListSerializer::class)
        val clinics: List<ClinicData>
    )

    @kotlinx.serialization.Serializable
    data class ClinicData(
        val id: Int,
        val start: String,
        val end: String,
        val location: String
    )

    override fun onSuccess(code: Int, message: String, a: JSONArray) {

        val dataList: MutableList<ClinicData> = mutableListOf()
        for (i in 0 until a.length()) {
            val item = a.getJSONObject(i)
            val clinic = Json.decodeFromString<ClinicData>(item.toString())
            dataList.add(clinic)
        }
        LayoutClinicList(dataList)
    }
    override fun onSuccess(code: Int, message: String, a: JSONObject) {}
    override fun onSuccess(code: Int, message: String) {}
    override fun onFail(code: Int, message: String) {}

    private fun getClinicList() {
        var listener: RESTCompletionListener = this
        var ret = CommonSessionSingleton.getInstance().getAllClinics(listener)
        if (ret != null) {
            // was cached, just return result
            onSuccess(200, "", ret)
        }
    }

    override fun onResume() {
        super.onResume()
        val window: Window? = getDialog()?.getWindow()
        if (window != null) {
            window.setLayout(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        if (window != null) {
            window.setGravity(Gravity.CENTER)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = getContext()?.let { AlertDialog.Builder(it) }
        var ret: Dialog = super.onCreateDialog(savedInstanceState)
        // Get the layout inflater
        val inflater: LayoutInflater? = getActivity()?.getLayoutInflater()
        getClinicList()
        m_view = inflater?.inflate(R.layout.select_clinic_dialog, null)

        if (builder != null) {
            builder.setView(m_view) // Add action buttons
                .setPositiveButton(R.string.label_continue,
                    DialogInterface.OnClickListener { dialog, id ->
                        val i = Intent(getContext(), PatientSearchActivity::class.java)
                        startActivity(i)
                        getActivity()?.finish()
                        dialog.dismiss()
                    })
                .setNegativeButton(R.string.register_no,
                    DialogInterface.OnClickListener { dialog, id -> val i = Intent(getContext(), LoginActivity::class.java)
                        startActivity(i)
                        getActivity()?.finish()
                        dialog.dismiss() })
            ret = builder.create()
            ret.setTitle(R.string.title_no_clinic_today)
            ret.setOnShowListener(OnShowListener {
                val positive: Button = ret.getButton(AlertDialog.BUTTON_NEGATIVE)
                positive.isFocusable = true
                positive.isFocusableInTouchMode = true
                positive.requestFocus()
            })
            ret.setCanceledOnTouchOutside(false)
        }
        return ret
    }
}