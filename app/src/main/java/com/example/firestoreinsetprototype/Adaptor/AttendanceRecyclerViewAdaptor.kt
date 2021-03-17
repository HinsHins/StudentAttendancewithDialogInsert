package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Extension.timeFormat
import com.example.firestoreinsetprototype.Model.Attendance
import com.example.firestoreinsetprototype.R
import com.example.firestoreinsetprototype.ViewModel.AttendanceViewModel
import kotlinx.android.synthetic.main.attendance_item.view.*

class AttendanceRecyclerViewAdaptor(attendances: ArrayList<AttendanceViewModel>) : RecyclerView.Adapter<AttendanceRecyclerViewAdaptor.AttendanceHolder>() {

     interface OnItemClickListener {
        fun onClick(v: View,position : Int)
        fun onLongClick(v: View,position : Int) : Boolean
    }
    interface OnCheckBoxClickListener {
        fun onClick(v: View,position : Int)
    }

    val attendances : ArrayList<AttendanceViewModel> = attendances
    var onItemClickListener : OnItemClickListener? = null
    var onCheckboxClickListener : OnCheckBoxClickListener? = null

    //create item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceHolder {
        //without Extension
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.student_item,parent,false)
        //With Extension
        val view = parent.inflate(R.layout.attendance_item)
        return AttendanceHolder(view)
    }

    //bind data to view
    override fun onBindViewHolder(holder: AttendanceHolder, position: Int) {
        holder.view.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(it,position)
        })
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position) ?: true
            return@OnLongClickListener(isClickable)
        })

        holder.view.checkbox_present.setOnClickListener{
            onCheckboxClickListener?.onClick(it,position)
        }
        holder.bindAttendance(attendances[position])
    }

    override fun getItemCount(): Int {
        return attendances.size
    }

    fun clear(){
        attendances.clear()
        onItemClickListener = null
    }

    //list item
    class AttendanceHolder(v: View) : RecyclerView.ViewHolder(v) {
        var view: View = v
        private var attendance: AttendanceViewModel? = null

        fun bindAttendance(attendance: AttendanceViewModel) {
            this.attendance = attendance
            view.student_id_tv.text = attendance.studentId
            view.student_name_tv.text = attendance.studentName
            view.present_tv.text = if(attendance.time != null) attendance.time.timeFormat() else "No"
            view.checkbox_present.isChecked = attendance.time != null
//            view.country_tv.text = student.country
        }
    }
}



