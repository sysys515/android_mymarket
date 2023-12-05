package com.example.tradesite

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.tradesite.databinding.ActivityMainBinding
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.analytics.FirebaseAnalytics
import android.util.Log
import android.view.MenuItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    //private lateinit var database: DatabaseReference

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val productsRef: DatabaseReference = database.getReference("products")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            startActivity(Intent(this, SellItemActivity::class.java))

//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Firebase 초기화
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        //database = FirebaseDatabase.getInstance().reference

        if(auth.currentUser == null) {
            //startActivity(Intent(this, SignupActivity::class.java))
        }

        //addProduct()
    }

    // Product.kt 파일에 데이터 클래스 정의
    data class Product(
        val title: String = "",
        val content: String = "",
        val imageUrl: String = "",
        val price: Double = 0.0,
        val seller: String = "",
        val registrationDate: String = "",
        val isSold: Boolean = false,
        val views: Int = 0
    )

    // 데이터 추가
    fun addProduct() {
        // 새로운 제품 데이터 생성
        val newProduct = Product(
            title = "제품 제목",
            content = "제품 내용",
            imageUrl = "이미지 URL",
            price = 29.99,
            seller = "판매자 이름",
            registrationDate = getCurrentDateTime(),
            isSold = false,
            views = 0
        )

        // Firebase Database에 데이터 추가
        val productId = productsRef.push().key
        if (productId != null) {
            productsRef.child(productId).setValue(newProduct)
                .addOnSuccessListener {
                    Log.d("Firebase", "데이터 추가 성공")
                    // 성공적으로 추가되었을 때의 처리를 추가할 수 있습니다.
                }
                .addOnFailureListener {
                    Log.e("Firebase", "데이터 추가 실패", it)
                    // 추가 실패 시 처리를 추가할 수 있습니다.
                }
        } else {
            // 키 생성 실패 시 처리를 추가할 수 있습니다.
            Log.e("Firebase", "키 생성 실패")
        }
    }

    // 현재 날짜 및 시간을 문자열로 반환하는 함수
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_settings -> {
                return true
            }
            R.id.action_logout -> {
                auth.signOut()
                return true
            }
            else -> super.onContextItemSelected(item)
        }

    }
}

