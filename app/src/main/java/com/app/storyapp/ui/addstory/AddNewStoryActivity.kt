package com.app.storyapp.ui.addstory

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AddNewStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddNewStoryBinding
    private lateinit var sharedPrefs: SharedPrefs
    private val viewModel by viewModels<AddNewStoryViewModel>()

    private var currentPhotoPath: String? = null
    private var imageFile: File? = null

    private lateinit var loadingDialog: AlertDialog
    private var isLoading = false

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

        binding.apply {
            btnCamera.setOnClickListener { startTakePhoto() }
            btnGallery.setOnClickListener { startGallery() }
            buttonAdd.setOnClickListener { handleAddStory() }
        }

        setupLoadingDialog()
        setupObserver()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
        val isValid: Boolean = isImageFileValid() && isDescriptionValid() && isTokenValid(token)
        isImageFileValid()
        if (isValid) {
            if (imageFile != null) {
                val file = reduceFileImage(imageFile as File)
                viewModel.addNewStory(file, description, token)
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