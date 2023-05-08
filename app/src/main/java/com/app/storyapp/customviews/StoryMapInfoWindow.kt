package com.app.storyapp.customviews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.app.storyapp.R
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.utils.dateCreated
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class StoryMapInfoWindow(context: Context) : GoogleMap.InfoWindowAdapter {

    private val view: View? = LayoutInflater.from(context).inflate(R.layout.story_info_window, null)

    private fun drawInfo(marker: Marker, view: View?) {
        val title = view?.findViewById<TextView>(R.id.tv_iw_story_title)
        val created = view?.findViewById<TextView>(R.id.tv_iw_story_created)
        val address = view?.findViewById<TextView>(R.id.tv_iw_address)

        title?.text = marker.title
        address?.text = marker.snippet

        val storyItem = marker.tag as ListStoryItem
        val createdOn = dateCreated(storyItem.createdAt)
        created?.text = createdOn
    }

    override fun getInfoContents(marker: Marker): View? {
        drawInfo(marker, view)
        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        drawInfo(marker, view)
        return view
    }
}