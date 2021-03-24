package com.brugia.eatwithme

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.brugia.eatwithme.data.user.UserRepository
import com.brugia.eatwithme.location.LocationViewModel
import com.brugia.eatwithme.location.LocationViewModelFactory
import com.brugia.eatwithme.myprofile.MyProfileViewModel
import com.brugia.eatwithme.myprofile.MyProfileViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    // private lateinit var drawerLayout: DrawerLayout
    // private lateinit var destinationListener: NavController.OnDestinationChangedListener
    private val personViewModel: MyProfileViewModel by viewModels {
        MyProfileViewModelFactory(this.application)
    }
    private val locationViewModel by viewModels<LocationViewModel> {
        LocationViewModelFactory(this.application)
    }


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
        //drawerLayout = findViewById(R.id.drawer_layout)

        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

        // Fragments in which action bar will have hamburger icon instead of back arrow
        //val topLevelDestinations = setOf(
         //       R.id.mainFragment,
        //)
        //appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)
        //setupActionBarWithNavController(navController, appBarConfiguration)

        val navigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        navigationView.menu.findItem(R.id.createTablePagerFragment).setOnMenuItemClickListener{
            onCreateTableNavigation()
            return@setOnMenuItemClickListener true
        }

        // retrieve custom location previously stored
        val sharedPreferences = this.getSharedPreferences(
            getString(R.string.custom_location_file_key),
            MODE_PRIVATE
        )

        val lat = sharedPreferences.getFloat(getString(R.string.latitude),0F)
        val long = sharedPreferences.getFloat(getString(R.string.longitude),0F)

        // if location has been previously stored, set it as actual location
        if (lat != 0F && long != 0F) {
            val location = Location("")
            location.latitude = lat.toDouble()
            location.longitude = long.toDouble()
            locationViewModel.setLocation(location) // now accessible in any fragment
            println("previously set position found")
        } else {
            locationViewModel.setLocation(null)
        }

        println("activity onstart")
    }

    private fun logout(){
        Firebase.auth.signOut()
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        this.finish()
    }

    private fun onCreateTableNavigation() {
        val person = personViewModel.myprofileLiveData.value
        println(person)
        if ( person == null || person.isProfileIncomplete() ) {
            val alert = AlertDialog.Builder(this)
            alert
                    .setTitle(R.string.incomplete_account_title)
                    .setMessage(R.string.incomplete_account_message)
                    .setNegativeButton(R.string.not_now) { _, _ -> }
                    .setPositiveButton(R.string.incomplete_account_pos_button) { _, _ ->
                        // user accept to update profile, go to profile settings fragment
                        navController.navigate(R.id.myProfileSettingsFragment)
                    }
                    .create().show()
        } else {
            navController.navigate(R.id.createTablePagerFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        // delete all Volley requests in the queue
        //userRepository.queue.cancelAll(this.application)
    }
}