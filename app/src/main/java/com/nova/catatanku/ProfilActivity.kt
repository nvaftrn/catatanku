package com.nova.catatanku

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        // 1. Inisialisasi ID sesuai XML
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val menuLogout = findViewById<LinearLayout>(R.id.menuLogout)
        val btnEditFoto = findViewById<TextView>(R.id.btnEditFoto)
        val tvNamaProfil = findViewById<TextView>(R.id.tvNamaProfil)
        val menuUbahPassword = findViewById<LinearLayout>(R.id.menuUbahPassword)

        // Ambil parent dari tvNamaProfil agar baris nama bisa diklik untuk edit
        val menuEditNama = tvNamaProfil.parent as LinearLayout

        // 2. Ambil data dari SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Gunakan key "NAMA" agar sinkron dengan RegisterActivity
        val namaUser = sharedPreferences.getString("NAMA", "Nova Fitriani")
        tvNamaProfil.text = namaUser

        // 3. Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // 4. Fitur Ubah Nama (Klik pada baris nama)
        menuEditNama.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ubah Nama Lengkap")

            val input = EditText(this)
            input.setText(tvNamaProfil.text.toString()) // Tampilkan nama saat ini
            input.setSelection(input.text.length) // Kursor di akhir teks
            builder.setView(input)

            builder.setPositiveButton("Simpan") { _, _ ->
                val namaBaru = input.text.toString().trim()
                if (namaBaru.isNotEmpty()) {
                    sharedPreferences.edit().putString("NAMA", namaBaru).apply()
                    tvNamaProfil.text = namaBaru
                    Toast.makeText(this, "Nama diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Batal", null)
            builder.show()
        }

        // 5. Tombol Ubah Password
        menuUbahPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ubah Password")

            val input = EditText(this)
            input.hint = "Password Baru"
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            builder.setView(input)

            builder.setPositiveButton("Simpan") { _, _ ->
                val passBaru = input.text.toString()
                if (passBaru.isNotEmpty()) {
                    sharedPreferences.edit().putString("PASSWORD", passBaru).apply()
                    Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Batal", null)
            builder.show()
        }

        // 6. Tombol Logout
        menuLogout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("IS_LOGGED_IN", false)
            editor.apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // 7. Tombol Edit Foto
        btnEditFoto.setOnClickListener {
            Toast.makeText(this, "Fitur foto segera hadir", Toast.LENGTH_SHORT).show()
        }
    }
}