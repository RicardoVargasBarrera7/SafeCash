package com.project.safecash.data.repository

import com.project.safecash.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio encargado de gestionar la comunicación con Firebase Auth y Firestore
 * para todo lo relacionado con la autenticación y perfiles de usuario.
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Obtiene el ID único del usuario actual si hay una sesión iniciada.
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Realiza el inicio de sesión con email y contraseña.
     */
    suspend fun login(email: String, pass: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Error en el inicio de sesión"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario en Auth y crea su documento en la colección 'usuarios'.
     */
    suspend fun register(user: User, pass: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.correo, pass).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("Error en el registro"))
            
            // Sincronizamos el ID de Auth con el del modelo
            val newUser = user.copy(id = uid)
            
            // Guardamos en la colección 'usuarios' (centralizada para todos los roles)
            firestore.collection("usuarios").document(uid).set(newUser).await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recupera el rol del usuario desde Firestore para determinar su acceso.
     */
    suspend fun getUserRole(uid: String): String? {
        return try {
            // Buscamos en la colección principal de usuarios
            val doc = firestore.collection("usuarios").document(uid).get().await()
            doc.getString("rol")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cierra la sesión activa en el dispositivo.
     */
    fun logout() {
        auth.signOut()
    }
}
