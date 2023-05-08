package com.app.storyapp.customviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.setPadding
import com.app.storyapp.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

class StoryClusterManagerRenderer(
    private val context: Context, map: GoogleMap?,
    clusterManager: ClusterManager<StoryClusterMarker>?,
) : DefaultClusterRenderer<StoryClusterMarker>(
    context, map, clusterManager
) {
    private val iconGenerator: IconGenerator = IconGenerator(context.applicationContext)
    private val imageView: ImageView = ImageView(context.applicationContext)
    private val markerWidth: Int =
        context.resources.getDimension(R.dimen.story_marker_image).toInt()
    private val markerHeight: Int =
        context.resources.getDimension(R.dimen.story_marker_image).toInt()

    init {
        imageView.layoutParams = ViewGroup.LayoutParams(markerWidth, markerHeight)
        val padding = context.resources.getDimension(R.dimen.story_marker_padding).toInt()
        imageView.setPadding(padding)
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        iconGenerator.setContentView(imageView)
    }

    override fun onClusterItemRendered(clusterItem: StoryClusterMarker, marker: Marker) {
        Glide.with(context).load(clusterItem.getStory().photoUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(object: CustomTarget<Drawable>() {
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                imageView.setImageDrawable(resource)
                val icon = iconGenerator.makeIcon()
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon))
                marker.tag = clusterItem.getStory()
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

        })
    }

    override fun onBeforeClusterItemRendered(
        item: StoryClusterMarker,
        markerOptions: MarkerOptions
    ) {
        val icon = iconGenerator.makeIcon()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.title)
            .snippet(item.snippet)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<StoryClusterMarker>): Boolean {
        return false
    }
}