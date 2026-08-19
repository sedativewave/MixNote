package com.mixnote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mixnote.data.Note

class NoteAdapter(
    private var notes: List<Note>, 
    private val onNoteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textTitle)
        val content: TextView = itemView.findViewById(R.id.textContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title.ifEmpty { "Без названия" }
        holder.content.text = note.content
        holder.itemView.setOnClickListener { onNoteClick(note) }
    }

    override fun getItemCount() = notes.size

    fun update(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}
