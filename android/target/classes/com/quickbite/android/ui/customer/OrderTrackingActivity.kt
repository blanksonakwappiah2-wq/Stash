package com.quickbite.android.ui.customer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.quickbite.android.databinding.ActivityOrderTrackingBinding

class OrderTrackingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOrderTrackingBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Track Order"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // TODO: Implement order tracking with map
        // For now, show placeholder
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
