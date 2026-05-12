package com.project.markmyday.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Timetable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TimetableRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val timetableCollection = firestore.collection("timetable")

    fun getAllTimetables(): Flow<List<Timetable>> = callbackFlow {
        val subscription = timetableCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val timetables = snapshot.toObjects(Timetable::class.java)
                trySend(timetables)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun updateTimetable(timetable: Timetable) {
        try {
            timetableCollection.document(timetable.className).set(timetable).await()
        } catch (e: Exception) {
            throw e
        }
    }
}
