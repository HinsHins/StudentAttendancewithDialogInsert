package com.example.firestoreinsetprototype

//import com.github.mikephil.charting.data.BarData
//import com.github.mikephil.charting.data.BarDataSet
//import com.github.mikephil.charting.data.BarEntry
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.NumberUtil
import com.example.firestoreinsetprototype.Util.SpinnerUtil
import com.example.firestoreinsetprototype.Util.retrieveData
import com.example.firestoreinsetprototype.Util.retrieveDataWithMatch
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.android.synthetic.main.activity_monitoring.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.floor

class MonitoringActivity : AppCompatActivity() {

    private val timetablePath = FirestoreCollectionPath.TIMETABLES_PATH
    private val attendancesPath = FirestoreCollectionPath.ATTENDANCES_PATH
    private val studentsPath = FirestoreCollectionPath.STUDENTS_PATH
    private val modulesPath = FirestoreCollectionPath.MODULES_PATH
    private var selectedModule: Module? = null
    private var selectedStudent : Student? = null
    private var selectedTimetable: Timetable? = null
    private val timetables = ArrayList<Timetable>()
//    private val attendances = ArrayList<Attendance>()
    private val allAttendances = ArrayList<ArrayList<Attendance>>()
    private val allTimetables = ArrayList<ArrayList<Timetable>>()
    private val modules = ArrayList<Module>()
    private val students = ArrayList<Student>()
    private val fb = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitoring)
        initViews()
