package com.example.firestoreinsetprototype.Util

import android.util.Log
import com.example.firestoreinsetprototype.Model.Model
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QueryDocumentSnapshot

fun <T> CollectionReference.realTimeUpdate(
    list: ArrayList<Model>,
    valueType: Class<T>,
    completion: () -> Unit
) {
    this.addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.w("Fail", "Listen failed.", e)
            return@addSnapshotListener
        }

        val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
            "Local"
        else
            "Server"
        Log.d("snapshot", snapshot.toString())
        if (snapshot == null) {
            android.util.Log.d("null", "$source data: null"); return@addSnapshotListener; }
        for (dc in snapshot.documentChanges) {
            val doc = dc.document.toObject(valueType) as Model
            Log.d("dc.type", dc.type.toString())
            when (dc.type) {
                DocumentChange.Type.ADDED -> {
                    if (ArrayUtil.hasItem((list), doc) == null) {
                        list.add(doc)
                        Log.d("adding item", doc.toString())
                    }
                }
                DocumentChange.Type.MODIFIED -> {
                    ArrayUtil.hasItem(list, doc).let { item ->
                        val index = list.indexOf(item)
                        list[index] = doc
                        Log.d("modify item", doc.toString())
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    ArrayUtil.hasItem(list, doc).let { item ->
                        list.remove(item)
                        Log.d("removing item", doc.toString())
                    }
                }
            }
        }
        Log.d("RealTimeUpdate", "$list")
        completion()
    }
}
fun CollectionReference.realTimeUpdate(
    list: ArrayList<QueryDocumentSnapshot>,
    completion: () -> Unit
) {
    this.addSnapshotListener { snapshot, e ->
        if (e != null) {
            Log.w("Fail", "Listen failed.", e)
            return@addSnapshotListener
        }

        val source = if (snapshot != null && snapshot.metadata.hasPendingWrites())
            "Local"
        else
            "Server"
        Log.d("snapshot", snapshot.toString())
        if (snapshot == null) {
            android.util.Log.d("null", "$source data: null"); return@addSnapshotListener; }
        for (dc in snapshot.documentChanges) {
            val doc = dc.document
            Log.d("dc.type", dc.type.toString())
            when (dc.type) {
                DocumentChange.Type.ADDED -> {
                    if (ArrayUtil.hasItem((list), doc) == null) {
                        list.add(doc)
                        Log.d("adding item", doc.toString())
                    }
                }
                DocumentChange.Type.MODIFIED -> {
                    ArrayUtil.hasItem(list, doc).let { item ->
                        val index = list.indexOf(item)
                        list[index] = doc
                        Log.d("modify item", doc.toString())
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    ArrayUtil.hasItem(list, doc).let { item ->
                        list.remove(item)
                        Log.d("removing item", doc.toString())
                    }
                }
            }
        }
        Log.d("RealTimeUpdate", "$list")
        completion()
    }
}

fun <T> CollectionReference.retrieveData(list : ArrayList<Model>, valueType : Class<T>, completion : ((CollectionReference) -> Unit)? = null) {
    this.get().addOnSuccessListener { result ->
            for(document in result){
                Log.d("Item", "${document.id} => ${document.data}")
                var item = document.toObject(valueType) as Model
                Log.d("Item","$item")
                list.add(item)
            }
            Log.d("load item", "$list")
            if (completion != null) {
                completion(this)
            }
        }
        .addOnFailureListener { exception ->
            Log.d("", "Error getting documents: ", exception)
        }
}
fun <T> CollectionReference.retrieveDataWithMatch(field : String, value : Any, list : ArrayList<Model>, valueType : Class<T>, completion : (CollectionReference) -> Unit) {
    this.whereEqualTo(field,value).get().addOnSuccessListener { result ->
        for(document in result){
            Log.d("Item", "${document.id} => ${document.data}")
            var item = document.toObject(valueType) as Model
            Log.d("Item","$item")
            list.add(item)
        }
        Log.d("load item", "$list")
        completion(this)
    }
        .addOnFailureListener { exception ->
            Log.d("", "Error getting documents: ", exception)
        }
}

fun <T> CollectionReference.retrieveDataWithContains(field : String, value : Any, list : ArrayList<Model>, valueType : Class<T>, completion : (CollectionReference) -> Unit) {
    this.whereArrayContains(field,value).get().addOnSuccessListener { result ->
        for(document in result){
            Log.d("Item", "${document.id} => ${document.data}")
            var item = document.toObject(valueType) as Model
            Log.d("Item","$item")
            list.add(item)
        }
        Log.d("load item", "$list")
        completion(this)
    }
        .addOnFailureListener { exception ->
            Log.d("", "Error getting documents: ", exception)
        }
}

fun CollectionReference.retrieveData(list : ArrayList<QueryDocumentSnapshot>, completion : ((CollectionReference) -> Unit)? = null ) {
    this.get().addOnSuccessListener { result ->
        for(document in result){
            Log.d("Item", "${document.id} => ${document.data}")
//            var item = document.toObject(valueType) as Model
//            Log.d("Item","$item")
            list.add(document)
//            Log.d("Item","$item")
        }
        Log.d("load item", "$list")
        if (completion != null) {
            completion(this)
        }
    }
        .addOnFailureListener { exception ->
            Log.d("", "Error getting documents: ", exception)
        }
}


