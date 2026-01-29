package com.nova.catatanku

data class Note(
    var judul: String,
    var isi: String,
    var waktuPengingat: String? = null,
    var isPinned: Boolean = false // Pastikan menggunakan 'var'
)