//        barChart.setMaxVisibleValueCount(100)
        retrieveModules()
        retrieveStudents()

    }

    override fun onStop() {
        super.onStop()
        graph.removeAllSeries()
    }

    private fun initViews() {
        rb_module.isChecked = true
        graph.visibility = View.VISIBLE
        module_Spinner.visibility = View.VISIBLE
        barChart.visibility = View.INVISIBLE
        student_Spinner.visibility = View.INVISIBLE
    }

    private fun retrieveModules() {
        val modulesCollection = fb.collection(modulesPath)
        modulesCollection.retrieveData(modules as ArrayList<Model>, Module::class.java) {
            var modules = modules as ArrayList<Module>
            val modulesString = (modules.map { it.name } as ArrayList<String>)
            val modulesSpinner: Spinner = findViewById(R.id.module_Spinner)
            val moduleAdapter = SpinnerUtil.setupSpinner(
                this,
                modulesSpinner,
                modulesString
            ) {
                selectedModule = modules[it]
                selectedModule?.let {
                    retrieveTimetables(it)
                }
            }
            moduleAdapter.notifyDataSetChanged()
        }
    }

    private fun retrieveStudents() {
        fb.collection(studentsPath).retrieveData(students as ArrayList<Model>, Student::class.java) {
            var students = students as ArrayList<Student>
            val studentString = (students.map { it.name } as ArrayList<String>)
            val studentSpinner: Spinner = findViewById(R.id.student_Spinner)
            val studentAdapter = SpinnerUtil.setupSpinner(
                this,
                studentSpinner,
                studentString
            ) {
                selectedStudent = students[it]
                selectedStudent?.let {
                    retrieveStudentModules(it)
                }
            }
            studentAdapter.notifyDataSetChanged()
        }
    }


    private fun retrieveStudentModules(student :Student) {
        var modules = ArrayList<Module>()
        allTimetables.clear()
        fb.collection(modulesPath).retrieveDataWithMatch(
            "programmeId",
            student.programmeId,
            modules as ArrayList<Model>,
            Module::class.java
        ) {
            modules.sortBy { it.id }
//            Log.d("timetables sort", "$timetables")
            modules.forEach { _ ->
                allTimetables.add(ArrayList())
            }
            var barEntries = ArrayList<BarEntry>()
            var xDisplayVals = ArrayList<String>()
            GlobalScope.launch {

                var defered = ArrayList<Deferred<BarEntry>>()
                modules.zip(allTimetables).forEachIndexed { index, element ->
                    xDisplayVals.add((element.first as Module).name)
                    defered.add(async {
                        retrieveTimeTables(
                            element.second,
                            element.first as Module,
                            student,
                            barEntries,
                            index
                        )
                    })
                }

                if (defered.filter { it.await() != null }.isNotEmpty()) {
                    barEntries.sortBy { it.x }
                    barEntries.forEachIndexed() { index, data ->
                        Log.d("barEntries", "index : ${index} ${data.toString()}")
                    }
                    xDisplayVals.forEachIndexed() { index, data ->
                        Log.d("xDisplayVals", "index : ${index} ${data.toString()}")
                    }
                    val dataSet = BarDataSet(barEntries,"DataSet")
                    val data = BarData(dataSet)

                    Log.d("data", "entryCount : ${data.entryCount}")

                    barChart.data = data
                    //setting xAxis
                    barChart.axisLeft.mAxisMaximum = 100f
                    barChart.axisLeft.axisMinimum = 0f
                    barChart.axisRight.mAxisMaximum = 100f
                    barChart.axisRight.axisMinimum = 0f
//                    xAxis.axisMinimum = 0.0F
//                    xAxis.axisMinimum = (xDisplayVals.count() - 1).toFloat()
                    //setting yAxis
                    val xAxis = barChart.xAxis
                    xAxis.valueFormatter = IndexAxisValueFormatter(xDisplayVals)
                    xAxis.position = XAxis.XAxisPosition.TOP
                    xAxis.granularity = 1f
                    xAxis.setDrawAxisLine(false)
                    xAxis.setDrawGridLines(false)
                    barChart.invalidate()
                }

            }
        }
    }

    private fun retrieveTimetables(module: Module) {
        timetables.clear()
        allAttendances.clear()
//        val timetableCollection = fb.collection(timetablePath)
        fb.collection(timetablePath).retrieveDataWithMatch(
            "moduleId",
            module.id,
            timetables as ArrayList<Model>,
            Timetable::class.java
        ) {
            graph.removeAllSeries()
            var timetables = timetables as ArrayList<Timetable>
            timetables.sortBy { it.week }
            Log.d("timetables sort", "$timetables")
            timetables.forEach { _ ->
                allAttendances.add(ArrayList())
            }
//            var attendanceRates = ArrayList<BarEntry>()
            var dataPointArray =  Array(timetables.size) { _ -> DataPoint(0.0,0.0)}
            GlobalScope.launch {

                var defered = ArrayList<Deferred<DataPoint>>()
                timetables.zip(allAttendances).forEachIndexed { index, element ->
//                retrieveAttendance(element.second,element.first,dataPointArray,index == (timetables.count() - 1),index)
//                Log.d("allAttendance", "retrieveTimetables: it.first ${it.first} ,it.second : ${it.second}")
//                retrieveAttendance(allAttendance,it.id)
                    defered.add(async {
                        retrieveAttendance(
                            element.second,
                            element.first,
                            dataPointArray,
                            index
                        )
                    })
                }
                if (defered.filter { it.await() != null }.isNotEmpty()) {
                    dataPointArray.forEachIndexed() { index, data ->
                        Log.d("dataPointArray", "index : ${index} ${data.toString()}")
                    }
                    val series = BarGraphSeries<DataPoint>()
//                    val doubleArray = dataPointArray.map { it.x }
//                    if (doubleArray.isSorted()) {
                        series.resetData(dataPointArray)
                        //percentage
                        graph.viewport.setMinY(0.0)
                        graph.viewport.setMaxY(100.0)
                        val maxWeek = timetables.last().week
                        //week
                        graph.viewport.setMinX(1.0)
                        graph.viewport.setMaxX(maxWeek.toDouble())
                        graph.viewport.isYAxisBoundsManual = true
                        graph.viewport.isXAxisBoundsManual = true
                        graph.title = "Attendance rate"
//                        var horizontalArray = Array(maxWeek) { _ -> "" }
//                        for(index in 0 until maxWeek){
//                            horizontalArray[index] = "${index + 1}"
//                        }
//                        var verticalArray = Array(((100/20) + 1)) { _ -> "" }
//                        var i = 0
//                        for(number in 0..100 step 20){
//                            verticalArray[i] = "$number"
//                            Log.d("i", "i: $i ")
//                            i++;
//                        }
                        var horizontalDataSet = NumberUtil.generate(maxWeek)
                        var verticalDataSet = NumberUtil.generate(100,10)
                        graph.gridLabelRenderer.labelFormatter = StaticLabelsFormatter(graph, horizontalDataSet,verticalDataSet)
                        graph.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.HORIZONTAL
//                        graph.gridLabelRenderer.horizontalAxisTitleTextSize = 20f
                        graph.gridLabelRenderer.horizontalAxisTitle = "Week"
                        graph.gridLabelRenderer.reloadStyles()
                        graph.addSeries(series)
//                    }
                }
            }
//            timetables.sortBy { it.week }
//            val timetablesString = (timetables.map { it.week } as ArrayList<String>)
////            val timetablesSpinner: Spinner = findViewById(R.id.timetable_Spinner)
//            val timetableAdapter = SpinnerUtil.setupSpinner(
//                this,
////                timetablesSpinner,
//                timetablesString
//            ) {
//                selectedTimetable = timetables[it]
//                retrieveAttendance()
//            }
//            timetableAdapter.notifyDataSetChanged()
        }
    }

