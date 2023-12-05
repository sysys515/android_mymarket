package com.example.tradesite

// PurchaseItem.kt

data class PurchaseItem(
    val sellItemId: String? = null,
    val sellerUid: String? = null,
    val buyerUid: String? = null,
    val title: String? = null,
    val price: Double? = null,
    val purchaseDate: Any? = null
)
