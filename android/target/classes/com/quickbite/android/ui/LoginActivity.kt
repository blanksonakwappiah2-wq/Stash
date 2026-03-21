package com.quickbite.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quickbite.android.R
import com.quickbite.android.data.repository.UserRepository
import com.quickbite.android.databinding.ActivityLoginBinding
import com.quickbite.android.ui.agent.DeliveryAgentActivity
import com.quickbite.android.ui.customer.MainActivity
import com.quickbite.android.ui.owner.OwnerDashboardActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val userRepository = UserRepository()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            performLogin(email, password)
        }
        
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun performLogin(email: String, password: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            val result = userRepository.login(email, password)
            
            showLoading(false)
            
            result.onSuccess { user ->
                // Save user info to SharedPreferences
                val prefs = getSharedPreferences("quickbite_prefs", MODE_PRIVATE)
                prefs.edit().apply {
                    putLong("user_id", user.id ?: 0L)
                    putString("user_email", user.email)
                    putString("user_name", user.name)
                    putString("user_role", user.role)
                    apply()
                }
                
                Toast.makeText(this@LoginActivity, "Welcome ${user.name}!", Toast.LENGTH_SHORT).show()
                
                // Navigate based on role
                navigateBasedOnRole(user.role)
            }
            
            result.onFailure { error ->
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun navigateBasedOnRole(role: String) {
        val intent = when (role) {
            "CUSTOMER" -> Intent(this, MainActivity::class.java)
            "RESTAURANT_OWNER" -> Intent(this, OwnerDashboardActivity::class.java)
            "DELIVERY_AGENT" -> Intent(this, DeliveryAgentActivity::class.java)
            "MANAGER" -> Intent(this, MainActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }
}
