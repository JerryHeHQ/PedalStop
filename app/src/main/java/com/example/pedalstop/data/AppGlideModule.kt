package com.example.pedalstop.data

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import java.io.InputStream

@GlideModule
class AppGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}
object Glide {
    private var glideOptions = RequestOptions ()
        .fitCenter()

    fun fetch(storageReference: StorageReference, imageView: ImageView, width: Int, height: Int) {
        GlideApp.with(imageView.context)
            .asBitmap()
            .load(storageReference)
            .apply(glideOptions)
            .error(android.R.color.holo_red_dark)
            .override(width, height)
            .into(imageView)
    }
}