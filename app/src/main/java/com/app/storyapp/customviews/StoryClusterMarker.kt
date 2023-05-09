package com.app.storyapp.customviews

import com.app.storyapp.data.remote.response.ListStoryItem
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class StoryClusterMarker(
    lat: Double, lon: Double, private var title: String?, private var snippet: String?,
    private var story: ListStoryItem
) : ClusterItem {
    private var position: LatLng

    init {
        this.position = LatLng(lat, lon)
    }

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getSnippet(): String? {
        return snippet
    }

    fun getStory(): ListStoryItem {
        return story
    }

}