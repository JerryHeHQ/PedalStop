package com.example.pedalstop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.pedalstop.data.MainViewModel
import com.example.pedalstop.data.PostData
import com.example.pedalstop.databinding.FragmentOnePostBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale


class OnePostFragment : Fragment() {
    private var _binding: FragmentOnePostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setFavoriteButton(postData: PostData) {
        if (viewModel.isFavorited(postData.firestoreID)) {
            binding.onePostFavoriteButton.setImageResource(R.drawable.baseline_favorite_24_nopad)
        } else {
            binding.onePostFavoriteButton.setImageResource(R.drawable.baseline_favorite_border_24_nopad)
        }
    }

    private fun setRatings(postData: PostData) {
        val rating = if (postData.reviews.isEmpty()) { 0.0 } else {
            postData.ratingSum / postData.reviews.size
        }
        val stars : List<ImageView> = listOf(
            binding.onePostStar1,
            binding.onePostStar2,
            binding.onePostStar3,
            binding.onePostStar4,
            binding.onePostStar5,
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
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        binding.onePostRating.text = ratingFormatter.format(rating).toString()

        val numRatingsText = "(${postData.reviews.size})"
        binding.onePostNumRatings.text = numRatingsText
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.existOnePostSwipeRefreshLayout.setOnRefreshListener {
            binding.existOnePostSwipeRefreshLayout.isRefreshing = false
            viewModel.currentPost.value = null
        }

        // postData is always not null if this view is shown
        val postData = viewModel.currentPost.value ?: return

        binding.onePostUsername.text = postData.ownerName

        val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mma", Locale.getDefault())
        val date = postData.timeStamp?.toDate()
        binding.onePostTimestamp.text = date?.let { timeFormatter.format(it) }

        setFavoriteButton(postData)

        binding.onePostFavoriteButton.setOnClickListener {
            viewModel.togglePostFavorite(postData) {
                if (it) {
                    setFavoriteButton(postData)
                }
            }
        }

        binding.onePostFavoriteCount.text = postData.favoritedBy.size.toString()

        val width: Int = requireContext().resources.displayMetrics.widthPixels
        val height: Int = requireContext().resources.displayMetrics.heightPixels
        viewModel.glideFetch(postData.imageUUID, binding.onePostImage, width, height)


        val latitudeText = "${postData.latitude}° N"
        binding.onePostLatitude.text = latitudeText

        val longitudeText = "${postData.longitude}° E"
        binding.onePostLongitude.text = longitudeText

        binding.onePostShape.text = postData.shape
        binding.onePostMounting.text = postData.mounting
        binding.onePostDescription.text = postData.description

        setRatings(postData)

        binding.reviewTextInputLayout.setEndIconOnClickListener {
            val reviewDescription = binding.reviewTextInputEditText.text.toString()
            val reviewRating = binding.onePostReviewSeekBar.progress * 0.5
            viewModel.isLoading.value = true
            viewModel.addReview(reviewRating, reviewDescription) {
                viewModel.isLoading.value = false
                if (it) {
                    setRatings(postData)
                }
                val toastMessage = if (it) {
                    "Review successfully added"
                } else {
                    "Error adding review"
                }
                Toast.makeText(activity, toastMessage, Toast.LENGTH_LONG).show()
            }
        }

        binding.onePostReviewSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val reviewRatingFormatter = DecimalFormat("#.##").apply {
                    minimumFractionDigits = 1
                    maximumFractionDigits = 1
                }
                binding.onePostReviewRating.text = reviewRatingFormatter.format(
                    progress * 0.5).toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}