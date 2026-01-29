package com.nova.catatanku

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class DaftarActivity : AppCompatActivity() {

    private lateinit var layoutListItems: LinearLayout
    private lateinit var etJudulDaftar: EditText
    private lateinit var btnMenuOpsi: ImageView
    private lateinit var btnPin: ImageView
    private lateinit var btnArsip: ImageView

    private lateinit var layoutWaktu: LinearLayout
    private lateinit var tvJam: TextView

    private var isEdit = false
    private var position = -1
    private var waktuPengingat: String? = null
    private var isPinned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar)

        layoutListItems = findViewById(R.id.layoutListItems)
        etJudulDaftar = findViewById(R.id.etJudulDaftar)
        val btnAddItem = findViewById<LinearLayout>(R.id.btnAddItem)
        val btnBack = findViewById<ImageView>(R.id.btnBackDaftar)
        val btnNotif = findViewById<ImageView>(R.id.btnNotifDaftar)

        btnMenuOpsi = findViewById(R.id.btnOpsi)
        btnPin = findViewById(R.id.btnPinDaftar)
        btnArsip = findViewById(R.id.btnArsipDaftar)

        layoutWaktu = findViewById(R.id.layoutWaktuDaftar)
        tvJam = findViewById(R.id.tvJamDaftar)

        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        if (isEdit) {
            position = intent.getIntExtra("POSITION", -1)
            val judulLama = intent.getStringExtra("EXTRA_JUDUL")
            val isiLama = intent.getStringExtra("EXTRA_ISI")
            waktuPengingat = intent.getStringExtra("EXTRA_WAKTU")
            isPinned = intent.getBooleanExtra("EXTRA_PIN", false)

            etJudulDaftar.setText(judulLama)

            if (!waktuPengingat.isNullOrEmpty()) {
                tvJam.text = waktuPengingat
                layoutWaktu.visibility = View.VISIBLE
            }

            val barisDaftar = isiLama?.split("\n") ?: listOf()
            if (barisDaftar.isNotEmpty()) {
                for (baris in barisDaftar) {
                    val teksMurni = baris.removePrefix("- ").trim()
                    if (teksMurni.isNotEmpty()) {
                        addNewItem(teksMurni)
                    }
                }
            }
        } else {
            addNewItem()
        }

        updatePinUI()

        btnPin.setOnClickListener {
            isPinned = !isPinned
            updatePinUI()
        }

        btnArsip.setOnClickListener { arsipCatatan() }

        btnNotif.setOnClickListener { showDateTimePicker() }

        // --- LOGIKA MENU OPSI (SALIN & BAGIKAN) ---
        btnMenuOpsi.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.layout_opsi_catatan, null)

            val menuSalin = view.findViewById<LinearLayout>(R.id.btnSalin)
            val menuBagikan = view.findViewById<LinearLayout>(R.id.btnBagikan)

            // 1. Logika Salin
            menuSalin.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val teksDaftar = "JUDUL: ${etJudulDaftar.text}\n\n${kumpulkanIsiDaftar()}"
                val clip = android.content.ClipData.newPlainText("DaftarKu", teksDaftar)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Daftar disalin ke clipboard", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            // 2. Logika Bagikan
            menuBagikan.setOnClickListener {
                val teksBagikan = "JUDUL: ${etJudulDaftar.text}\n\n${kumpulkanIsiDaftar()}"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, teksBagikan)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Bagikan via"))
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.show()
        }

        layoutWaktu.setOnClickListener {
            waktuPengingat = null
            layoutWaktu.visibility = View.GONE
        }

        btnAddItem.setOnClickListener { addNewItem() }
        btnBack.setOnClickListener { simpanDanKembali() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                simpanDanKembali()
            }
        })
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedTime = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, hourOfDay, minute)
                }
                val timeString = String.format("%02d:%02d", hourOfDay, minute)
                val diffDays = selectedTime.get(Calendar.DAY_OF_YEAR) - Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                val labelHari = when {
                    diffDays == 0 -> "Hari ini"
                    diffDays == 1 -> "Besok"
                    else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(selectedTime.time)
                }
                waktuPengingat = "$labelHari, $timeString"
                tvJam.text = waktuPengingat
                layoutWaktu.visibility = View.VISIBLE
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun updatePinUI() {
        if (isPinned) {
            btnPin.setImageResource(R.drawable.pin_aktif)
            btnPin.setColorFilter(resources.getColor(R.color.primary_dark))
        } else {
            btnPin.setImageResource(R.drawable.pin)
            btnPin.setColorFilter(resources.getColor(R.color.grey_text))
        }
    }

    private fun arsipCatatan() {
        if (isEdit && position != -1) {
            val judul = etJudulDaftar.text.toString()
            val isi = kumpulkanIsiDaftar()
            if (judul.isNotEmpty() || isi.isNotEmpty()) {
                val noteArsip = Note(judul, isi, waktuPengingat, isPinned)
                DataManager.listArsip.add(noteArsip)
                DataManager.listCatatan.removeAt(position)
                DataManager.saveData(this)
                Toast.makeText(this, "Daftar diarsipkan", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Simpan dahulu sebelum mengarsip", Toast.LENGTH_SHORT).show()
        }
    }

    private fun kumpulkanIsiDaftar(): String {
        val listData = mutableListOf<String>()
        for (i in 0 until layoutListItems.childCount) {
            val itemView = layoutListItems.getChildAt(i)
            val etItem = itemView.findViewById<EditText>(R.id.etItemChecklist)
            val teksItem = etItem.text.toString()
            if (teksItem.isNotEmpty()) {
                listData.add("- $teksItem")
            }
        }
        return listData.joinToString("\n")
    }

    private fun addNewItem(teksAwal: String = "") {
        val view = LayoutInflater.from(this).inflate(R.layout.item_checklist, layoutListItems, false)
        val etItem = view.findViewById<EditText>(R.id.etItemChecklist)
        val btnRemove = view.findViewById<ImageView>(R.id.btnRemoveItem)
        etItem.setText(teksAwal)
        btnRemove.setOnClickListener { layoutListItems.removeView(view) }
        layoutListItems.addView(view)
    }

    private fun simpanDanKembali() {
        val judul = etJudulDaftar.text.toString()
        val isiCatatan = kumpulkanIsiDaftar()
        if (judul.isNotEmpty() || isiCatatan.isNotEmpty()) {
            if (isEdit && position != -1) {
                val noteUpdate = Note(judul, isiCatatan, waktuPengingat, isPinned)
                DataManager.listCatatan[position] = noteUpdate
                DataManager.saveData(this)
            } else {
                val resultIntent = Intent()
                resultIntent.putExtra("EXTRA_JUDUL", judul)
                resultIntent.putExtra("EXTRA_ISI", isiCatatan)
                resultIntent.putExtra("EXTRA_WAKTU", waktuPengingat)
                resultIntent.putExtra("EXTRA_PIN", isPinned)
                setResult(RESULT_OK, resultIntent)
            }
        }
        finish()
    }
}