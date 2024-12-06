package com.hlwdy.bjut.ui.network

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

import com.hlwdy.bjut.databinding.FragmentNetworkBinding

class NetworkFragment : Fragment() {

    private var _binding: FragmentNetworkBinding? = null

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

        _binding = FragmentNetworkBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}