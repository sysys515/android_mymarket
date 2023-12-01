package com.example.tradesite.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tradesite.DBKey.Companion.CHILD_CHAT
import com.example.tradesite.DBKey.Companion.DB_ARTICLES
import com.example.tradesite.DBKey.Companion.DB_USERS
import com.example.tradesite.R
import com.example.tradesite.chatlist.ChatListItem
import com.example.tradesite.databinding.FragmentHomeBinding
import com.example.tradesite.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private var binding: FragmentSignUpBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentSignUpBinding = FragmentSignUpBinding.bind(view)
        binding = fragmentSignUpBinding

        fragmentSignUpBinding.signUpButton.isEnabled=true

        fragmentSignUpBinding.signUpButton.setOnClickListener {
            val email = fragmentSignUpBinding.emailEditText.text.toString()
            val password = fragmentSignUpBinding.passwordEditText.text.toString()

            // 이메일과 비밀번호를 확인합니다.

            // Firebase 인증을 사용하여 새 사용자 생성
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 사용자 등록 성공
                        Snackbar.make(view, "회원가입이 완료되었습니다.", Snackbar.LENGTH_LONG).show()

                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    // Log in successful
                                    Toast.makeText(context, "자동 로그인 되었습니다.", Toast.LENGTH_SHORT).show()
                                    // You can perform additional actions upon successful login if needed
                                } else {
                                    // Log in failed after registration
                                    Toast.makeText(context, "자동 로그인 실패", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        // 등록이 실패하면 사용자에게 메시지를 표시합니다.
                        Snackbar.make(view, "회원가입 실패: ${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }

    companion object {
        fun newInstance(): SignUpFragment {
            return SignUpFragment()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
