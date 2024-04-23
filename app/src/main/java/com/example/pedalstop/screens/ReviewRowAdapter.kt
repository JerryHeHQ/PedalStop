package com.example.pedalstop.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.ListAdapter
import com.example.pedalstop.data.MainViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pedalstop.R
import com.example.pedalstop.data.ReviewData
import com.example.pedalstop.databinding.RowReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewRowAdapter(private val context: Context, private val viewModel: MainViewModel)
    : ListAdapter<ReviewData, ReviewRowAdapter.VH>(ReviewDataDiff()) {
    class ReviewDataDiff : DiffUtil.ItemCallback<ReviewData>() {
        override fun areItemsTheSame(oldItem: ReviewData, newItem: ReviewData): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ReviewData, newItem: ReviewData): Boolean {
            return oldItem.reviewerName == newItem.reviewerName
                    && oldItem.reviewerUid == newItem.reviewerUid
                    && oldItem.rating == newItem.rating
                    && oldItem.description == newItem.description
                    && oldItem.timeStamp == newItem.timeStamp
        }
    }

    private fun getPos(holder: VH) : Int {
        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) {
            return holder.absoluteAdapterPosition
        }
        return pos
    }

    inner class VH(val rowPosBinding: RowReviewBinding)
        : RecyclerView.ViewHolder(rowPosBinding.root) {
        init {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowReviewBinding = RowReviewBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VH(rowReviewBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val adapterPosition = getPos(holder)
        val reviewData = getItem(adapterPosition)
        val binding = holder.rowPosBinding

        binding.rowReviewUsername.text = reviewData.reviewerName

        val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mma", Locale.getDefault())
        val date = reviewData.timeStamp
        binding.rowReviewTimestamp.text = date.let { timeFormatter.format(it) }

        val stars : List<ImageView> = listOf(
            binding.rowReviewStar1,
            binding.rowReviewStar2,
            binding.rowReviewStar3,
            binding.rowReviewStar4,
            binding.rowReviewStar5,
        )
        for (i in stars.indices) {
            if (reviewData.rating - i > 0) {
                if (reviewData.rating - i >= 1) {
                    stars[i].setImageResource(R.drawable.baseline_star_24_nopad)
                } else {
                    stars[i].setImageResource(R.drawable.baseline_star_half_24_nopad)
                }
            } else {
                stars[i].setImageResource(R.drawable.baseline_star_border_24_nopad)
            }
        }

        binding.rowReviewDescription.text = reviewData.description

    }
}