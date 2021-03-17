package com.example.firestoreinsetprototype.Extension

fun List<Double>.isSorted(): Boolean {
    for (i in 0 until this.size - 1) {
        if (this[i] > this[i + 1]) {
            return false
        }
    }
    return true
}