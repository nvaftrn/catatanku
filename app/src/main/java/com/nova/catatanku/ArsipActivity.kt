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

class ArsipActivity : AppCompatActivity() {

    private lateinit var rvArsip: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var layoutEmpty: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arsip)

        rvArsip = findViewById(R.id.rvArsip)
        layoutEmpty = findViewById(R.id.layoutEmptyArsip)
        val btnBack = findViewById<ImageView>(R.id.btnBackArsip)

        btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        updateUI()
    }

    private fun setupRecyclerView() {
        // Menggunakan listArsip dari DataManager
        adapter = NoteAdapter(DataManager.listArsip,
            onLongClick = { position ->
                // Opsi: Hapus permanen jika ditekan lama
                showDeletePermanentDialog(position)
            },
            onClick = { position ->
                // Logika UNARCHIVE: Kembalikan ke Beranda
                showUnarchiveDialog(position)
            }
        )
        rvArsip.layoutManager = LinearLayoutManager(this)
        rvArsip.adapter = adapter
    }

    private fun showUnarchiveDialog(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Buka dari Arsip")
        builder.setMessage("Apakah Anda ingin mengembalikan catatan ini ke halaman utama?")

        builder.setPositiveButton("Ya, Kembalikan") { _, _ ->
            val note = DataManager.listArsip[position]

            // 1. Tambahkan kembali ke list utama
            DataManager.listCatatan.add(note)

            // 2. Hapus dari list arsip
            DataManager.listArsip.removeAt(position)

            // 3. Simpan perubahan ke memori HP
            DataManager.saveData(this)

            adapter.notifyItemRemoved(position)
            updateUI()
            Toast.makeText(this, "Catatan kembali ke Beranda", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun showDeletePermanentDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Permanen")
            .setMessage("Hapus catatan ini selamanya?")
            .setPositiveButton("Hapus") { _, _ ->
                DataManager.listArsip.removeAt(position)
                DataManager.saveData(this)
                adapter.notifyItemRemoved(position)
                updateUI()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUI() {
        if (DataManager.listArsip.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvArsip.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvArsip.visibility = View.VISIBLE
        }
    }
}