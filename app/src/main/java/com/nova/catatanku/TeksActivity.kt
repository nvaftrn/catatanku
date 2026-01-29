package com.nova.catatanku

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

class TeksActivity : AppCompatActivity() {

    private lateinit var etJudul: EditText
    private lateinit var etCatatan: EditText
    private lateinit var layoutWaktu: LinearLayout
    private lateinit var tvJam: TextView
    private lateinit var btnPin: ImageView
    private lateinit var btnArsip: ImageView

    private var waktuPengingat: String? = null
    private var isPinned: Boolean = false
    private var isEdit = false
    private var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teks)

        etJudul = findViewById(R.id.etJudul)
        etCatatan = findViewById(R.id.etCatatan)
        layoutWaktu = findViewById(R.id.layoutWaktuTerpilih)
        tvJam = findViewById(R.id.tvJamPengingat)
        btnPin = findViewById(R.id.btnPin)
        btnArsip = findViewById(R.id.btnArsip)

        val btnBack = findViewById<ImageView>(R.id.btnBackTeks)
        val btnNotif = findViewById<ImageView>(R.id.btnNotif)
        val btnMenuOpsi = findViewById<ImageView>(R.id.btnMenuOpsi)

        // --- MODE EDIT ---
        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        if (isEdit) {
            position = intent.getIntExtra("POSITION", -1)
            etJudul.setText(intent.getStringExtra("EXTRA_JUDUL"))
            etCatatan.setText(intent.getStringExtra("EXTRA_ISI"))
            waktuPengingat = intent.getStringExtra("EXTRA_WAKTU")
            isPinned = intent.getBooleanExtra("EXTRA_PIN", false)

            if (!waktuPengingat.isNullOrEmpty()) {
                tvJam.text = waktuPengingat
                layoutWaktu.visibility = View.VISIBLE
            }
        }

        updatePinUI()

        btnPin.setOnClickListener {
            isPinned = !isPinned
            updatePinUI()
        }

        // --- LOGIKA MENU OPSI (SALIN & BAGIKAN SAJA) ---
        btnMenuOpsi.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.layout_opsi_catatan, null)

            val menuSalin = view.findViewById<LinearLayout>(R.id.btnSalin)
            val menuBagikan = view.findViewById<LinearLayout>(R.id.btnBagikan)

            // 1. Logika Salin
            menuSalin.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val teksSalin = "Judul: ${etJudul.text}\n\n${etCatatan.text}"
                val clip = android.content.ClipData.newPlainText("CatatanKu", teksSalin)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Catatan disalin ke clipboard", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            // 2. Logika Bagikan
            menuBagikan.setOnClickListener {
                val teksBagikan = "Judul: ${etJudul.text}\n\n${etCatatan.text}"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, teksBagikan)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Bagikan via")
                startActivity(shareIntent)
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.show()
        }

        // --- LOGIKA ARSIP (TETAP DI TOMBOL ATAS) ---
        btnArsip.setOnClickListener {
            if (isEdit && position != -1) {
                if (position < DataManager.listCatatan.size) {
                    val currentNote = DataManager.listCatatan[position]
                    currentNote.judul = etJudul.text.toString()
                    currentNote.isi = etCatatan.text.toString()
                    currentNote.waktuPengingat = waktuPengingat
                    currentNote.isPinned = isPinned

                    DataManager.listArsip.add(currentNote)
                    DataManager.listCatatan.removeAt(position)

                    DataManager.saveData(this)
                    Toast.makeText(this, "Catatan diarsipkan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Simpan dahulu sebelum mengarsip", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener { simpanDanKembali() }
        btnNotif.setOnClickListener { showDateTimePicker() }

        layoutWaktu.setOnClickListener {
            waktuPengingat = null
            layoutWaktu.visibility = View.GONE
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                simpanDanKembali()
            }
        })
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

    private fun simpanDanKembali() {
        val judul = etJudul.text.toString()
        val isi = etCatatan.text.toString()

        if (judul.isNotEmpty() || isi.isNotEmpty()) {
            if (isEdit && position != -1 && position < DataManager.listCatatan.size) {
                val noteUpdate = DataManager.listCatatan[position]
                noteUpdate.judul = judul
                noteUpdate.isi = isi
                noteUpdate.waktuPengingat = waktuPengingat
                noteUpdate.isPinned = isPinned
                DataManager.saveData(this)
            } else if (!isEdit) {
                val resultIntent = Intent()
                resultIntent.putExtra("EXTRA_JUDUL", judul)
                resultIntent.putExtra("EXTRA_ISI", isi)
                resultIntent.putExtra("EXTRA_WAKTU", waktuPengingat)
                resultIntent.putExtra("EXTRA_PIN", isPinned)
                setResult(RESULT_OK, resultIntent)
            }
        }
        finish()
    }
}