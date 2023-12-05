package com.example.tradesite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.example.tradesite.databinding.ActivitySellItemBinding
import java.util.*

class SellItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellItemBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sellItemsRef: DatabaseReference
    private var sellItemId: String? = null
    private var selectedImageUri: Uri? = null

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sellItemsRef = FirebaseDatabase.getInstance().getReference("sellItems")

        sellItemId = intent.getStringExtra("sellItemId")
        Log.v("SellItemActivity", "sellItemId=${sellItemId}")

        if (sellItemId != null) {
            loadSellItemForEditing(sellItemId!!)
        }

        binding.buttonSelectPhoto.setOnClickListener {
            openGallery()
        }

        binding.buttonRegister.setOnClickListener {
            registerOrEditSellItem()
        }
    }

    private fun loadSellItemForEditing(sellItemId: String) {
        // 판매 아이템을 수정하기 위해 기존 정보를 불러옴
        sellItemsRef.child(sellItemId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val sellItem = snapshot.getValue(SellItem::class.java)
                    if (sellItem != null) {
                        Log.v("SellItemActivity", sellItem.toString())
                        // 기존 정보를 UI에 채워넣음
                        binding.editTextTitle.setText(sellItem.title)
                        binding.editTextDescription.setText(sellItem.description)
                        binding.editTextPrice.setText(sellItem.price.toString())
                        binding.textSellerName.setText(sellItem.sellerName)



                        // 이미지 URL이 있다면 미리보기에 표시
                        if (!sellItem.photoUrl.isNullOrEmpty()) {
//                            selectedImageUri = Uri.parse(sellItem.photoUrl)
//                            binding.imagePreview.setImageURI(selectedImageUri)

                            // Glide를 사용하여 이미지 로드
                            Glide.with(this@SellItemActivity)
                                .load(sellItem.photoUrl)
                                .into(binding.imagePreview)

                        }
                    }
                } else {
                    Log.v("SellItemActivity", "snapshot doest not exists")

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
                Log.v("SellItemActivity", "Error")

            }
        })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data

            // 선택한 이미지를 미리보여주기
            binding.imagePreview.setImageURI(selectedImageUri)
        }
    }

    private fun registerOrEditSellItem() {
        val currentUser = auth.currentUser
        val sellerUid = currentUser?.uid ?: ""
        val sellerName = currentUser?.displayName ?: ""
        val title = binding.editTextTitle.text.toString()
        val description = binding.editTextDescription.text.toString()
        val price = binding.editTextPrice.text.toString().toDouble()

        if (selectedImageUri != null) {
            // 이미지를 Firebase Storage에 업로드하고 URL을 얻어오기
            uploadImageToFirebaseStorage(selectedImageUri!!) { downloadUrl ->

                // SellItem 객체 생성
                val sellItem = SellItem(
                    id = sellItemId,
                    sellerUid = sellerUid,
                    sellerName = sellerName,
                    title = title,
                    description = description,
                    price = price,
                    photoUrl = downloadUrl,
                    status = "판매 중",
                    isSold = false,
                    registrationDate = ServerValue.TIMESTAMP,
                    modificationDate = ServerValue.TIMESTAMP
                )

                // 등록 또는 수정
                if (sellItemId != null) {
                    sellItemsRef.child(sellItemId!!).setValue(sellItem.copy(modificationDate = ServerValue.TIMESTAMP))
                    Toast.makeText(this, "중고물품이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                } else {

                    val newItemRef = sellItemsRef.push()
                    sellItemId = newItemRef.key
                    // SellItem 객체 생성
                    val sellItem = SellItem(
                        id = sellItemId,
                        sellerUid = sellerUid,
                        sellerName = sellerName,
                        title = title,
                        description = description,
                        price = price,
                        photoUrl = downloadUrl,
                        status = "판매 중",
                        isSold = false,
                        registrationDate = ServerValue.TIMESTAMP,
                        modificationDate = ServerValue.TIMESTAMP
                    )

                    sellItemsRef.push().setValue(sellItem)
                    Toast.makeText(this, "중고물품이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                }

                finish()
            }
        } else {
            Toast.makeText(this, "이미지를 선택하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, onComplete: (String) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val filename = "${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child("images/$filename")

        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onComplete(downloadUrl.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}