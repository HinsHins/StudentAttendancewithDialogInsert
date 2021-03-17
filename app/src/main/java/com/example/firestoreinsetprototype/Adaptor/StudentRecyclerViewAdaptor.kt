package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Model.Student
import com.example.firestoreinsetprototype.R
import kotlinx.android.synthetic.main.student_item.view.*

//arraylist<Student> -> Adaptor (data source -> view) -> View(Items)
class StudentRecyclerViewAdaptor(students: ArrayList<Student>) : RecyclerView.Adapter<StudentRecyclerViewAdaptor.StudentHolder>() {

     interface OnItemClickListener {
        fun onClick(v: View,position : Int)
        fun onLongClick(v: View,position : Int) : Boolean
    }

    val students : ArrayList<Student> = students
    var onItemClickListener : OnItemClickListener? = null

    //create item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        //without Extension
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.student_item,parent,false)
        //With Extension
        val view = parent.inflate(R.layout.student_item)
        return StudentHolder(view)
    }

    //bind data to view
    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        holder.view.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(it,position)
        })
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position) ?: true
            return@OnLongClickListener(isClickable)
        })
        holder.bindStudent(students[position])
    }

    override fun getItemCount(): Int {
        return students.size
    }

    fun clear(){
        students.clear()
        onItemClickListener = null
    }

    //list item
    class StudentHolder(v: View) : RecyclerView.ViewHolder(v) {
        var view: View = v
        private var student: Student? = null

        fun bindStudent(student: Student) {
            this.student = student
            view.sid_tv.text = student.id
            view.student_name_tv.text = student.name
//            view.student_email_tv.text = student.email
            view.programme_tv.text = student.programmeName
//            view.country_tv.text = student.country
        }
    }
}



