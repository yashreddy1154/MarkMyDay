package com.project.markmyday.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.DigitalDiaryEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DigitalDiaryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val diaryCollection = firestore.collection("digital_diary")

    suspend fun postDiaryEntry(entry: DigitalDiaryEntry) {
        try {
            val docRef = if (entry.id.isEmpty()) {
                diaryCollection.document()
            } else {
                diaryCollection.document(entry.id)
            }
            val entryWithId = entry.copy(id = docRef.id)
            docRef.set(entryWithId).await()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getDiaryEntriesForClass(className: String): Flow<List<DigitalDiaryEntry>> = callbackFlow {
        val subscription = diaryCollection
            .whereEqualTo("className", className)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("DigitalDiaryRepository", "Error fetching diary entries: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val entries = snapshot.toObjects(DigitalDiaryEntry::class.java)
                        .sortedByDescending { it.timestamp }
                    trySend(entries)
                }
            }
        awaitClose { subscription.remove() }
    }
}
