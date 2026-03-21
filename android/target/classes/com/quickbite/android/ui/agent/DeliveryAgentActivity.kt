package com.quickbite.android.ui.agent

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quickbite.android.databinding.ActivityDeliveryAgentBinding

class DeliveryAgentActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeliveryAgentBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryAgentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Delivery Agent"
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnGoOnline.setOnClickListener {
            Toast.makeText(this, "You are now Online!", Toast.LENGTH_SHORT).show()
            binding.btnGoOnline.isEnabled = false
            binding.btnGoOffline.isEnabled = true
        }
        
        binding.btnGoOffline.setOnClickListener {
            Toast.makeText(this, "You are now Offline", Toast.LENGTH_SHORT).show()
            binding.btnGoOnline.isEnabled = true
            binding.btnGoOffline.isEnabled = false
        }
        
        binding.cardActiveDeliveries.setOnClickListener {
            Toast.makeText(this, "Active Deliveries - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
