package com.example.virologacifrado

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {
    val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    lateinit var cipher: Cipher
    private val key = "1234567890123456".toByteArray(Charsets.UTF_8)
    private val iv = "abcdefghijklmnop".toByteArray(Charsets.UTF_8)
    private val secretKeySpec = SecretKeySpec(key, "AES")
    private val ivParameterSpec = IvParameterSpec(iv)

    private var encode: Boolean = true

    lateinit var edittextBase64: EditText
    lateinit var btnBase64: Button
    lateinit var txtOutputBase64: TextView
    lateinit var btnCopyBase64: ImageView

    lateinit var edittextAes: EditText
    lateinit var btnAes: Button
    lateinit var txtOutputAes: TextView
    lateinit var btnCopyAes: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarBase64()
        cargarAES()
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            when (view.getId()) {
                R.id.radio_cifrado ->
                    if (checked) {
                        encode = true
                        Toast.makeText(applicationContext, "Todo listo para cifrar", Toast.LENGTH_SHORT).show()
                    }
                R.id.radio_descifrado ->
                    if (checked) {
                        encode = false
                        Toast.makeText(applicationContext, "Todo listo para descifrar", Toast.LENGTH_SHORT).show()
                    }
            }
            btnBase64.text = if (encode) "Cifrar" else "Descifrar"
            btnAes.text = if (encode) "Cifrar" else "Descifrar"
            edittextBase64.text.clear()
            txtOutputBase64.text = ""
            edittextAes.text.clear()
            txtOutputAes.text = ""
        }
    }
    fun cargarBase64(){

        edittextBase64 = findViewById(R.id.edittext_base64)
        btnBase64 = findViewById(R.id.btn_base64)
        txtOutputBase64 = findViewById(R.id.txt_output_base64)
        btnCopyBase64 = findViewById(R.id.btn_copy_base64)


        btnBase64.setOnClickListener {
            if (encode){
                // Cifrado en Base64
                val texto = edittextBase64.text.toString()

                val encodedBytes = encodeBase64(texto.toByteArray(Charsets.UTF_8))
                val encodedText = String(encodedBytes)
                txtOutputBase64.text = encodedText
                println("### Texto cifrado en Base64: $encodedText")
            }else{
                val texto = edittextBase64.text.toString()
                val out = decodeBase64(texto)
                txtOutputBase64.text = out
                println("### Texto decifrado en Base64: $out")
            }
        }

        btnCopyBase64.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("",txtOutputBase64.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(applicationContext, "Texto copiado", Toast.LENGTH_SHORT).show()
        }
    }

    fun encodeBase64(input: ByteArray): ByteArray {
        val output = mutableListOf<Byte>()

        var i = 0
        var j = 0
        while (i < input.size) {
            val bloq1 = input[i++].toInt() and 0xFF
            val bloq2 = if (i < input.size) input[i++].toInt() and 0xFF else 0
            val bloq3 = if (i < input.size) input[i++].toInt() and 0xFF else 0

            val trit1 = bloq1 shr 2
            val trit2 = (bloq1 and 0x03) shl 4 or (bloq2 shr 4)
            val trit3 = (bloq2 and 0x0F) shl 2 or (bloq3 shr 6)
            val trit4 = bloq3 and 0x3F

            output.add(base64Chars[trit1].toByte())
            output.add(base64Chars[trit2].toByte())
            output.add(if (j < output.size) base64Chars[trit3].toByte() else '='.toByte())
            output.add(if (j < output.size) base64Chars[trit4].toByte() else '='.toByte())
            j += 4
        }

        return output.toByteArray()
    }

    fun decodeBase64(input: String): String {
        val result = mutableListOf<Byte>()
        var i = 0
        while (i < input.length) {
            // Decodificar 4 caracteres Base64 en 3 bytes
            val c1 = base64Chars.indexOf(input[i++])
            val c2 = base64Chars.indexOf(input[i++])
            val c3 = base64Chars.indexOf(input[i++])
            val c4 = base64Chars.indexOf(input[i++])

            val b1 = (c1 shl 2) or (c2 shr 4)
            val b2 = ((c2 and 0x0F) shl 4) or (c3 shr 2)
            val b3 = ((c3 and 0x03) shl 6) or c4

            result.add(b1.toByte())
            if (c3 != 64) result.add(b2.toByte())
            if (c4 != 64) result.add(b3.toByte())
        }
        return result.toByteArray().toString(Charsets.UTF_8)
    }

    fun cargarAES(){

        edittextAes = findViewById(R.id.edittext_aes)
        btnAes = findViewById(R.id.btn_aes)
        txtOutputAes = findViewById(R.id.txt_output_aes)
        btnCopyAes = findViewById(R.id.btn_copy_aes)

        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        btnAes.setOnClickListener {
            if (encode){
                // Cifrado con AES

                val texto = edittextAes.text.toString()
                val cifrado = encodeAES(texto)
                txtOutputAes.text = cifrado
                println("### Texto cifrado en aes: $cifrado")
            }else{
                // Descifrar el texto
                val texto = edittextAes.text.toString()
                val out = decode(texto)
                txtOutputAes.text = out
                println("### Texto AES: $texto")
                println("### Texto AES descifrado: $out")
            }
        }

        btnCopyAes.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("",txtOutputAes.text.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(applicationContext, "Texto copiado", Toast.LENGTH_SHORT).show()
        }
    }

    fun encodeAES(texto: String): String{
        // Cifrar el texto
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encrypted = cipher.doFinal(texto.toByteArray(Charset.defaultCharset()))
        // Convertir el texto cifrado a una cadena hexadecimal para facilitar la manipulación
        return bytesToHex(encrypted)
    }

    fun decode(encryptedHex: String): String{
        // Descifrar el texto
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(hexToBytes(encryptedHex))
        return String(decryptedBytes, Charset.defaultCharset())
    }

    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.replace("\\s|0x".toRegex(), "")
        val byteArray = ByteArray(cleanHex.length / 2)
        for (i in 0 until cleanHex.length step 2) {
            val hexByte = cleanHex.substring(i, i + 2)
            byteArray[i / 2] = hexByte.toInt(16).toByte()
        }
        return byteArray
    }
}
