package com.samurainomichi.cloud_storage_client.temporary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.samurainomichi.cloud_storage_client.databinding.ItemTempFileBinding

class TemporaryStorageFilesAdapter(): RecyclerView.Adapter<TemporaryStorageFilesViewHolder>() {
    var list: List<String> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val _checkedCards = mutableSetOf<String>()
    val checkedCards: Set<String>
        get() = _checkedCards


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemporaryStorageFilesViewHolder {
        val binding = ItemTempFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
        )
        val holder = TemporaryStorageFilesViewHolder(binding)
        binding.cardItemTmp.setOnClickListener { card ->
            (card as MaterialCardView).isChecked = !card.isChecked

            if(card.isChecked)
                _checkedCards.add(holder.fileName)
            else
                _checkedCards.remove(holder.fileName)
        }

        return holder
    }

    override fun onBindViewHolder(holder: TemporaryStorageFilesViewHolder, position: Int) {
        holder.bind(list[position], checkedCards)
    }

    override fun getItemCount(): Int = list.size
}

class TemporaryStorageFilesViewHolder(private val binding: ItemTempFileBinding): RecyclerView.ViewHolder(binding.root) {
    var fileName: String = ""

    fun bind(fileName: String, checkedCards: Set<String>) {
        this.fileName = fileName
        binding.fileName = fileName
        binding.cardItemTmp.isChecked = checkedCards.contains(fileName)
    }
}