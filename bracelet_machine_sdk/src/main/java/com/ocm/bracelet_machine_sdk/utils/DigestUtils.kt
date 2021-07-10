package com.ocm.bracelet_machine_sdk.utils

import android.util.Base64
import android.util.Log
import java.lang.Exception
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object DigestUtils {

    private const val TAG = "DigestUtils"
    private const val encryptKey = "www.ocmcom.com"
    private const val cipherStr = "AES/CBC/NoPadding"
    private const val encrypt = "AES"
    private const val ivParameter = "a091d2b30c7ee86b"
    private val encodingFormat = Charset.forName("UTF-8")

    /**
     * md5加密字符串
     * md5使用后转成16进制变成32个字节
     */
    fun md5(str: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(str.toByteArray())
        //没转16进制之前是16位
        println("result${result.size}")
        //转成16进制后是32字节
        return toHex(result)
    }

    fun toHex(byteArray: ByteArray): String {
        //转成16进制后是32字节
        return with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            toString()
        }
    }

    fun sha1(str:String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(str.toByteArray())
        return toHex(result)
    }

    fun sha256(str:String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(str.toByteArray())
        return toHex(result)
    }

    fun encodeAES(contentText: String): String? {
        return try {
            var targetStr = contentText
            while (targetStr.length%16 != 0) {
                targetStr += " "
            }
            val cipher = Cipher.getInstance(cipherStr)
            val tmpRaw = encryptKey.toByteArray(encodingFormat)
            val raw = tmpRaw.plus(byteArrayOf(0, 0))
            val skeySpec = SecretKeySpec(raw, encrypt)
            val iv = IvParameterSpec(ivParameter.toByteArray(encodingFormat))
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(targetStr.toByteArray(encodingFormat))
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun decodeAes(srcStr: String): String? {
        return try {
            val cipher = Cipher.getInstance(cipherStr)
            val tmpRaw = encryptKey.toByteArray(encodingFormat)
            val raw = tmpRaw.plus(byteArrayOf(0, 0))
            val skeySpec = SecretKeySpec(raw, encrypt)
            val iv = IvParameterSpec(ivParameter.toByteArray(encodingFormat))
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val decodeStr = Base64.decode(srcStr, Base64.DEFAULT)
            val original = cipher.doFinal(decodeStr)
            original.toString(encodingFormat)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

}