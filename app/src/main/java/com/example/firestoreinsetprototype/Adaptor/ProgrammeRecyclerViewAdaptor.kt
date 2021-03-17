package com.example.firestoreinsetprototype.Adaptor

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestoreinsetprototype.Extension.inflate
import com.example.firestoreinsetprototype.Model.Programme
import com.example.firestoreinsetprototype.R
import kotlinx.android.synthetic.main.programme_item.view.*


//arraylist<Programme> -> Adaptor (data source -> view) -> View(Items)
class ProgrammeRecyclerViewAdaptor(programmes: ArrayList<Programme>) : RecyclerView.Adapter<ProgrammeRecyclerViewAdaptor.ProgrammeHolder>() {

    interface OnItemClickListener {
        fun onClick(v: View,position : Int)
        fun onLongClick(v: View,position : Int) : Boolean
    }

    val programmes : ArrayList<Programme> = programmes
    var onItemClickListener : OnItemClickListener? = null

    //create item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgrammeHolder {
        //without Extension
//        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
//        val view = layoutInflater.inflate(R.layout.programme_item,parent,false)
        //With Extension
        val view = parent.inflate(R.layout.programme_item)
        return ProgrammeHolder(view)
    }

    //bind data to view
    override fun onBindViewHolder(holder: ProgrammeHolder, position: Int) {
        holder.view.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(it,position)
        })
        holder.view.setOnLongClickListener(View.OnLongClickListener {
            val isClickable = onItemClickListener?.onLongClick(it,position) ?: true
            return@OnLongClickListener(isClickable)
        })
        holder.bindProgramme(programmes[position])
    }

    override fun getItemCount(): Int {
        return programmes.size
    }

    fun clear(){
        programmes.clear()
        onItemClickListener = null
    }

    //list item
    class ProgrammeHolder(v: View) : RecyclerView.ViewHolder(v) {
        var view: View = v
        private var programme: Programme? = null

        fun bindProgramme(programme: Programme) {
            this.programme = programme
            view.programme_id_tv.text = programme.id
            view.programme_name_tv.text = programme.name
        }
    }
}



