package com.nova.catatanku

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PengingatActivity : AppCompatActivity() {

    private lateinit var rvPengingat: RecyclerView
    private lateinit var layoutEmptyPengingat: LinearLayout
    private lateinit var adapter: NoteAdapter
    private var dataTerfilter = mutableListOf<Note>() // List lokal untuk adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengingat)

        rvPengingat = findViewById(R.id.rvPengingat)
        layoutEmptyPengingat = findViewById(R.id.layoutEmptyPengingat)
        val btnBack = findViewById<ImageView>(R.id.btnBackPengingat)

        btnBack.setOnClickListener { finish() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Ambil data dari DataManager yang hanya ada pengingatnya
        dataTerfilter = DataManager.getDataPengingat()

        adapter = NoteAdapter(
            dataTerfilter,
            onLongClick = { position ->
                showDeleteDialog(position)
            },
            onClick = { position ->
                val note = dataTerfilter[position]
                Toast.makeText(this, "Melihat: ${note.judul}", Toast.LENGTH_SHORT).show()
            }
        )

        rvPengingat.layoutManager = LinearLayoutManager(this)
        rvPengingat.adapter = adapter

        updateUI()
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Pengingat")
            .setMessage("Apakah Anda ingin menghapus pengingat ini? (Catatan tetap ada di Beranda)")
            .setPositiveButton("Hapus") { _, _ ->
                val note = dataTerfilter[position]

                // Mencari note yang sama di list utama
                val originalNote = DataManager.listCatatan.find { it == note }

                // Sekarang ini sudah tidak error karena sudah jadi 'var'
                originalNote?.waktuPengingat = null

                // PENTING: Simpan perubahan ke memori HP
                DataManager.saveData(this)

                // Hapus dari tampilan lokal (list pengingat)
                dataTerfilter.removeAt(position)
                adapter.notifyItemRemoved(position)

                updateUI()
                Toast.makeText(this, "Pengingat dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUI() {
        if (dataTerfilter.isEmpty()) {
            layoutEmptyPengingat.visibility = View.VISIBLE
            rvPengingat.visibility = View.GONE
        } else {
            layoutEmptyPengingat.visibility = View.GONE
            rvPengingat.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Segarkan data setiap kali masuk ke halaman ini
        setupRecyclerView()
    }
}