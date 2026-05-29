package com.project.safecash.data.repository

import com.project.safecash.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserData(uid: String): User? {
        return try {
            val document = firestore.collection("usuarios").document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserDataFlow(uid: String) = 
        firestore.collection("usuarios").document(uid)
}
