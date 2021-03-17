package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.dateFormat
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Extension.timeFormat
import com.example.firestoreinsetprototype.Model.Timetable
import com.example.firestoreinsetprototype.R
import com.example.firestoreinsetprototype.Util.DateUtil
import com.example.firestoreinsetprototype.ViewModel.TimetableViewModel
import kotlinx.android.synthetic.main.module_item.view.*
import kotlinx.android.synthetic.main.timetable_item.view.*
import java.text.SimpleDateFormat

class TimetableRecyclerViewAdaptor(timetables: ArrayList<TimetableViewModel>) : RecyclerView.Adapter<TimetableRecyclerViewAdaptor.TimetableHolder>() {

     interface OnItemClickListener {
        fun onClick(v: View,position : Int)
        fun onLongClick(v: View,position : Int) : Boolean
    }

    val timetables : ArrayList<TimetableViewModel> = timetables
    var onItemClickListener : OnItemClickListener? = null

    //create item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableHolder {
        //without Extension
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.student_item,parent,false)
        //With Extension
        val view = parent.inflate(R.layout.timetable_item)
        return TimetableHolder(view)
    }

    //bind data to view
    override fun onBindViewHolder(holder: TimetableHolder, position: Int) {
        holder.view.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(it,position)
        })
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position) ?: true
            return@OnLongClickListener(isClickable)
        })
        holder.bindTimetable(timetables[position])
    }

    override fun getItemCount(): Int {
        return timetables.size
    }

    fun clear(){
        timetables.clear()
        onItemClickListener = null
    }

    //list item
    class TimetableHolder(v: View) : RecyclerView.ViewHolder(v) {
        var view: View = v
        private var timetable: TimetableViewModel? = null

        fun bindTimetable(timetable: TimetableViewModel) {
            this.timetable = timetable
            //"dd/M/yyyy hh:mm:ss"
//            val dateFormat = SimpleDateFormat("dd/M/yyyy").format(timetable.date)
//            val timeFormat = SimpleDateFormat("hh:mm").format(timetable.time)
            view.lesson_name_tv.text = timetable.moduleName
            view.date_tv.text = timetable.date.dateFormat()
            view.time_tv.text = timetable.time.timeFormat()

        }
    }
}



