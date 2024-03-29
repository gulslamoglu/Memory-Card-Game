package com.example.memorygame

import android.app.Activity
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
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.utils.BitmapScaler
import com.example.memorygame.utils.EXTRA_BOARD_SIZE
import com.example.memorygame.utils.isPermissionGranted
import com.example.memorygame.utils.requestPermission
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream




class CreateActivity : AppCompatActivity() {

    companion object{
        private const val TAG ="CreateActivity"
        private const val PICK_PHOTO_CODE= 655
        private const val READ_EXTERNAL_PHOTOS_CODE= 248
        private const val READ_PHOTO_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MIN_GAME_NAME_LENGHT= 3
        private const val MAX_GAME_NAME_LENGHT= 14

    }
    private lateinit var rvimagePicker: RecyclerView
    private lateinit var etGameName: EditText
    private lateinit var btnSave: Button


    private  lateinit var adapter: ImagePickerAdapter
    private lateinit var boardSize: BoardSize
    private var numImagesRequired = -1
    private val chosenImageUris = mutableListOf<Uri>()
    private val storage= Firebase.storage
    private val db= Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        rvimagePicker=findViewById(R.id.rvimagePicker)
        etGameName=findViewById(R.id.etGameName)
        btnSave=findViewById(R.id.btnSave)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        boardSize= intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired= boardSize.getNumPairs()
        supportActionBar?.title = "Fotoğraf Seçin (0/ $numImagesRequired)"

        btnSave.setOnClickListener{
            saveDataToFirebase()
        }
        etGameName.filters= arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGHT))
        etGameName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnSave.isEnabled= shouldEnableSaveButton()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }


        })

        adapter =ImagePickerAdapter(this,chosenImageUris,boardSize,object: ImagePickerAdapter.ImageClickListener{
            override fun onPlaceHolderClicked() {
                if(isPermissionGranted(this@CreateActivity, READ_PHOTO_PERMISSION)){
                    launchIntentForPhotos()
                } else {
                    requestPermission(this@CreateActivity, READ_PHOTO_PERMISSION,READ_EXTERNAL_PHOTOS_CODE)
                }

            }

        })
        rvimagePicker.adapter= adapter
        rvimagePicker.setHasFixedSize(true)
        rvimagePicker.layoutManager= GridLayoutManager(this, boardSize.getWidth()/2)
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode== READ_EXTERNAL_PHOTOS_CODE){
            if(grantResults.isNotEmpty()&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos()
            }else{
                Toast.makeText(this,"Özel oyun oluşturabilmek için fotoğraf erişimini kabul edin!",Toast.LENGTH_LONG)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode != PICK_PHOTO_CODE || resultCode!= Activity.RESULT_OK || data==null){
            Log.w(TAG, "başlatılan etkinlikten veri geri alınmadı, kullanıcı muhtemelen iptal etti.")
            return
        }
        Log.i(TAG, "onActivityResult")
        val selectedUri= data.data
        val clipData=data.clipData
        if(clipData!=null){
            Log.i(TAG,"clipData numImages ${clipData.itemCount}:$clipData")
            for(i in 0 until clipData.itemCount){
                val clipItem=clipData.getItemAt(i)
                if(chosenImageUris.size < numImagesRequired){
                    chosenImageUris.add(clipItem.uri)
                }
            }
        }else if(selectedUri !=null){
            Log.i(TAG,"data: $selectedUri")
            chosenImageUris.add(selectedUri)
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.title="Fotoğraf Seçin (${chosenImageUris.size} / $numImagesRequired"
        btnSave.isEnabled=shouldEnableSaveButton()
    }

    private fun saveDataToFirebase() {
        val customGameName=etGameName.text.toString()
        Log.i(TAG,"FirebaseEKaydet")
        var didEncounterError =false
        val uploadedImageUrls= mutableListOf<String>()
        for((index,photoUri) in chosenImageUris.withIndex()){
            val imageByteArray = getImageByteArray(photoUri)
            val filePath= "images/$customGameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoReference=storage.reference.child(filePath)
            photoReference.putBytes(imageByteArray)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG,"Yüklenen baytlar: ${photoUploadTask.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener{downloadUrlTask ->
                    if(!downloadUrlTask.isSuccessful){
                        Log.i(TAG,"exception with firebase storage",downloadUrlTask.exception)
                        Toast.makeText(this,"Failed to upload image", Toast.LENGTH_SHORT).show()
                        didEncounterError=true
                        return@addOnCompleteListener
                    }
                    if(didEncounterError){
                        return@addOnCompleteListener
                    }
                    val downloadUrl= downloadUrlTask.result.toString()
                    uploadedImageUrls.add(downloadUrl)
                    Log.i(TAG, "Finished uploading $photoUri, num uploaded ${uploadedImageUrls.size}")
                    if(uploadedImageUrls.size== chosenImageUris.size){
                        handleAllImagesUploaded(customGameName, uploadedImageUrls)
                    }
                }
        }
    }

    private fun handleAllImagesUploaded(gameName: String, imageUrls: MutableList<String>) {

    }

    private fun getImageByteArray(photoUri: Uri): ByteArray {
        val originalBitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source= ImageDecoder.createSource(contentResolver,photoUri)
            ImageDecoder.decodeBitmap(source)
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
        }
        Log.i(TAG,"Orijinal en ${originalBitmap.width} ve yükseklik ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap,250)
        Log.i(TAG,"ölçeklenmiş en ${scaledBitmap.width} ve yükseklik ${scaledBitmap.height}")
        val byteOutputStream=ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60,byteOutputStream)
        return byteOutputStream.toByteArray()
    }

    private fun shouldEnableSaveButton(): Boolean {
        if(chosenImageUris.size != numImagesRequired){
            return false
        }
        if(etGameName.text.isBlank() || etGameName.text.length< MIN_GAME_NAME_LENGHT){
            return false
        }
        return true
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type= "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent,"Fotoğraf Seçin"), PICK_PHOTO_CODE)
    }
}