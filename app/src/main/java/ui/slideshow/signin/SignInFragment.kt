package com.example.tradesite.ui.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tradesite.SellItemListFragment
import com.example.tradesite.databinding.FragmentSignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signUp(email, password)
            } else {
                Toast.makeText(requireContext(), "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    Toast.makeText(requireContext(), "로그인 성공", Toast.LENGTH_SHORT).show()


                    gotoHome()
                    // TODO: 로그인 후의 화면으로 전환
                } else {
                    // 로그인 실패
                    Toast.makeText(requireContext(), "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // 가입 성공
                    Toast.makeText(requireContext(), "가입 성공!! 로그인 하세요", Toast.LENGTH_SHORT).show()
                    // TODO: 가입 후의 화면으로 전환
                } else {
                    // 가입 실패
                    Toast.makeText(requireContext(), "가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private  fun gotoHome() {
//        val sellItemListFragment = SellItemListFragment.newInstance()
//        requireActivity().supportFragmentManager.beginTransaction()
//            .replace(binding.containerFragment.id, sellItemListFragment)
//            .addToBackStack(null)
//            .commit()
    }

}