package com.nova.catatanku

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataManager {
    var listCatatan = mutableListOf<Note>()
    var listSampah = mutableListOf<Note>()
    var listArsip = mutableListOf<Note>() // Tambahkan list untuk Arsip

    fun saveData(context: Context) {
        val sharedPref = context.getSharedPreferences("CatatanPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val gson = Gson()

        // Simpan List Catatan
        val jsonCatatan = gson.toJson(listCatatan)
        editor.putString("key_catatan", jsonCatatan)

        // Simpan List Sampah
        val jsonSampah = gson.toJson(listSampah)
        editor.putString("key_sampah", jsonSampah)

        // Simpan List Arsip (Baru)
        val jsonArsip = gson.toJson(listArsip)
        editor.putString("key_arsip", jsonArsip)

        editor.apply()
    }

    fun loadData(context: Context) {
        val sharedPref = context.getSharedPreferences("CatatanPrefs", Context.MODE_PRIVATE)
        val gson = Gson()

        // Load List Catatan
        val jsonCatatan = sharedPref.getString("key_catatan", null)
        if (jsonCatatan != null) {
            try {
                val type = object : TypeToken<MutableList<Note>>() {}.type
                val loadedCatatan: MutableList<Note> = gson.fromJson(jsonCatatan, type)
                listCatatan.clear()
                listCatatan.addAll(loadedCatatan)
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Load List Sampah
        val jsonSampah = sharedPref.getString("key_sampah", null)
        if (jsonSampah != null) {
            try {
                val type = object : TypeToken<MutableList<Note>>() {}.type
                val loadedSampah: MutableList<Note> = gson.fromJson(jsonSampah, type)
                listSampah.clear()
                listSampah.addAll(loadedSampah)
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Load List Arsip (Baru)
        val jsonArsip = sharedPref.getString("key_arsip", null)
        if (jsonArsip != null) {
            try {
                val type = object : TypeToken<MutableList<Note>>() {}.type
                val loadedArsip: MutableList<Note> = gson.fromJson(jsonArsip, type)
                listArsip.clear()
                listArsip.addAll(loadedArsip)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun getDataPengingat(): MutableList<Note> {
        return listCatatan.filter { !it.waktuPengingat.isNullOrEmpty() }.toMutableList()
    }
}