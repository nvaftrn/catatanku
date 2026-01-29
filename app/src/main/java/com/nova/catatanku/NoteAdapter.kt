package com.nova.catatanku

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onLongClick: (Int) -> Unit,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul: TextView = view.findViewById(R.id.tvItemJudul)
        val tvIsi: TextView = view.findViewById(R.id.tvItemIsi)
        val layoutWaktuItem: LinearLayout = view.findViewById(R.id.layoutWaktuItem)
        val tvWaktuItem: TextView = view.findViewById(R.id.tvWaktuItem)
        val ivPin: ImageView = view.findViewById(R.id.ivPin) // Ikon pin dari XML terbaru
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes.getOrNull(position) ?: return

        holder.tvJudul.text = note.judul

        // 1. Tampilkan Ikon Pin jika isPinned true
        if (note.isPinned) {
            holder.ivPin.visibility = View.VISIBLE
        } else {
            holder.ivPin.visibility = View.GONE
        }

        // 2. Logika Label Pengingat
        if (!note.waktuPengingat.isNullOrEmpty()) {
            holder.layoutWaktuItem.visibility = View.VISIBLE
            holder.tvWaktuItem.text = note.waktuPengingat
        } else {
            holder.layoutWaktuItem.visibility = View.GONE
        }

        // 3. Logika Preview Isi (Gambar/Daftar/Teks)
        try {
            val isiCatatan = note.isi
            when {
                isiCatatan.startsWith("content://") || isiCatatan.startsWith("file://") -> {
                    holder.tvIsi.text = "[Gambar]"
                }
                isiCatatan.startsWith("-") -> {
                    val previewDaftar = isiCatatan.replace("- ", "").replace("\n", ", ")
                    holder.tvIsi.text = if (previewDaftar.length > 50) previewDaftar.take(50) + "..." else previewDaftar
                }
                else -> {
                    holder.tvIsi.text = isiCatatan
                }
            }
        } catch (e: Exception) {
            holder.tvIsi.text = ""
        }

        // 4. PERBAIKAN LOGIKA KLIK (Navigasi Halaman)
        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                val context = holder.itemView.context
                val noteToOpen = notes[currentPos]

                // Cari index asli di DataManager agar tidak salah buka saat di-sort/filter
                val originalIndex = DataManager.listCatatan.indexOf(noteToOpen)

                // Pilih Activity yang sesuai
                val targetActivity = when {
                    noteToOpen.isi.startsWith("-") -> DaftarActivity::class.java
                    noteToOpen.isi.startsWith("content://") || noteToOpen.isi.startsWith("file://") -> GambarActivity::class.java
                    else -> TeksActivity::class.java
                }

                val intent = Intent(context, targetActivity).apply {
                    putExtra("IS_EDIT", true)
                    putExtra("POSITION", originalIndex)
                    putExtra("EXTRA_JUDUL", noteToOpen.judul)
                    putExtra("EXTRA_ISI", noteToOpen.isi)
                    putExtra("EXTRA_WAKTU", noteToOpen.waktuPengingat)
                    putExtra("EXTRA_PIN", noteToOpen.isPinned) // Tambahkan ini agar status pin tidak hilang saat dibuka
                }
                context.startActivity(intent)

                // Menjalankan callback jika diperlukan (opsional)
                onClick(currentPos)
            }
        }

        holder.itemView.setOnLongClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onLongClick(currentPos)
            }
            true
        }
    }

    override fun getItemCount() = notes.size

    // Fungsi tambahan untuk update data pencarian
    fun updateList(newList: List<Note>) {
        notes.clear()
        notes.addAll(newList)
        notifyDataSetChanged()
    }
}