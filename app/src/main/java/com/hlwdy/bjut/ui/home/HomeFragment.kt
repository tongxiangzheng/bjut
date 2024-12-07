package com.hlwdy.bjut.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hlwdy.bjut.R
import com.hlwdy.bjut.RouterActivity
import com.hlwdy.bjut.account_session_util
import com.hlwdy.bjut.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun showToast(message: String) {
        activity?.let { fragmentActivity ->
            Handler(Looper.getMainLooper()).post {
                if (isAdded) {
                    Toast.makeText(fragmentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnWebVpn.setOnClickListener {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.removeSessionCookies(null)
            cookieManager.setCookie(".webvpn.bjut.edu.cn", "wengine_vpn_ticketwebvpn_bjut_edu_cn="+
                    account_session_util(requireContext()).getUserDetails()[account_session_util.KEY_WEBVPNTK].toString()+"; path=/")
            cookieManager.flush()
            val intent = Intent(requireContext(), WebVpnViewActivity::class.java)
            startActivity(intent)
        }

        binding.btnCardCode.setOnClickListener {
            //val bundle = Bundle().apply { putBoolean("jump_code", true) }
            //findNavController().navigate(R.id.nav_card,bundle)
            val intent = Intent(requireContext(), RouterActivity::class.java).apply {
                action = "openCardCode"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}