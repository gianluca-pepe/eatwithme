package com.brugia.eatwithme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
            this.finish()
        }

        val btn_logout = findViewById<Button>(R.id.btn_logout)
        // set on-click listener
        btn_logout.setOnClickListener {
           logout()
        }

    }
    private fun logout(){
        Firebase.auth.signOut()
        startActivity(Intent(this, LoginRegisterActivity::class.java))
        this.finish()
    }
}