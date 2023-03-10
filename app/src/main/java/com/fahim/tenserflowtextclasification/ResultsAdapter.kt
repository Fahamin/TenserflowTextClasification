package com.fahim.tenserflowtextclasification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fahim.tenserflowtextclasification.databinding.ItemClassificationBinding
import org.tensorflow.lite.support.label.Category

class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    var resultsList: List<Category> = emptyList()

    class ViewHolder(private val binding: ItemClassificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(label: String, score: Float) {
            with(binding) {
                result.text = binding.root.context.getString(
                    R.string.result_display_text,
                    label,
                    score)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemClassificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = resultsList[position]
        holder.bind(category.label, category.score)
    }

    override fun getItemCount() = resultsList.size
}