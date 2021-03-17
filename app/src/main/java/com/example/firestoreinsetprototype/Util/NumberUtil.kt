package com.example.firestoreinsetprototype.Util

import android.util.Log


object NumberUtil {

    fun generate(max : Int) : Array<String> {
        var array = Array(max) { _ -> "" }
        for(index in 0 until max){
            array[index] = "${index + 1}"
        }
        return array
    }

    fun generate(max : Int, interval : Int) : Array<String> {
        var array = Array(((max/interval) + 1)) { _ -> "" }
        var i = 0
        for(number in 0..max step interval){
            array[i] = "$number"
            i++;
        }
        return array
    }
}
