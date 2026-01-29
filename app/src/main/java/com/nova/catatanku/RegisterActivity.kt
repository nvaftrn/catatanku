package com.nova.catatanku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnDaftar = findViewById<Button>(R.id.btnDaftar)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)

        btnDaftar.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()

                // USERNAME & PASSWORD untuk login
                editor.putString("USERNAME", username)
                editor.putString("PASSWORD", password)

                // NAMA untuk tampilan profil (bisa diganti-ganti nanti)
                editor.putString("NAMA", username)

                editor.apply()

                Toast.makeText(this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                if (username.isEmpty()) etUsername.error = "Username tidak boleh kosong"
                if (password.isEmpty()) etPassword.error = "Password tidak boleh kosong"
            }
        }

        tvToLogin.setOnClickListener {
            finish()
        }
    }
}