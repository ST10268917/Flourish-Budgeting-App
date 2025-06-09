package com.aj.flourish.base

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.aj.flourish.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    // Call this in your child activities after setting content view
    protected fun setupNavigation() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        bottomNav = findViewById(R.id.bottomNavigationView)

        val menuIcon = findViewById<ImageView?>(R.id.ivMenu)
        menuIcon?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Side menu item handling
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_currency_converter -> {
                    startActivity(Intent(this, CurrencyConverterActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_calculator -> {
                    startActivity(Intent(this, CalculatorActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_achievements -> {
                    startActivity(Intent(this, AchievementsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }

        // Bottom nav item handling
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (this !is Dashboard) { // Prevent reopening if already on Dashboard
                        startActivity(Intent(this, Dashboard::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                        overridePendingTransition(0, 0)
                    }

                    true
                }
                R.id.nav_create_category -> {
                    startActivity(Intent(this, CreateCategory::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_expenses -> {
                    startActivity(Intent(this, FilterExpensesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_category_spending -> {
                    startActivity(Intent(this, CategorySpendingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    override fun setContentView(layoutResID: Int) {
        val fullLayout = layoutInflater.inflate(R.layout.base_layout, null)
        val container = fullLayout.findViewById<FrameLayout>(R.id.container)

        layoutInflater.inflate(layoutResID, container, true)
        super.setContentView(fullLayout)

        setupNavigation()
    }
}
