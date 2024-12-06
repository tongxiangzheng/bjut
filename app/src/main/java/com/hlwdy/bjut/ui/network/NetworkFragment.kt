package com.hlwdy.bjut.ui.network

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.net.DnsResolver
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

import com.hlwdy.bjut.databinding.FragmentNetworkBinding
import okhttp3.Dns
import java.net.InetAddress
import java.util.concurrent.Executors

fun isInternalIp(ip: String): Boolean {
    val privateIpPatterns = listOf(
        "^10\\..*".toRegex(),
        "^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*".toRegex(),
        "^192\\.168\\..*".toRegex()
    )
    return privateIpPatterns.any { it.matches(ip) }
}
@RequiresApi(Build.VERSION_CODES.Q)
fun checkDnsRecords(domain: String) {
    try {
        val executor = Executors.newSingleThreadExecutor()
        val resolver = DnsResolver.getInstance()
        resolver.query(
            null,
            domain,
            DnsResolver.TYPE_A,
            DnsResolver.FLAG_NO_CACHE_LOOKUP,
            executor,
            null,
            object : DnsResolver.Callback<List<InetAddress>> {
                override fun onAnswer(
                    addresses: List<InetAddress>,
                    rCode: Int,
                ) {
                    if (addresses.isEmpty()){
                        Log.w("Error","check bjut dns record with no result")
                    }
                    for(address in addresses){
                        val ipAddress = address.hostAddress
                        Log.d("normal","Host Address: $ipAddress")
                        val res=isInternalIp(ipAddress)
                        if (res) {
                            Log.d("normal","in bjut")
                        }else{
                            Log.d("normal","NOT in bjut")
                        }
                    }
                }

                override fun onError(e: DnsResolver.DnsException) {
                    //错误处理
                }
            },
        )

    } catch (e: Exception) {
        Log.w("Error","message: ${e.message}")
    }
}



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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        checkDnsRecords("bjut.edu.cn")
        _binding = FragmentNetworkBinding.inflate(inflater, container, false)
        val root: View = binding.root



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}