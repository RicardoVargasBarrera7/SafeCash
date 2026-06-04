package com.project.safecash.data.repository

import com.project.safecash.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Obtiene los datos del perfil de cualquier tipo de usuario.
     * Garantiza que el ID del objeto coincida con el UID de Firebase.
     */
    suspend fun getUserData(uid: String): User? {
        return try {
            val snapshot = firestore.collection("usuarios").document(uid).get().await()
            // Sincronizamos el ID del documento con la propiedad id del modelo
            snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene una referencia al documento del usuario para escuchas en tiempo real.
     */
    fun getUserReference(uid: String): DocumentReference {
        return firestore.collection("usuarios").document(uid)
    }

    /**
     * Permite al Administrador obtener listas de usuarios por rol.
     */
    fun getUsersByRole(rol: String) = 
        firestore.collection("usuarios")
            .whereEqualTo("rol", rol)
            .whereEqualTo("estado", "ACTIVO")
}
