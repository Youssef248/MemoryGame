package com.example.memorygame

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.example.memorygame.models.BoardSize
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.MemoryBoardAdapter.Companion.TAG
import com.example.memorygame.utils.BitmapScaler

import com.example.memorygame.utils.EXTRA_BOARD_SIZE
import com.example.memorygame.utils.isPermissionGranted
import com.example.memorygame.utils.requestPermission
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    companion object {
        //private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        //private const val READ_EXTERNAL_PHOTOS_CODE = 24
        private const val MIN_GAME_NAME_LENGTH = 3
        private const val MAX_GAME_NAME_LENGTH = 14
    }

    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button

    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1
    private val chosenImageUris = mutableListOf<Uri>()

    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result from the launched activity
                val data: Intent? = result.data
                data?.let {

                    val uri = it.data
                    if (uri != null) {
                        chosenImageUris.add(uri)
                    }
                    rvImagePicker.adapter?.notifyDataSetChanged()
                    supportActionBar?.title = "Choose pics (${chosenImageUris.size} / $numImagesRequired)"
                    btnSave.isEnabled = shouldEnableSaveButton()
                }
            }
        }

    private fun shouldEnableSaveButton(): Boolean {
        if (chosenImageUris.size != numImagesRequired){
            return false
        }
        if(etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NAME_LENGTH){
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        rvImagePicker = findViewById(R.id.rvImagePicker)
        etGameName = findViewById(R.id.etGameName)
        btnSave = findViewById(R.id.btnSave)

        // Initialize Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set the back button click listener
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE, BoardSize::class.java) as BoardSize
        numImagesRequired = boardSize.getNumPairs()

        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"
        btnSave.setOnClickListener{
            saveDataToFirebase()
        }
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))

        etGameName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        rvImagePicker.adapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object: ImagePickerAdapter.ImageClickListener{
            override fun onPLaceholderClicked() {
                // Launch the photo picker directly
                launchIntentForPhotos()
            }
        })
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())


    }

        private fun saveDataToFirebase() {
            Log.i(TAG, "saveDataToFirebase")
            for ((index, photoUri) in chosenImageUris.withIndex()){
                val imageByteArray = getImageByteArray(photoUri)
            }
        }

        private fun getImageByteArray(photoUri: Uri): ByteArray {
            val originalBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            }else{
                MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
            }
            Log.i(TAG, "Original width: ${originalBitmap.width} and height ${originalBitmap.height}")
            val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
            Log.i(TAG, "Scaled width: ${scaledBitmap.width} and height ${scaledBitmap.height}")
            val byteOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
            return byteOutputStream.toByteArray()
        }



    /* override fun onRequestPermissionsResult(
         requestCode: Int,
         permissions: Array<out String>,
         grantResults: IntArray
     ) {
         Log.d("PermissionDebug", "requestCode: $requestCode")
         Log.d("PermissionDebug", "grantResults: ${grantResults.joinToString()}")
         if (requestCode == READ_EXTERNAL_PHOTOS_CODE){
             if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                 Log.d("PermissionDebug", "about to do launchIntentForPhotos")
                 launchIntentForPhotos()
             } else {
                 Toast.makeText(this, "In order to create a custom game, you need to provide access to your photos.", Toast.LENGTH_LONG).show()
             }
         }
         super.onRequestPermissionsResult(requestCode, permissions, grantResults)
     }

     */

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        val chooserIntent = Intent.createChooser(intent, "Choose pictures")
        someActivityResultLauncher.launch(chooserIntent)
    }



}