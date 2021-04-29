package com.samurainomichi.cloud_storage_client.cloudstorage

import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.samurainomichi.cloud_storage_client.R
import com.samurainomichi.cloud_storage_client.databinding.ItemStorageFileBinding
import com.samurainomichi.cloud_storage_client.model.Structure
import kotlin.math.log
import kotlin.math.pow

class CloudStorageAdapter : RecyclerView.Adapter<CloudStorageAdapter.CloudStorageViewHolder>() {
    private val _onFileClicked = MutableLiveData<String>(null)
    val onFileClicked: LiveData<String>
        get() = _onFileClicked

    val callback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if(checkedCards.isNotEmpty()) {
                clearCheckedCards(true)
                checkCallback()
                return
            }
            goUp()
        }
    }

    var root: List<Structure> = listOf()
        set(value) {
            structures = value
            field = value
        }

    private var structures: List<Structure> = listOf()
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    private val currentPath: MutableList<String> = mutableListOf()

    private val _checkedCards = mutableSetOf<String>()
    val checkedCards: Set<String>
        get() = _checkedCards

    private fun onClick(name: String) {
        val item = structures.find { it.name == name }!!
        if(item.children != null) {
            structures = item.children
            currentPath += item.name
            clearCheckedCards()
            checkCallback()
        }
        else _onFileClicked.value = name
    }

    private fun goUp(): Boolean {
        if(currentPath.isEmpty())
            return false

        currentPath.removeLast()
        checkCallback()

        var c = root
        for(name in currentPath) {
            c = c.find { it.name == name }?.children!!
        }
        structures = c
        clearCheckedCards()
        return true
    }

    private fun checkCallback() {
        callback.isEnabled = currentPath.isNotEmpty() || checkedCards.isNotEmpty()
    }

    private fun clearCheckedCards(notify: Boolean = false) {
        _checkedCards.clear()
        if(notify)
            notifyDataSetChanged()

    }

    private fun flipCard(card: MaterialCardView, holder: CloudStorageViewHolder) {
        card.isChecked = !card.isChecked

        if (card.isChecked) {
            _checkedCards.add(holder.fileName)
        }
        else {
            _checkedCards.remove(holder.fileName)
        }

        checkCallback()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloudStorageViewHolder {
        val binding = ItemStorageFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = CloudStorageViewHolder(binding)

        binding.cardItemStorage.setOnClickListener { card ->
            if(checkedCards.isNotEmpty()) {
                flipCard(card as MaterialCardView, holder)
            }

            else {
                onClick(holder.fileName)
            }
        }

        binding.cardItemStorage.setOnLongClickListener { card ->
            flipCard(card as MaterialCardView, holder)
            true
        }

        return holder
    }

    override fun onBindViewHolder(holder: CloudStorageViewHolder, position: Int) {
        holder.bind(
            structures[position],
            checkedCards
        )
    }

    override fun getItemCount(): Int = structures.size

    class CloudStorageViewHolder(private val binding: ItemStorageFileBinding): RecyclerView.ViewHolder(binding.root) {
        var fileName: String = ""
        fun bind(structure: Structure, checkedCards: Set<String>) {
            this.fileName = structure.name
            binding.name = structure.name
            binding.info = structure.children?.let { "${it.size} items" }
                ?: structure.capacity.let { size ->
                    when (log(size.toDouble(), 1024.0)) {
                        in 0.0..1.0 -> "$size b"
                        in 1.0..2.0 -> {
                            "%.2f".format(size / 1024.0) + " kb"
                        }
                        in 2.0..3.0 -> {
                            "%.2f".format(size / 1024.0.pow(2)) + " mb"
                        }
                        else -> "%.2f".format(size / 1024.0.pow(3)) + " gb"
                    }
                }

            binding.cardItemStorage.isChecked = checkedCards.contains(fileName)
            binding.imageViewIcon.setImageResource(
                if(structure.children != null)
                    R.drawable.ic_folder_24px
                else
                    R.drawable.ic_blank_file
            )
        }
    }
}