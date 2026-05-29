package com.project.safecash.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.safecash.R
import com.project.safecash.data.repository.AuthRepository
import com.project.safecash.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000)
    }

    private fun checkUserSession() {
        val uid = authRepository.getCurrentUserId()
        if (uid != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val role = authRepository.getUserRole(uid)
                navigateToDashboard(role)
            }
        } else {
            if (isAdded) {
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }
    }

    private fun navigateToDashboard(role: String?) {
        if (!isAdded) return
        val actionId = when (role) {
            "ADMIN" -> R.id.action_splashFragment_to_adminDashboardFragment
            "ESCOLTA" -> R.id.action_splashFragment_to_escoltaDashboardFragment
            else -> R.id.action_splashFragment_to_userDashboardFragment
        }
        findNavController().navigate(actionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
