package com.nova.catatanku

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

        btnLogin.setOnClickListener {
            val userIn = etUsername.text.toString().trim()
            val passIn = etPassword.text.toString().trim()

            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

            // Mengosongkan default value (menjadi null jika tidak ada data)
            val userSaved = sharedPref.getString("USERNAME", null)
            val passSaved = sharedPref.getString("PASSWORD", null)

            // Logika Pengecekan
            if (userSaved == null || passSaved == null) {
                // Jika belum ada data sama sekali di memori
                Toast.makeText(this, "Akun tidak ditemukan. Silakan daftar dulu.", Toast.LENGTH_SHORT).show()
            } else if (userIn == userSaved && passIn == passSaved) {
                // Jika input cocok dengan data yang didaftarkan/diubah
                Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                // Jika salah
                Toast.makeText(this, "Username atau Password salah!", Toast.LENGTH_SHORT).show()
            }
        }

        tvToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}