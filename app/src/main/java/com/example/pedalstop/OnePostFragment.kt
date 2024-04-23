package com.example.pedalstop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.pedalstop.data.MainViewModel
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

        if (viewModel.isFavorited(postData.firestoreID)) {
            binding.onePostFavoriteButton.setImageResource(R.drawable.baseline_favorite_24_nopad)
        } else {
            binding.onePostFavoriteButton.setImageResource(R.drawable.baseline_favorite_border_24_nopad)
        }

        binding.onePostFavoriteButton.setOnClickListener {
            viewModel.togglePostFavorite(postData) {
                if (it) {
                    // Repeated code, but adding into function causes unnecessary clutter
                    if (viewModel.isFavorited(postData.firestoreID)) {
                        binding.onePostFavoriteButton.setImageResource(R.drawable.baseline_favorite_24_nopad)
                    } else {
                        binding.onePostFavoriteButton.setImageResource(R.drawable.baseline_favorite_border_24_nopad)
                    }
                    binding.onePostFavoriteButton.invalidate()
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

        val rating = if (postData.reviewIDs.isEmpty()) { 0.0 } else {
            postData.ratingSum / postData.reviewIDs.size
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

        val ratingFormatter = DecimalFormat("#.##")
        binding.onePostRating.text = ratingFormatter.format(rating).toString()

        val numRatingsText = "(${postData.reviewIDs.size})"
        binding.onePostNumRatings.text = numRatingsText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}