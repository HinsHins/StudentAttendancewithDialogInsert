package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Model.Module
import com.example.firestoreinsetprototype.R
import com.example.firestoreinsetprototype.Util.DateUtil
import kotlinx.android.synthetic.main.module_item.view.*

class ModuleRecyclerViewAdaptor(val modules:ArrayList<Module>):RecyclerView.Adapter<ModuleRecyclerViewAdaptor.ModuleHolder>(){

    interface OnItemClickListener {
        fun onClick(v: View, position : Int)
        fun onLongClick(v: View, position : Int) : Boolean
    }

    var onItemClickListener :ModuleRecyclerViewAdaptor.OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleRecyclerViewAdaptor.ModuleHolder {
        val view = parent.inflate(R.layout.module_item)
        return ModuleHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleRecyclerViewAdaptor.ModuleHolder, position: Int) {
       holder.view.setOnClickListener {
           onItemClickListener?.onClick(it, position)
       }
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position)?:true
            return@OnLongClickListener(isClickable)
        })
        holder.bindModule(modules[position])
    }

    override fun getItemCount(): Int{
        return modules.size
    }

    fun clear(){
        modules.clear()
        onItemClickListener = null
    }

    class ModuleHolder(v:View):RecyclerView.ViewHolder(v){
        var view:View = v
        private var module: Module? = null
        fun bindModule(module: Module){
            this.module = module
            view.module_id_tv.text = module.id
            view.module_name_tv.text = module.name
            view.startDate_tv.text = module.startDate?.toDate()?.date.toString() + "-" +
                    DateUtil.toMonthWith(module.startDate?.toDate()?.month ?: 0).toString() + "-" +
                    DateUtil.toYearWith((module.startDate?.toDate()?.year) ?: 0).toString()
            view.programme_tv.text = module.programmeName
            view.lecturer_tv.text = module.lecturerName
            view.number_of_week_tv.text = module.numOfWeek.toString()
           // view.module_year_tv.text = module.year.toString()
            //view.module_level_tv.text = module.credit.toString()
            //view.module_credit_tv.text = module.credit.toString()
        }
    }





}


