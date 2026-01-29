package com.nova.catatanku

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SampahActivity : AppCompatActivity() {

    private lateinit var rvSampah: RecyclerView
    private lateinit var layoutEmptySampah: LinearLayout
    private lateinit var adapter: NoteAdapter
    private lateinit var btnHapusSemua: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sampah)

        // 1. Inisialisasi View
        rvSampah = findViewById(R.id.rvSampah)
        layoutEmptySampah = findViewById(R.id.layoutEmptySampah)
        btnHapusSemua = findViewById(R.id.btnHapusSemua)
        val btnBack = findViewById<ImageView>(R.id.btnBackSampah)

        // 2. Tombol Kembali
        btnBack.setOnClickListener {
            finish()
        }

        // 3. Tombol Hapus Semua
        btnHapusSemua.setOnClickListener {
            if (DataManager.listSampah.isNotEmpty()) {
                showDeleteAllDialog()
            }
        }

        setupRecyclerView()
        updateUI()
    }

    private fun setupRecyclerView() {
        // Menggunakan list dari DataManager.listSampah
        adapter = NoteAdapter(DataManager.listSampah,
            onLongClick = { position ->
                showOptionsDialog(position)
            },
            onClick = {
                Toast.makeText(this, "Tekan lama untuk memulihkan atau hapus", Toast.LENGTH_SHORT).show()
            }
        )
        rvSampah.layoutManager = LinearLayoutManager(this)
        rvSampah.adapter = adapter
    }

    private fun showOptionsDialog(position: Int) {
        val options = arrayOf("Pulihkan Catatan", "Hapus Permanen")

        AlertDialog.Builder(this)
            .setTitle("Opsi Sampah")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pulihkanCatatan(position)
                    1 -> hapusPermanen(position)
                }
            }
            .show()
    }

    private fun pulihkanCatatan(position: Int) {
        // Pastikan posisi masih valid
        if (position >= 0 && position < DataManager.listSampah.size) {
            val note = DataManager.listSampah[position]

            // Pindahkan data
            DataManager.listCatatan.add(note)
            DataManager.listSampah.removeAt(position)

            // Update adapter dengan animasi
            adapter.notifyItemRemoved(position)

            // Update UI setelah delay sedikit agar animasi selesai
            rvSampah.postDelayed({ updateUI() }, 300)

            Toast.makeText(this, "Catatan dipulihkan ke Beranda", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hapusPermanen(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Permanen")
            .setMessage("Catatan ini akan dihapus selamanya. Lanjutkan?")
            .setPositiveButton("Hapus") { _, _ ->
                if (position >= 0 && position < DataManager.listSampah.size) {
                    DataManager.listSampah.removeAt(position)
                    adapter.notifyItemRemoved(position)

                    rvSampah.postDelayed({ updateUI() }, 300)
                    Toast.makeText(this, "Dihapus permanen", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Kosongkan Sampah")
            .setMessage("Hapus SEMUA catatan secara permanen?")
            .setPositiveButton("Ya, Kosongkan") { _, _ ->
                DataManager.listSampah.clear()
                adapter.notifyDataSetChanged()
                updateUI()
                Toast.makeText(this, "Sampah telah dibersihkan", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUI() {
        if (DataManager.listSampah.isEmpty()) {
            layoutEmptySampah.visibility = View.VISIBLE
            rvSampah.visibility = View.GONE
            btnHapusSemua.visibility = View.GONE
        } else {
            layoutEmptySampah.visibility = View.GONE
            rvSampah.visibility = View.VISIBLE
            btnHapusSemua.visibility = View.VISIBLE
            // Kita tidak memanggil notifyDataSetChanged di sini jika sudah panggil notifyItemRemoved
        }
    }
}