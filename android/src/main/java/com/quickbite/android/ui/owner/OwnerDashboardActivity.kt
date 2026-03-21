package com.quickbite.android.ui.owner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quickbite.android.databinding.ActivityOwnerDashboardBinding

class OwnerDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOwnerDashboardBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Owner Dashboard"
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.cardMenu.setOnClickListener {
            Toast.makeText(this, "Menu Management - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardOrders.setOnClickListener {
            Toast.makeText(this, "Orders - Coming Soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.cardSettings.setOnClickListener {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
