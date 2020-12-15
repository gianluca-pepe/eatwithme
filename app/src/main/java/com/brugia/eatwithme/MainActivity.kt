package com.brugia.eatwithme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var destinationListener: NavController.OnDestinationChangedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            this.finish()
        }


        val navHostFragment = supportFragmentManager.
            findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        drawerLayout = findViewById(R.id.drawer_layout)

        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)

        // Fragments in which action bar will have hamburger icon instead of back arrow
        val topLevelDestinations = setOf(
            R.id.mainFragment,
        )
        appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.menu.findItem(R.id.logout).setOnMenuItemClickListener{ _ ->
            this.logout()
            return@setOnMenuItemClickListener true
        }

    }

    private fun logout(){
        Firebase.auth.signOut()
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        this.finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}