package com.quickbite.android.ui.customer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quickbite.android.databinding.ActivityRestaurantDetailBinding

class RestaurantDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRestaurantDetailBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Get restaurant info from intent
        val restaurantName = intent.getStringExtra("restaurant_name") ?: "Restaurant"
        val restaurantAddress = intent.getStringExtra("restaurant_address") ?: ""
        val restaurantCategory = intent.getStringExtra("restaurant_category") ?: ""
        
        binding.apply {
            tvRestaurantName.text = restaurantName
            tvRestaurantAddress.text = restaurantAddress
            tvRestaurantCategory.text = restaurantCategory
        }
        
        // TODO: Load menu items for this restaurant
        loadMenuItems()
    }
    
    private fun loadMenuItems() {
        // TODO: Implement menu loading
        binding.emptyStateMenu.visibility = View.VISIBLE
        Toast.makeText(this, "Menu loading - Coming Soon", Toast.LENGTH_SHORT).show()
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
