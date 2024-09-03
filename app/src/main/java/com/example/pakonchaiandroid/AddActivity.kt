package com.example.pakonchaiandroid

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class AddActivity : AppCompatActivity() {

    private lateinit var editBrandName: EditText
    private lateinit var editModelName: EditText
    private lateinit var editSerialNumber: EditText
    private lateinit var editQuantity: EditText
    private lateinit var editPrice: EditText
    private lateinit var editCPU_Speed_GHz: EditText
    private lateinit var editMemory_GB: EditText
    private lateinit var editHDD_Capacity_GB: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var imageView: ImageView

    private val client = OkHttpClient()
    private val gson = Gson()

    private var selectedImageUri: Uri? = null

    private val selectImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                updateImageView()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        enableEdgeToEdge()

        // Initialize views
        editBrandName = findViewById(R.id.editbrandName)
        editModelName = findViewById(R.id.editModelName)
        editSerialNumber = findViewById(R.id.editSerialNumber)
        editQuantity = findViewById(R.id.editQuantity)
        editPrice = findViewById(R.id.editPrice)
        editCPU_Speed_GHz = findViewById(R.id.editCPU_Speed_GHz)
        editMemory_GB = findViewById(R.id.editmomory)
        editHDD_Capacity_GB = findViewById(R.id.editSSD)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        imageView = findViewById(R.id.imageView)

        // Set button listeners
        selectImageButton.setOnClickListener {
            selectImage()
        }

        saveButton.setOnClickListener {
            saveComputer()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun updateImageView() {
        selectedImageUri?.let { uri ->
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveComputer() {
        val brandName = editBrandName.text.toString().trim()
        val modelName = editModelName.text.toString().trim()
        val serialNumber = editSerialNumber.text.toString().trim()
        val quantity = editQuantity.text.toString().toIntOrNull() ?: 0
        val price = editPrice.text.toString().toDoubleOrNull() ?: 0.0
        val cpuSpeed = editCPU_Speed_GHz.text.toString().toDoubleOrNull() ?: 0.0
        val memory = editMemory_GB.text.toString().toIntOrNull() ?: 0
        val hddCapacity = editHDD_Capacity_GB.text.toString().toIntOrNull() ?: 0

        val file = selectedImageUri?.let { uri ->
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "image.jpg")
            file.outputStream().use { inputStream?.copyTo(it) }
            file
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("BrandName", brandName)
            .addFormDataPart("ModelName", modelName)
            .addFormDataPart("SerialNumber", serialNumber)
            .addFormDataPart("Quantity", quantity.toString())
            .addFormDataPart("Price", price.toString())
            .addFormDataPart("CPU_Speed_GHz", cpuSpeed.toString())
            .addFormDataPart("Memory_GB", memory.toString())
            .addFormDataPart("HDD_Capacity_GB", hddCapacity.toString())
            .apply {
                file?.let {
                    addFormDataPart(
                        "Image",
                        it.name,
                        it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }
            }
            .build()

        val request = Request.Builder()
            .url("http://10.13.4.124:3000/addComputer")
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AddActivity,
                            "Computer added successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddActivity,
                            "Server Error: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@AddActivity,
                        "Error Adding Computer: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
