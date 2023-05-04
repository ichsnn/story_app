package com.app.storyapp.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import androidx.exifinterface.media.ExifInterface
import com.app.storyapp.data.remote.response.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createErrorResponse(e: HttpException): ErrorResponse {
    val errorJSONString = e.response()?.errorBody()?.string()
    return Gson().fromJson(errorJSONString, ErrorResponse::class.java)
}

fun createCustomTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun dateCreated(dateString: String?): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val date = sdf.parse(dateString.toString()) ?: return ""
    val now = Calendar.getInstance()
    val dateTime = Calendar.getInstance().apply {
        timeInMillis = date.time
    }
    val timeDiff = now.timeInMillis - dateTime.timeInMillis

    return when {
        timeDiff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        timeDiff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(timeDiff)} minutes ago"
        timeDiff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(timeDiff)} hours ago"
        timeDiff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(timeDiff)} days ago"
        else -> {
            val years = timeDiff / (1000L * 60 * 60 * 24 * 365)
            if (years > 0) "$years year${if (years > 1) "s" else ""} ago" else ""
        }
    }
}

fun rotateImage(file: File) {
    val exifInterface = ExifInterface(file.path)
    val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
    val matrix = Matrix()
    val bitmap = BitmapFactory.decodeFile(file.path)
    when(orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270F)
    }
    val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createCustomTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()
    return myFile
}
