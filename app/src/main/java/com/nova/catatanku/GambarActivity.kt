package com.nova.catatanku

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GambarActivity : AppCompatActivity() {

    private lateinit var ivPreviewGambar: ImageView
    private lateinit var etJudulGambar: EditText
    private lateinit var etCatatanGambar: EditText
    private lateinit var btnPin: ImageView
    private lateinit var btnArsip: ImageView

    private lateinit var layoutWaktu: LinearLayout
    private lateinit var tvJam: TextView

    private var imageUri: Uri? = null
    private var tempCameraUri: Uri? = null

    private var isEdit = false
    private var position = -1
    private var waktuPengingat: String? = null
    private var isPinned = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedUri = result.data?.data
            if (selectedUri != null) {
                imageUri = selectedUri
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(selectedUri, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ivPreviewGambar.setImageURI(imageUri)
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = tempCameraUri
            ivPreviewGambar.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gambar)

        ivPreviewGambar = findViewById(R.id.ivPreviewGambar)
        etJudulGambar = findViewById(R.id.etJudulGambar)
        etCatatanGambar = findViewById(R.id.etCatatanGambar)
        btnPin = findViewById(R.id.btnPinGambar)
        btnArsip = findViewById(R.id.btnArsipGambar)
        val btnBack = findViewById<ImageView>(R.id.btnBackGambar)
        val btnNotif = findViewById<ImageView>(R.id.btnNotifGambar)
        val btnMenuOpsi = findViewById<ImageView>(R.id.btnOpsi) // Menambahkan inisialisasi btnOpsi

        layoutWaktu = findViewById(R.id.layoutWaktuGambar)
        tvJam = findViewById(R.id.tvJamGambar)

        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        val actionType = intent.getStringExtra("ACTION")

        if (isEdit) {
            position = intent.getIntExtra("POSITION", -1)
            val judulLama = intent.getStringExtra("EXTRA_JUDUL")
            val isiLama = intent.getStringExtra("EXTRA_ISI")
            waktuPengingat = intent.getStringExtra("EXTRA_WAKTU")
            isPinned = intent.getBooleanExtra("EXTRA_PIN", false)

            etJudulGambar.setText(judulLama)

            if (!waktuPengingat.isNullOrEmpty()) {
                tvJam.text = waktuPengingat
                layoutWaktu.visibility = View.VISIBLE
            }

            if (isiLama?.startsWith("content://") == true || isiLama?.startsWith("file://") == true) {
                try {
                    imageUri = Uri.parse(isiLama)
                    ivPreviewGambar.setImageURI(imageUri)
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                }
            } else {
                etCatatanGambar.setText(isiLama)
            }
        } else {
            when (actionType) {
                "CAMERA" -> bukaKamera()
                "GALLERY" -> bukaGaleri()
            }
        }

        updatePinUI()

        // --- LOGIKA MENU OPSI (SALIN & BAGIKAN) ---
        btnMenuOpsi?.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.layout_opsi_catatan, null)

            val menuSalin = view.findViewById<LinearLayout>(R.id.btnSalin)
            val menuBagikan = view.findViewById<LinearLayout>(R.id.btnBagikan)

            menuSalin.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val teksSalin = "Judul: ${etJudulGambar.text}\nKeterangan: ${etCatatanGambar.text}"
                val clip = android.content.ClipData.newPlainText("CatatanKu", teksSalin)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Teks berhasil disalin", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            menuBagikan.setOnClickListener {
                val teksBagikan = "Judul: ${etJudulGambar.text}\n\n${etCatatanGambar.text}"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, teksBagikan)
                    // Jika ingin berbagi gambar juga, diperlukan konfigurasi FileProvider tambahan
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Bagikan via"))
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.show()
        }

        btnPin.setOnClickListener { isPinned = !isPinned; updatePinUI() }
        btnArsip.setOnClickListener { arsipGambar() }
        btnNotif.setOnClickListener { showDateTimePicker() }
        layoutWaktu.setOnClickListener { waktuPengingat = null; layoutWaktu.visibility = View.GONE }
        ivPreviewGambar.setOnClickListener { bukaGaleri() }
        btnBack.setOnClickListener { simpanDanKembali() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { simpanDanKembali() }
        })
    }

    private fun bukaKamera() {
        val photoFile = File(filesDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        tempCameraUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            photoFile
        )
        tempCameraUri?.let { takePhotoLauncher.launch(it) }
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

    private fun arsipGambar() {
        if (isEdit && position != -1) {
            val judul = etJudulGambar.text.toString().trim()
            val dataIsi = imageUri?.toString() ?: etCatatanGambar.text.toString().trim()
            if (judul.isNotEmpty() || dataIsi.isNotEmpty()) {
                val noteArsip = Note(judul, dataIsi, waktuPengingat, isPinned)
                DataManager.listArsip.add(noteArsip)
                DataManager.listCatatan.removeAt(position)
                DataManager.saveData(this)
                finish()
            }
        }
    }

    private fun bukaGaleri() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun simpanDanKembali() {
        val judul = etJudulGambar.text.toString().trim()
        val catatan = etCatatanGambar.text.toString().trim()
        val dataIsi = imageUri?.toString() ?: catatan

        if (judul.isNotEmpty() || dataIsi.isNotEmpty()) {
            if (isEdit && position != -1 && position < DataManager.listCatatan.size) {
                val noteUpdate = DataManager.listCatatan[position]
                noteUpdate.judul = judul
                noteUpdate.isi = dataIsi
                noteUpdate.isPinned = isPinned
                noteUpdate.waktuPengingat = waktuPengingat
                DataManager.saveData(this)
            } else if (!isEdit) {
                val resultIntent = Intent()
                resultIntent.putExtra("EXTRA_JUDUL", judul)
                resultIntent.putExtra("EXTRA_ISI", dataIsi)
                resultIntent.putExtra("EXTRA_WAKTU", waktuPengingat)
                resultIntent.putExtra("EXTRA_PIN", isPinned)
                setResult(Activity.RESULT_OK, resultIntent)
            }
        }
        finish()
    }
}