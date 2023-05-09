package com.app.storyapp.ui.addstory

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.app.storyapp.R
import com.app.storyapp.data.ResultState
import com.app.storyapp.data.local.SharedPrefs
import com.app.storyapp.data.remote.response.AddNewStoryResponse
import com.app.storyapp.databinding.ActivityAddNewStoryBinding
import com.app.storyapp.ui.message.SuccessActivity
import com.app.storyapp.utils.createCustomTempFile
import com.app.storyapp.utils.rotateImage
import com.app.storyapp.utils.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class AddNewStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewStoryBinding
    private lateinit var sharedPrefs: SharedPrefs
    private val viewModel by viewModels<AddNewStoryViewModel>()

    private var currentPhotoPath: String? = null
    private var imageFile: File? = null

    private lateinit var loadingDialog: AlertDialog
    private var isLoading = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath.toString())
            myFile.let { file ->
                rotateImage(file)
                imageFile = file
                binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddNewStoryActivity)
                rotateImage(myFile)
                imageFile = myFile
                currentPhotoPath = myFile.path
                binding.previewImageView.setImageURI(uri)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURRENT_PHOTO_PATH, currentPhotoPath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.add_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Permissions
        setupPermission()

        sharedPrefs = SharedPrefs(this)

        if (savedInstanceState != null) {
            currentPhotoPath = savedInstanceState.getString(CURRENT_PHOTO_PATH)
            if (currentPhotoPath != null) {
                imageFile = File(currentPhotoPath.toString())
                binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.apply {
            btnCamera.setOnClickListener { startTakePhoto() }
            btnGallery.setOnClickListener { startGallery() }
            buttonAdd.setOnClickListener { handleAddStory() }
        }

        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            handleSwitchLocation(isChecked)
        }

        setupLoadingDialog()
        setupObserver()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Not have permission",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                }
                else -> {
                }
            }
        }

    private fun checkPermission(permissions: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permissions
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation(callback: (Location?) -> Unit) {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    callback(location)
                } else {
                    callback(null)
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun handleSwitchLocation(checked: Boolean) {
        if (checked) {
            getMyLocation { location ->
                if (location != null) {
                    val myLocation = LatLng(location.latitude, location.longitude)
                    binding.tvAddress.tag = myLocation
                    val address = getAddressName(myLocation.latitude, myLocation.longitude)
                    binding.tvAddress.text = address
                } else {
                    showMessage("Location is not found try again")
                    binding.switchLocation.isChecked = false
                }
            }
        } else {
            binding.tvAddress.tag = null
            binding.tvAddress.text = null
        }
    }

    private fun getAddressName(lat: Double, lon: Double): String? {
        var addressName: String? = null
        val geocoder = Geocoder(this@AddNewStoryActivity, Locale.getDefault())
        try {
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                addressName = list[0].getAddressLine(0)
            }
        } catch (e: Exception) {
            Toast.makeText(this@AddNewStoryActivity, e.message.toString(), Toast.LENGTH_SHORT)
                .show()
        }
        return addressName
    }

    private fun setupObserver() {
        viewModel.addStoryRes.observe(this) { handleAddStoryRes(it) }
    }

    private fun setupPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddNewStoryActivity,
                "com.app.storyapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_a_photo))
        launcherIntentGallery.launch(chooser)
    }

    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > FILE_MAXIMAL_SIZE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun handleAddStory() {
        val description = binding.edAddDescription.text.toString()
        val token = sharedPrefs.getUser().token.toString()
        val myLocation = binding.tvAddress.tag as LatLng?
        val isValid: Boolean = isImageFileValid() && isDescriptionValid() && isTokenValid(token)
        isImageFileValid()
        if (isValid) {
            showLoading(true)
            if (imageFile != null) {
                val file = reduceFileImage(imageFile as File)
                viewModel.addNewStory(token, file, description, myLocation?.latitude, myLocation?.longitude)
            }
        }
    }

    private fun handleAddStoryRes(result: ResultState<AddNewStoryResponse>) {
        when (result) {
            is ResultState.Loading -> {
                showLoading(true)
            }

            is ResultState.Success -> {
                showLoading(false)
                handleSuccess(result.data)
            }

            is ResultState.Error -> {
                showLoading(false)
                showMessage(result.error)
            }
        }
    }

    private fun handleSuccess(data: AddNewStoryResponse) {
        intentToSuccess(data.message)
    }

    private fun intentToSuccess(message: String? = getString(R.string.success)) {
        val intent = Intent(this, SuccessActivity::class.java)
        intent.putExtra(SuccessActivity.EXTRA_MESSAGE_SUCCESS, message)
        startActivity(intent)
        finishAffinity()
    }

    private fun setupLoadingDialog() {
        val adBuilder = AlertDialog.Builder(this)
        adBuilder.setView(R.layout.loading)
        adBuilder.setCancelable(false)
        loadingDialog = adBuilder.create()
    }

    private fun showLoading(value: Boolean) {
        isLoading = if (value) {
            loadingDialog.show()
            true
        } else {
            loadingDialog.dismiss()
            false
        }
    }

    private fun isImageFileValid(): Boolean {
        if (imageFile != null) {
            return true
        }
        showMessage("Photo cannot be empty")
        return false
    }

    private fun isDescriptionValid(): Boolean {
        if (binding.edAddDescription.text.toString().isNotEmpty()) {
            binding.inputLayout.error = null
            return true
        }
        binding.inputLayout.error = getString(R.string.field_cannot_be_empty)
        return false
    }

    private fun isTokenValid(token: String): Boolean {
        if (token.isNotEmpty()) {
            return true
        }
        showMessage(getString(R.string.unauthorized))
        return false
    }

    private fun showMessage(message: String) {
        Toast.makeText(this@AddNewStoryActivity, message, Toast.LENGTH_SHORT).show()
    }


    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10

        private const val CURRENT_PHOTO_PATH = "current_photo_path"
        private const val FILE_MAXIMAL_SIZE = 1000000
    }
}