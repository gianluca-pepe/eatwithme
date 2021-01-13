package com.brugia.eatwithme

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var destinationListener: NavController.OnDestinationChangedListener
    private val personViewModel: MyProfileViewModel = MyProfileViewModel()
    private lateinit var requestLocationPermissionLauncher : ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            this.finish()
        }else{
            personViewModel.checkPersonData()//check if person data are loaded and load them
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

        requestLocationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // Callback called when the user interacts with system dialog requesting permission
            if (!isGranted) {
                // retrieve custom location previously stored
                sharedPreferences = getSharedPreferences(getString(R.string.custom_location_file_key), MODE_PRIVATE)
                val lat = sharedPreferences.getFloat(
                        getString(R.string.latitude),
                        0F
                )
                val long = sharedPreferences.getFloat(
                        getString(R.string.longitude),
                        0F
                )

                // if location has never been stored, ask the user to manually set it
                if (lat == 0F || long == 0F) {
                    AlertDialog.Builder(this)
                            .setTitle(R.string.missing_location_title)
                            .setMessage(R.string.missing_location_message)
                            .setPositiveButton(R.string.missing_location_pos_button) { _, _ ->
                                navController.navigate(R.id.mapsFragment)
                            }.create().show()
                }
            }
        }
        checkLocationPermission()
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

    fun checkLocationPermission() {
        when {
            isPermissionGranted() -> { }
            //shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {}
            else -> {
                // You can directly ask for the permission.
                // onRequestPermissionsResult(...) gets the result of this request.
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    fun isPermissionGranted(): Boolean =
            ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}