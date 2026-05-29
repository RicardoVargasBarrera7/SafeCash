package com.project.safecash.data.repository

import com.project.safecash.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun login(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Login failed"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(user: User, pass: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.correo, pass).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("Registration failed"))
            val newUser = user.copy(id = uid)
            firestore.collection("usuarios").document(uid).set(newUser).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(uid: String): String? {
        return try {
            val doc = firestore.collection("usuarios").document(uid).get().await()
            doc.getString("rol")
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}
