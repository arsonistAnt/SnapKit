package com.example.snapkit.mediaviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.snapkit.databinding.FragmentPhotoViewBinding


class PhotoViewFragment(private val filePath: String) : Fragment() {
    private lateinit var photoViewBinding: FragmentPhotoViewBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        photoViewBinding = FragmentPhotoViewBinding.inflate(inflater, container, false)
        return photoViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setPhoto()
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Set the image for PhotoView using Glide and the private filePath property.
     */
    private fun setPhoto() {
        var photoView = photoViewBinding.photoView

        Glide.with(photoView.context)
            .load(filePath)
            .into(photoView)
    }
}