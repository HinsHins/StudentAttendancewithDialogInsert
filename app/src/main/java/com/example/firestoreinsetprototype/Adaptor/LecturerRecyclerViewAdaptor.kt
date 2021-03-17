package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Model.Lecturer
import com.example.firestoreinsetprototype.R
import kotlinx.android.synthetic.main.lecturer_item.view.*

class LecturerRecyclerViewAdaptor(val lecturers: ArrayList<Lecturer>)
    :RecyclerView.Adapter<LecturerRecyclerViewAdaptor.LecturerHolder>(){

    interface OnItemClickListener {
        fun onClick(v: View, position : Int)
        fun onLongClick(v: View, position : Int) : Boolean
    }

    var onItemClickListener : OnItemClickListener? = null

    //create item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LecturerHolder {
        //without Extension
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.student_item,parent,false)
        //With Extension
        val view = parent.inflate(R.layout.lecturer_item)
        return LecturerHolder(view)
    }

    //bind data to view
    override fun onBindViewHolder(holder: LecturerHolder, position: Int) {
        holder.view.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(it,position)
        })
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position) ?: true
            return@OnLongClickListener(isClickable)
        })
        holder.bindLecturer(lecturers[position])
    }

    override fun getItemCount(): Int {
        return lecturers.size
    }

    fun clear(){
        lecturers.clear()
        onItemClickListener = null
    }

    //list item
    class LecturerHolder(v: View) : RecyclerView.ViewHolder(v) {
        var view: View = v
        private var lecturer: Lecturer? = null

        fun bindLecturer(lecturer: Lecturer) {
            this.lecturer = lecturer
            view.lecturer_id_tv.text = lecturer.id
            view.lecturer_name_tv.text = lecturer.name
            view.lecturer_position_tv.text = lecturer.position
            view.lecturer_department_tv.text = lecturer.department

        }
    }
}