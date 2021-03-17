package com.example.firestoreinsetprototype.Util

import android.R
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

object SpinnerUtil {
    fun setupSpinner(context : Context, spinner : Spinner, displayList : ArrayList<String>, onItemSelected : (Int) -> Unit )  : ArrayAdapter<String> {
        val arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item,displayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                onItemSelected(p2)
//                selectedProgramme = programmes[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        return arrayAdapter
    }
}