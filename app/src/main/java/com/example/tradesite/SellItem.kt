package com.example.tradesite
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue

data class SellItem(
    val id: String? = null,
    val sellerUid: String,
    val sellerName: String,
    val title: String,
    val description: String,
    val price: Double,
    val photoUrl: String,
    val status: String,
    val isSold: Boolean,
    val registrationDate: Any,
    val modificationDate: Any
)
{

    companion object {
        // SellItem을 Firebase Database에 매핑하기 위한 함수
        fun fromSnapshot(snapshot: DataSnapshot): SellItem {
            val sellItem = snapshot.getValue(SellItem::class.java)
            // Firebase에서는 TIMESTAMP를 Long으로 받아오므로 Any 타입에서 Long으로 변환
            val registrationDate = sellItem?.registrationDate as? Long
            val modificationDate = sellItem?.modificationDate as? Long

            return sellItem?.copy(
                id = snapshot.key,
                registrationDate = registrationDate ?: 0,
                modificationDate = modificationDate ?: 0
            ) ?: SellItem()
        }


    }

    constructor() : this(null, "","", "", "", 0.0, "", "", false, ServerValue.TIMESTAMP, ServerValue.TIMESTAMP)

    override fun toString(): String {
        return "SellItem(id=$id, sellerUid='$sellerUid', sellerName='$sellerName', title='$title', description='$description', price=$price, photoUrl='$photoUrl', status='$status', isSold=$isSold, registrationDate=$registrationDate, modificationDate=$modificationDate)"
    }

}