//    private fun retrieveAttendance(arrayList: ArrayList<Attendance>,timetable :Timetable, dataPointArray : Array<DataPoint>, isLast : Boolean,index : Int) {
//        fb.collection(attendancesPath).retrieveDataWithMatch(
//            "timetableId",
//            timetable.id,
//            arrayList as ArrayList<Model>,
//            Attendance::class.java
//        ) {
//            Log.d("timetable Id: ", "${timetable.id}")
//            Log.d("timetable week: ", "${timetable.week}")
////            Log.d("retrieveAttendance", "arrayList ${arrayList}")
////            barChartArray.add(BarEntry(timetable.week.toFloat(),calculateAttendanceRate(arrayList).toFloat()))
//            dataPointArray[index] = DataPoint(timetable.week.toDouble(),calculateAttendanceRate(arrayList))
//            if(isLast) {
//                dataPointArray.forEachIndexed() { index,data ->
//                    Log.d("dataPointArray", "index : ${index} ${data.toString()}")
//                }
//
//                val series = BarGraphSeries<DataPoint>()
//                val doubleArray = dataPointArray.map { it.x }
//                if(doubleArray.isSorted()) {
//                    series.resetData(dataPointArray)
//                    graph.addSeries(series)
//                }
//
////                if (barChartArray.isNotEmpty()) {
////                try {
////                    synchronized(barChart) {
////                        Log.d("barChartArray", "barChartArray:$barChartArray ")
////                        var barDataSet = BarDataSet(barChartArray,"AttendanceRates")
////                        val barData = BarData(barDataSet)
////                        barChart.data = barData
////                        barChart.animateY(2000)
////                        barChart.invalidate()
////                        Log.i(this.javaClass.toString(), "Chart updated successfully.")
////                    }
////                } catch (ex: Exception) {
////                    Log.e(
////                        this.javaClass.toString(),
////                        "Setting chart date in Heart rate thread throw an exception."
////                    )
////                    Log.e(this.javaClass.toString(), ex.toString())
////                }
////                }
//
//            }
//        }
//    }

    private fun calculateAttendanceRate(arrayList: ArrayList<Attendance>) : Double {
        val total = arrayList.count()
        val present = arrayList.filter { it.status }.count()
        return (present.toDouble()/total.toDouble()) * 100
        Log.d("Rate Total", "$total ")
        Log.d("Rate Present", "$present ")
        Log.d("Rate", "${(present.toDouble()/total.toDouble()) * 100} ")
    }

    private suspend fun retrieveAttendance(arrayList: ArrayList<Attendance>, timetable :Timetable, dataPointArray : Array<DataPoint>, index : Int): DataPoint = suspendCoroutine { continuation ->
        fb.collection(attendancesPath).retrieveDataWithMatch(
            "timetableId",
            timetable.id,
            arrayList as ArrayList<Model>,
            Attendance::class.java
        ) {
            Log.d("timetable Id: ", "${timetable.id}")
            Log.d("timetable week: ", "${timetable.week}")
            dataPointArray[index] = DataPoint(timetable.week.toDouble(),calculateAttendanceRate(arrayList))
            continuation.resume(dataPointArray[index])
        }
    }
    private suspend fun retrieveTimeTables(arrayList: ArrayList<Timetable>, module : Module, student : Student,barEntries : ArrayList<BarEntry>, index : Int): BarEntry = suspendCoroutine { continuation ->
        fb.collection(timetablePath).retrieveDataWithMatch(
            "moduleId",
            module.id,
            arrayList as ArrayList<Model>,
            Timetable::class.java
        ) {
            Log.d("module Id: ", "${module.id}")
            Log.d("module name: ", "${module.name}")
            val barEntry = BarEntry(floor(index.toFloat()),calculateModuleAttendanceRate(arrayList,student).toFloat())
            barEntries.add(barEntry)
            continuation.resume(barEntry)
        }
    }
    private fun calculateModuleAttendanceRate(arrayList: ArrayList<Timetable>, student : Student) : Double {
        val total = arrayList.count()
        val present = arrayList.filter { it.presentedStudents.contains(student.id) }.count()
        return (present.toDouble()/total.toDouble()) * 100
        Log.d("Module Rate Total", "$total ")
        Log.d("Module Rate Present", "$present ")
        Log.d("Module Rate", "${(present.toDouble()/total.toDouble()) * 100} ")
    }




    fun onRadioButtonClicked(view : View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.rb_module ->
                    if (checked) {
                        graph.visibility = View.VISIBLE
                        module_Spinner.visibility = View.VISIBLE
                        barChart.visibility = View.INVISIBLE
                        student_Spinner.visibility = View.INVISIBLE
                    }
                R.id.rb_student ->
                    if (checked) {
                        graph.visibility = View.INVISIBLE
                        module_Spinner.visibility = View.INVISIBLE
                        barChart.visibility = View.VISIBLE
                        student_Spinner.visibility = View.VISIBLE
                    }
            }
        }
    }

}