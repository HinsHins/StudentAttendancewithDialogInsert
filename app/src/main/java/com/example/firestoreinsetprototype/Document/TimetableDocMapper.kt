package com.example.firestoreinsetprototype.Document

import com.example.firestoreinsetprototype.Model.Module
import com.example.firestoreinsetprototype.Model.Timetable
import com.example.firestoreinsetprototype.ViewModel.TimetableViewModel
import java.util.*
import kotlin.collections.ArrayList

class TimetableDocMapper(var timetables: ArrayList<Timetable>, val timetableViewModels: ArrayList<TimetableViewModel>, ){


    fun updateViewModel(modules : ArrayList<Module>) {
        timetableViewModels.clear()
        val timetableViewModels = timetableToViewModel(modules)
        timetableViewModels.forEach{
            this.timetableViewModels.add(it)
        }
    }

    private fun timetableToViewModel(modules : ArrayList<Module>) : ArrayList<TimetableViewModel>{
        var timetableViewModels = ArrayList<TimetableViewModel>()
        for (timetable in timetables) {
            val module = findModule(modules,timetable.moduleId)
            val date = timetable.date?.toDate() ?: Date()
            val time = timetable.time?.toDate() ?: Date()
            if(module != null) {
                val viewModel = TimetableViewModel(module.name,date,time,0)
                timetableViewModels.add(viewModel)
            }
        }
        return timetableViewModels
    }

    private fun findModule(modules: ArrayList<Module>, moduleId: String) : Module?{
        for (module in modules) {
            if(module.id == moduleId){
                return module
            }
        }
        return null
    }

}