package com.example.tradesite

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.tradesite.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle the image selection
            val data: Intent? = result.data
            selectedImageUri = data?.data
            binding.imageViewProfile.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        binding.buttonSignup.setOnClickListener {
            signUp()
        }

        binding.buttonPickImage.setOnClickListener {
            pickImage()
        }
    }

    private fun signUp() {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        val nickname = binding.editTextNickname.text.toString()

        Log.v("Signup", "createUserWithEmailAndPassword... ($email) ($password) ($nickname)")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.v("Signup", "sign up Success... ${user?.uid}")

                    // Upload profile image to Firebase Storage
                    uploadProfileImage(user?.uid, nickname)
                } else {
                    // Handle sign up failure
                    Log.v("Signup", "sign up failure ")

                    // 사용자 생성이 실패한 경우
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        // 이미 등록된 이메일 주소인 경우
                        Toast.makeText(this, "이미 사용 중인 이메일 주소입니다.", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthWeakPasswordException) {
                        // 약한 암호를 사용한 경우
                        Toast.makeText(this, "암호는 최소 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 그 외의 실패 원인
                        Toast.makeText(this, "사용자 생성 실패: ${exception?.message}", Toast.LENGTH_SHORT).show()
                    }

                }
            }
    }

    private fun uploadProfileImage(userId: String?, nickname: String) {
        if (selectedImageUri != null && userId != null) {
            val imageRef = storageReference.child("profile_images/$userId.jpg")

            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully, get download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveUserInfo(userId, nickname, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    // Image upload failed
                    Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No image selected, save user info without profile image
            if(userId != null)
                saveUserInfo(userId, nickname, "")
        }
    }

    private fun saveUserInfo(userId: String, nickname: String, profileImageUrl: String) {

        Log.v("Signup", "saveUserInfo... ($userId) ($profileImageUrl) ($nickname)")

        // 사용자 데이터를 Realtime Database에 저장
        val userRef = db.getReference("users").child(userId)
        userRef.setValue(
            mapOf(
                "username" to nickname,
                "profileImageUrl" to profileImageUrl
            )
        )
        //userRef.child("username").setValue(nickname)
        //userRef.child("profileImageUrl").setValue(profileImageUrl)
    }

    private fun pickImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        getContent.launch(galleryIntent)
    }
}
