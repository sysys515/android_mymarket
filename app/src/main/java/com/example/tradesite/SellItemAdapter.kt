package com.example.tradesite

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tradesite.databinding.ItemSellItemBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SellItemAdapter(
    private val context: Context,
    private var sellItemList: List<SellItem>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SellItemAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(sellItem: SellItem)
    }

    inner class ViewHolder(private val binding: ItemSellItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sellItem: SellItem) {

            binding.textViewTitle.text = sellItem.title
            binding.textViewPrice.text = "${sellItem.price} 원"
            binding.textViewSellerId.text = sellItem.sellerUid

            if(sellItem.registrationDate is Long) {
                val date = Date(sellItem.registrationDate)
                val dFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                binding.textViewRegistrationDate.text = dFormat.format(date)
            }

            Log.v("SellItemAdapter", sellItem.toString())

            // Glide를 사용하여 이미지 로드
            Glide.with(context)
                .load(sellItem.photoUrl)
                .into(binding.imageViewSellItem)

            binding.root.setOnClickListener {
                onItemClickListener.onItemClick(sellItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSellItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sellItem = sellItemList[position]
        holder.bind(sellItem)
    }

    override fun getItemCount(): Int {
        return sellItemList.size
    }

    fun submitList(_sellItemList: List<SellItem>) {
        Log.v("SellItemAdapter", _sellItemList.toString())
        for(item in _sellItemList) {
            Log.v("SellItemAdapter submitList()", item.toString())
        }
        this.sellItemList = _sellItemList
        notifyDataSetChanged()
    }
}