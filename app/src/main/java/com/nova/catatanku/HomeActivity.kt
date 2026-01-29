package com.nova.catatanku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeActivity : AppCompatActivity() {

    private lateinit var rvCatatan: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var etSearch: EditText

    private var listTampil = mutableListOf<Note>()

    private val startNoteActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val judul = data?.getStringExtra("EXTRA_JUDUL") ?: ""
            val isi = data?.getStringExtra("EXTRA_ISI") ?: ""
            val waktu = data?.getStringExtra("EXTRA_WAKTU")
            // Ambil status pin dari catatan baru jika ada
            val isPinned = data?.getBooleanExtra("EXTRA_PIN", false) ?: false

            if (judul.isNotEmpty() || isi.isNotEmpty()) {
                val noteBaru = Note(judul, isi, waktu, isPinned)
                DataManager.listCatatan.add(noteBaru)
                DataManager.saveData(this)
                updateUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        DataManager.loadData(this)

        drawerLayout = findViewById(R.id.drawer_layout)
        rvCatatan = findViewById(R.id.rvNotes)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        etSearch = findViewById(R.id.etSearch)

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val btnTeks = findViewById<LinearLayout>(R.id.btnTambahTeks)
        val btnDaftar = findViewById<LinearLayout>(R.id.btnNavDaftar)
        val btnGambar = findViewById<LinearLayout>(R.id.btnNavGambar)

        btnMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        ivProfile.setOnClickListener { startActivity(Intent(this, ProfilActivity::class.java)) }
        btnTeks.setOnClickListener { startNoteActivity.launch(Intent(this, TeksActivity::class.java)) }
        btnDaftar.setOnClickListener { startNoteActivity.launch(Intent(this, DaftarActivity::class.java)) }
        btnGambar.setOnClickListener { showImagePopup() }

        // Sidebar Menu
        findViewById<LinearLayout>(R.id.menuPengingat).setOnClickListener {
            startActivity(Intent(this, PengingatActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<LinearLayout>(R.id.menuArsip).setOnClickListener {
            startActivity(Intent(this, ArsipActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<LinearLayout>(R.id.menuSampah).setOnClickListener {
            startActivity(Intent(this, SampahActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCatatan(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(listTampil,
            onLongClick = { position -> showDeleteConfirmation(position) },
            onClick = { _ -> }
        )
        rvCatatan.layoutManager = LinearLayoutManager(this)
        rvCatatan.adapter = adapter
    }

    private fun filterCatatan(query: String) {
        listTampil.clear()
        if (query.isEmpty()) {
            // Urutkan ulang list utama agar PIN tetap di atas sebelum ditampilkan kembali
            DataManager.listCatatan.sortWith(compareByDescending<Note> { it.isPinned }
                .thenByDescending { it.judul }) // Secondary sort agar rapi
            listTampil.addAll(DataManager.listCatatan)
        } else {
            val hasilFilter = DataManager.listCatatan.filter {
                it.judul.contains(query, ignoreCase = true) || it.isi.contains(query, ignoreCase = true)
            }
            listTampil.addAll(hasilFilter)
        }
        adapter.notifyDataSetChanged()
        toggleEmptyLayout()
    }

    private fun showDeleteConfirmation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Catatan")
            .setMessage("Pindahkan catatan ini ke Sampah?")
            .setPositiveButton("Ya") { _, _ ->
                val noteDihapus = listTampil[position]
                DataManager.listSampah.add(noteDihapus)
                DataManager.listCatatan.remove(noteDihapus)
                DataManager.saveData(this)
                updateUI()
                Toast.makeText(this, "Dipindahkan ke Sampah", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateUI() {
        // --- LOGIKA UTAMA PIN ---
        // Urutkan listCatatan secara permanen: isPinned true (1) akan di atas false (0)
        DataManager.listCatatan.sortWith(compareByDescending<Note> { it.isPinned })

        if (etSearch.text.isEmpty()) {
            listTampil.clear()
            listTampil.addAll(DataManager.listCatatan)
            adapter.notifyDataSetChanged()
        }
        toggleEmptyLayout()
    }

    private fun toggleEmptyLayout() {
        if (listTampil.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvCatatan.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvCatatan.visibility = View.VISIBLE
        }
    }

    private fun showImagePopup() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_popup_gambar, null)
        view.findViewById<LinearLayout>(R.id.btnAmbilFoto)?.setOnClickListener {
            startNoteActivity.launch(Intent(this, GambarActivity::class.java).apply { putExtra("ACTION", "CAMERA") })
            dialog.dismiss()
        }
        view.findViewById<LinearLayout>(R.id.btnPilihGambar)?.setOnClickListener {
            startNoteActivity.launch(Intent(this, GambarActivity::class.java).apply { putExtra("ACTION", "GALLERY") })
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        DataManager.loadData(this)
        updateUI() // Ini akan memaksa sorting ulang setiap kali kembali ke Home
    }
}