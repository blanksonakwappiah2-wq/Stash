package com.quickbite.android.ui.customer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quickbite.android.R
import com.quickbite.android.data.model.Restaurant
import com.quickbite.android.data.repository.RestaurantRepository
import com.quickbite.android.databinding.ActivityMainBinding
import com.quickbite.android.ui.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), RestaurantAdapter.OnRestaurantClickListener {
    
    private lateinit var binding: ActivityMainBinding
    private val restaurantRepository = RestaurantRepository()
    private lateinit var adapter: RestaurantAdapter
    
    private var userId: Long = 0L
    private var userEmail: String = ""
    private var userName: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "QuickBite"
        supportActionBar?.subtitle = "Discover Restaurants"
        
        loadUserInfo()
        setupRecyclerView()
        setupSwipeRefresh()
        loadRestaurants()
    }
    
    private fun loadUserInfo() {
        val prefs = getSharedPreferences("quickbite_prefs", MODE_PRIVATE)
        userId = prefs.getLong("user_id", 0L)
        userEmail = prefs.getString("user_email", "") ?: ""
        userName = prefs.getString("user_name", "") ?: ""
    }
    
    private fun setupRecyclerView() {
        adapter = RestaurantAdapter(this)
        binding.recyclerViewRestaurants.adapter = adapter
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadRestaurants()
        }
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.secondary,
            R.color.accent
        )
    }
    
    private fun loadRestaurants() {
        if (!binding.swipeRefresh.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        
        lifecycleScope.launch {
            val result = restaurantRepository.getAllRestaurants()
            
            binding.swipeRefresh.isRefreshing = false
            binding.progressBar.visibility = View.GONE
            
            result.onSuccess { restaurants ->
                if (restaurants.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.recyclerViewRestaurants.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerViewRestaurants.visibility = View.VISIBLE
                    adapter.submitList(restaurants)
                }
            }
            
            result.onFailure { error ->
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load restaurants: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.emptyState.visibility = View.VISIBLE
            }
        }
    }
    
    override fun onRestaurantClick(restaurant: Restaurant) {
        val intent = Intent(this, RestaurantDetailActivity::class.java)
        intent.putExtra("restaurant_id", restaurant.id)
        intent.putExtra("restaurant_name", restaurant.name)
        intent.putExtra("restaurant_address", restaurant.address)
        intent.putExtra("restaurant_category", restaurant.category)
        startActivity(intent)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_my_orders -> {
                Toast.makeText(this, "My Orders - Coming Soon", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_track_order -> {
                startActivity(Intent(this, OrderTrackingActivity::class.java))
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun logout() {
        val prefs = getSharedPreferences("quickbite_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
