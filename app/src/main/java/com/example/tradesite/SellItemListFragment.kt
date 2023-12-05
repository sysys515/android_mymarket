package com.example.tradesite

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tradesite.databinding.FragmentSellItemListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SellItemListFragment : Fragment() {

    companion object {
        fun newInstance() : SellItemListFragment {
            var fragment = SellItemListFragment()
            var args = Bundle();
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var database: FirebaseDatabase
    private lateinit var sellItemsRef: DatabaseReference
    private lateinit var sellItemAdapter: SellItemAdapter
    private lateinit var binding: FragmentSellItemListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellItemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase 초기화
        database = FirebaseDatabase.getInstance()
        sellItemsRef = database.reference.child("sellItems")

        // RecyclerView 초기화 및 어댑터 설정
        sellItemAdapter = SellItemAdapter(requireContext(), ArrayList(), object : SellItemAdapter.OnItemClickListener {
            override fun onItemClick(sellItem: SellItem) {
                /*
                // 아이템 클릭 시 동작 정의
                // 예시: SellItemDetailFragment로 이동
                val sellItemDetailFragment = SellItemDetailFragment.newInstance(sellItem)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, sellItemDetailFragment)
                    .addToBackStack(null)
                    .commit()

                 */

                //val intent = Intent(requireContext(), SellItemActivity::class.java)
                val intent = Intent(requireContext(), SellItemDetailActivity::class.java)
                intent.putExtra("editMode", true)
                intent.putExtra("sellItemId", sellItem.id)
                startActivity(intent)
            }
        })

        binding.recyclerViewSellItemList.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSellItemList.adapter = sellItemAdapter


        binding.buttonSearch.setOnClickListener() {
            onSoldOutFilter = binding.doSoldOutFilter.isChecked
            if(binding.doMaxPriceFilter.text.toString().isNullOrEmpty())
                onMaxPriceFilter = -1
            else
                onMaxPriceFilter = Integer.parseInt(binding.doMaxPriceFilter.text.toString())
            fetchSellItemsFromFirebase()
        }

        // Firebase에서 데이터 가져오기
        fetchSellItemsFromFirebase()
    }

    var onSoldOutFilter : Boolean = false;
    var onMaxPriceFilter : Int = -1;

    private fun fetchSellItemsFromFirebase() {
        // ValueEventListener을 사용하여 데이터 변경 감지
        sellItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sellItemList = mutableListOf<SellItem>()

                for (data in snapshot.children) {
                    //val sellItem = data.getValue(SellItem::class.java)
                    val sellItem = SellItem.fromSnapshot(data)

                    Log.v("SellItemListFragment", sellItem.toString())

                    var doAdd : Boolean = false;
                    if(!onSoldOutFilter && onMaxPriceFilter < 0) {

                        doAdd = true;

                    } else if(!onSoldOutFilter && onMaxPriceFilter > 0) {
                        if(onMaxPriceFilter >= sellItem.price)
                            doAdd = true;
                    } else if(onSoldOutFilter && onMaxPriceFilter < 0) {
                        if(!sellItem.isSold) {
                            doAdd = true;
                        }
                    } else if(onSoldOutFilter && onMaxPriceFilter > 0) {
                        if(!sellItem.isSold && onMaxPriceFilter >= sellItem.price) {
                            doAdd = true;
                        }
                    }

                    if(doAdd) {
                        Log.v("SellItemListFragment", "key=[${onMaxPriceFilter}/${onSoldOutFilter}] ${sellItem.toString()}")
                        sellItem?.let {
                            sellItemList.add(it)
                        }
                    } else {

                        Log.v("SellItemListFragment", " PASS key=[${onMaxPriceFilter}/${onSoldOutFilter}] ${sellItem.toString()}")
//                        sellItem?.let {
//                            sellItemList.add(it)
//                        }
                    }
                }

                // 어댑터에 데이터 설정
                sellItemAdapter.submitList(sellItemList.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "데이터 가져오기 실패", Toast.LENGTH_SHORT).show()

                // 데이터 가져오기 실패 또는 취소 시 동작 정의
            }
        })
    }
}