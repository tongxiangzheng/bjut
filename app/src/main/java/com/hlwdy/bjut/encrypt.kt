package com.hlwdy.bjut

import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import android.util.Base64
import javax.crypto.Cipher

object RSAUtils {
    private const val RSA = "RSA"
    private const val RSA_PADDING = "RSA/ECB/PKCS1Padding"
    private const val MAX_ENCRYPT_BLOCK = 117  // RSA_KEY_SIZE / 8 - 11 for PKCS1Padding
    private const val MAX_DECRYPT_BLOCK = 128  // RSA_KEY_SIZE / 8

    fun encrypt(data: String, publicKey: String): String {
        val keySpec = X509EncodedKeySpec(Base64.decode(publicKey, Base64.NO_WRAP))
        val keyFactory = KeyFactory.getInstance(RSA)
        val key: PublicKey = keyFactory.generatePublic(keySpec)
        val cipher = Cipher.getInstance(RSA_PADDING)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val dataBytes = data.toByteArray()
        val inputLen = dataBytes.size
        var offset = 0
        val output = mutableListOf<Byte>()

        while (inputLen - offset > 0) {
            val curBlock = if (inputLen - offset > MAX_ENCRYPT_BLOCK) MAX_ENCRYPT_BLOCK else inputLen - offset
            val encryptedBlock = cipher.doFinal(dataBytes, offset, curBlock)
            output.addAll(encryptedBlock.toList())
            offset += curBlock
        }

        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    fun decrypt(data: String, privateKey: String): String {
        val keySpec = PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.NO_WRAP))
        val keyFactory = KeyFactory.getInstance(RSA)
        val key: PrivateKey = keyFactory.generatePrivate(keySpec)
        val cipher = Cipher.getInstance(RSA_PADDING)
        cipher.init(Cipher.DECRYPT_MODE, key)

        val encryptedData = Base64.decode(data, Base64.NO_WRAP)
        val inputLen = encryptedData.size
        var offset = 0
        val output = mutableListOf<Byte>()

        while (inputLen - offset > 0) {
            val curBlock = if (inputLen - offset > MAX_DECRYPT_BLOCK) MAX_DECRYPT_BLOCK else inputLen - offset
            val decryptedBlock = cipher.doFinal(encryptedData, offset, curBlock)
            output.addAll(decryptedBlock.toList())
            offset += curBlock
        }

        return String(output.toByteArray())
    }
}

object BjutHttpRsa{
    private val PRIVATE_KEY = NativeEncrypthlwdyck().keypr()
    private val PUBLIC_KEY = NativeEncrypthlwdyck().keypu()
    fun requestDecrypt(paramString: String): String {
        return try {
            RSAUtils.decrypt(paramString, PRIVATE_KEY)
        } catch (exception: Exception) {
            exception.printStackTrace()
            ""
        }
    }

    fun requestEncrypt(paramString: String): String {
        return try {
            RSAUtils.encrypt(paramString, PUBLIC_KEY)
        } catch (exception: Exception) {
            exception.printStackTrace()
            ""
        }
    }
}