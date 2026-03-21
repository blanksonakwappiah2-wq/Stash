package com.quickbite.android.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.quickbite.android.data.repository.UserRepository
import com.quickbite.android.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private val userRepository = UserRepository()
    
    private val roles = listOf("CUSTOMER", "RESTAURANT_OWNER", "DELIVERY_AGENT")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupSpinner()
        setupClickListeners()
    }
    
    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
    }
    
    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()
            
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            performRegister(name, email, password, role)
        }
        
        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
    
    private fun performRegister(name: String, email: String, password: String, role: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            val result = userRepository.register(name, email, password, role)
            
            showLoading(false)
            
            result.onSuccess { user ->
                Toast.makeText(
                    this@RegisterActivity,
                    "Registration successful! Please login.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            
            result.onFailure { error ->
                Toast.makeText(
                    this@RegisterActivity,
                    "Registration failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
    }
}
