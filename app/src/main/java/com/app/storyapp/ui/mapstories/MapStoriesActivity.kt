package com.app.storyapp.ui.mapstories

import android.location.Geocoder
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.storyapp.R
import com.app.storyapp.customviews.StoryClusterManagerRenderer
import com.app.storyapp.customviews.StoryClusterMarker
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.remote.response.ListStoryItem
import com.app.storyapp.databinding.ActivityMapStoriesBinding
import com.app.storyapp.utils.dateCreated
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import java.util.Locale

class MapStoriesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapStoriesBinding
    private val viewModel by viewModels<MapStoriesViewModel>()

    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var storyClusterManagerRenderer: StoryClusterManagerRenderer
    private lateinit var storyClusterManager: ClusterManager<StoryClusterMarker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
        }
        setupClusterMarker()
        setupObserver()
    }

    private fun setupClusterMarker() {
        storyClusterManager = ClusterManager<StoryClusterMarker>(applicationContext, mMap)
        storyClusterManagerRenderer = StoryClusterManagerRenderer(this, mMap, storyClusterManager)
        storyClusterManager.renderer = storyClusterManagerRenderer
    }

    private fun setupObserver() {
        viewModel.mapStoryList.observe(this@MapStoriesActivity) {
            when (it) {
                is ResultState.Loading -> {
                    Toast.makeText(
                        this@MapStoriesActivity,
                        getString(R.string.please_wait),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is ResultState.Error -> {
                    Toast.makeText(this@MapStoriesActivity, it.error, Toast.LENGTH_SHORT).show()
                }

                is ResultState.Success -> {
                    addMarkerStories(it.data)
                }
            }
        }
    }

    private fun addMarkerStories(stories: List<ListStoryItem>) {
        try {
            stories.forEach { story ->
                val latLng = LatLng(story.lat as Double, story.lon as Double)
                val addressName = getAddressName(story.lat, story.lon)
                val createdOn = dateCreated(story.createdAt)
                val newMarker = StoryClusterMarker(
                    story.lat,
                    story.lon,
                    "${story.name} - $createdOn",
                    addressName,
                    story
                )
                storyClusterManager.addItem(newMarker)
                boundsBuilder.include(latLng)
            }
            storyClusterManager.cluster()

            val bounds: LatLngBounds = boundsBuilder.build()
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    100
                )
            )
        } catch (e: Exception) {
            Toast.makeText(this@MapStoriesActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun getAddressName(lat: Double, lon: Double): String? {
        var addressName: String? = null
        val geocoder = Geocoder(this@MapStoriesActivity, Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                addressName = list[0].getAddressLine(0)
            }
        } catch (e: Exception) {
            Toast.makeText(this@MapStoriesActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
        return addressName
    }
}