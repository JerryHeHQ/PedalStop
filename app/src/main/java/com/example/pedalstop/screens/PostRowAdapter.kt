package com.example.pedalstop.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.ListAdapter
import com.example.pedalstop.data.MainViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pedalstop.MainActivity
import com.example.pedalstop.R
import com.example.pedalstop.data.PostData
import com.example.pedalstop.databinding.RowPostBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class PostRowAdapter(private val context: Context, private val viewModel: MainViewModel)
    : ListAdapter<PostData, PostRowAdapter.VH>(PostDataDiff()) {
    class PostDataDiff : DiffUtil.ItemCallback<PostData>() {
        override fun areItemsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            return oldItem.firestoreID == newItem.firestoreID
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: PostData, newItem: PostData): Boolean {
            // This does not allow for location based updates
            // A new value for the calculated distance can be used instead, but that would require
            // more backend support and messy code, so this will do for now.
//            return oldItem.ownerName == newItem.ownerName
//                    && oldItem.imageUUID == newItem.imageUUID
//                    && oldItem.latitude == newItem.latitude
//                    && oldItem.longitude == newItem.longitude
//                    && oldItem.shape == newItem.shape
//                    && oldItem.mounting == newItem.mounting
//                    && oldItem.description == newItem.description
//                    && oldItem.favoritedBy == newItem.favoritedBy
//                    && oldItem.ratingSum == newItem.ratingSum
//                    && oldItem.reviewIDs == newItem.reviewIDs
//                    && oldItem.timeStamp == newItem.timeStamp
            return false
        }
    }

    private fun getPos(holder: VH) : Int {
        val pos = holder.bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) {
            return holder.absoluteAdapterPosition
        }
        return pos
    }

    inner class VH(val rowPosBinding: RowPostBinding)
        : RecyclerView.ViewHolder(rowPosBinding.root) {
        init {
            rowPosBinding.root.setOnClickListener {
                val adapterPosition = getPos(this)
                val postData = getItem(adapterPosition)
                viewModel.currentPost.value = postData
            }
            rowPosBinding.rowPostFavoriteButton.setOnClickListener {
                val adapterPosition = getPos(this)
                val postData = getItem(adapterPosition)
                viewModel.togglePostFavorite(postData) {} // No need for callback due to observer
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowPostBinding = RowPostBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VH(rowPostBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val adapterPosition = getPos(holder)
        val postData = getItem(adapterPosition)
        val binding = holder.rowPosBinding

        binding.rowPostUsername.text = postData.ownerName

        val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mma", Locale.getDefault())
        val date = postData.timeStamp?.toDate()
        binding.rowPostTimestamp.text = date?.let { timeFormatter.format(it) }

        if (viewModel.isFavorited(postData.firestoreID)) {
            binding.rowPostFavoriteButton.setImageResource(R.drawable.baseline_favorite_24_nopad)
        } else {
            binding.rowPostFavoriteButton.setImageResource(R.drawable.baseline_favorite_border_24_nopad)
        }

        binding.rowPostFavoriteCount.text = postData.favoritedBy.size.toString()

        val width: Int = context.resources.displayMetrics.widthPixels
        val height: Int = context.resources.displayMetrics.heightPixels
        viewModel.glideFetch(postData.imageUUID, binding.rowPostImage, width, height)

        val rating = if (postData.reviews.isEmpty()) { 0.0 } else {
            postData.ratingSum / postData.reviews.size
        }
        val stars : List<ImageView> = listOf(
            binding.rowPostStar1,
            binding.rowPostStar2,
            binding.rowPostStar3,
            binding.rowPostStar4,
            binding.rowPostStar5,
        )
        for (i in stars.indices) {
            if (rating - i > 0) {
                if (rating - i >= 1) {
                    stars[i].setImageResource(R.drawable.baseline_star_24_nopad)
                } else {
                    stars[i].setImageResource(R.drawable.baseline_star_half_24_nopad)
                }
            } else {
                stars[i].setImageResource(R.drawable.baseline_star_border_24_nopad)
            }
        }

        val ratingFormatter = DecimalFormat("#.##").apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        binding.rowPostRating.text = ratingFormatter.format(rating).toString()

        val numRatingsText = "(${postData.reviews.size})"
        binding.rowPostNumRatings.text = numRatingsText

        binding.rowPostShape.text = postData.shape
        binding.rowPostMounting.text = postData.mounting

        val milesPerMeter = 0.000621371192
        val distance = viewModel.getDistance(postData.latitude, postData.longitude) * milesPerMeter
        val distanceFormatter = DecimalFormat("##,###.##")
        val distanceText = "${distanceFormatter.format(distance)} mi"
        binding.rowPostDistance.text = distanceText
    }
}