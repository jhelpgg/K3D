package khelp.database

import khelp.io.StringInputStream
import khelp.io.StringOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

class Security(password: String)
{
    private val key: ByteArray
    val encrypted: Boolean

    private fun computeKey(string: String): ByteArray
    {
        var hash: Long = 0
        string.toCharArray().forEach { hash = 31L * hash + it.toLong() }
        val key = ByteArray(8)

        (0..7).forEach { index ->
            key[index] = (hash and 0xFF).toByte()
            hash = hash shr 8
        }

        return key
    }

    init
    {
        loadSecurity()
        if (password.isEmpty())
        {
            this.key = ByteArray(0)
            this.encrypted = false
        }
        else
        {
            this.key = this.computeKey(password)
            this.encrypted = true
        }
    }

    internal fun same(password: String) =
            if (this.encrypted)
            {
                if (password.isEmpty())
                {
                    false
                }
                else
                {
                    val key = this.computeKey(password)
                    (0..7).all { this.key[it] == key[it] }
                }
            }
            else
            {
                password.isEmpty()
            }

    private fun desOperation(mode: Int, inputStream: InputStream, outputStream: OutputStream)
    {
        val desKeySpec = DESKeySpec(this.key)
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val secretKey = keyFactory.generateSecret(desKeySpec)
        val cipher = Cipher.getInstance("DES")
        cipher.init(mode, secretKey)
        val cipherInputStream = CipherInputStream(inputStream, cipher)
        val buffer = ByteArray(8)
        var read = cipherInputStream.read(buffer)

        while (read >= 0)
        {
            outputStream.write(buffer, 0, read)
            read = cipherInputStream.read(buffer)
        }

        outputStream.flush()
        cipherInputStream.close()
    }

    internal fun encrypt(string: String): String
    {
        if (this.key.size == 0)
        {
            return string
        }

        val inputStream = StringInputStream(string)
        val outputStream = ByteArrayOutputStream()
        this.desOperation(Cipher.ENCRYPT_MODE, inputStream, outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    internal fun decrypt(string: String): String
    {
        if (this.key.size == 0)
        {
            return string
        }

        val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(string))
        val outputStream = StringOutputStream()
        this.desOperation(Cipher.DECRYPT_MODE, inputStream, outputStream)
        return outputStream.string
    }
}