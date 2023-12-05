package com.example.tradesite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tradesite.databinding.ActivitySellItemDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SellItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellItemDetailBinding
    private lateinit var sellItemId: String
    private lateinit var sellItemRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sellItemId = intent.getStringExtra("sellItemId") ?: ""
        sellItemRef = FirebaseDatabase.getInstance().getReference("sellItems").child(sellItemId)

        binding.buttonBuy.setOnClickListener {
            purchaseItem()
        }

        binding.buttonEdit.setOnClickListener {
            val intent = Intent(this, SellItemActivity::class.java)
            intent.putExtra("editMode", true)
            intent.putExtra("sellItemId", sellItemId)
            startActivity(intent)
        }

        sellItemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val sellItem = SellItem.fromSnapshot(snapshot)

                    binding.textViewTitle.text = sellItem.title
                    binding.textViewDescription.text = sellItem.description
                    binding.textViewPrice.text = "가격: ${sellItem.price} 원"
                    binding.textViewSeller.text = "판매자: ${sellItem.sellerName}"
                    binding.textViewRegistrationDate.text = "등록일자: ${sellItem.registrationDate}"

                    Glide.with(this@SellItemDetailActivity)
                        .load(sellItem.photoUrl)
                        .into(binding.imageViewSellItem)

                    binding.buttonEdit.visibility = View.GONE

                    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserUid == sellItem.sellerUid) {
                        binding.buttonEdit.visibility = View.VISIBLE
                        binding.buttonBuy.visibility = View.GONE

                        showToast("자신이 등록한 물품입니다.")

                    } else if (sellItem.isSold) {
                        binding.buttonBuy.visibility = View.GONE
                        showToast("이미 판매된 물품입니다.")
                    } else {
                        binding.buttonBuy.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
            }
        })
    }

    private fun purchaseItem() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        sellItemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val sellItem = SellItem.fromSnapshot(snapshot)

                    if (currentUserUid == sellItem.sellerUid) {
                        showToast("자신이 등록한 물품은 구매할 수 없습니다.")
                        return
                    }

                    if (sellItem.isSold) {
                        showToast("이미 판매된 물품입니다.")
                        return
                    }

                    sellItemRef.child("isSold").setValue(true)
                    addToPurchaseHistory(sellItem)
                    Log.v("SellItemDetailActivity", sellItem.toString())

                    showToast("물품을 구매하였습니다.")
                    finish()  // 구매가 완료되면 현재 화면 종료
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
            }
        })
    }

    private fun addToPurchaseHistory(sellItem: SellItem) {
        val purchaseHistoryRef = FirebaseDatabase.getInstance().getReference("purchaseHistory")
        val purchaseItem = PurchaseItem(
            sellItemId = sellItem.id,
            sellerUid = sellItem.sellerUid,
            buyerUid = FirebaseAuth.getInstance().currentUser?.uid,
            title = sellItem.title,
            price = sellItem.price,
            purchaseDate = ServerValue.TIMESTAMP
        )

        val purchaseItemRef = purchaseHistoryRef.push()
        purchaseItemRef.setValue(purchaseItem)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}