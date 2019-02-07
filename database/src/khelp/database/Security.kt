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

/**
 * Encrypt/Decrypt manager
 */
class Security(password: String)
{
    /**Key to use*/
    private val key: ByteArray
    /**Indicates if have to do encryption operations*/
    val encrypted: Boolean

    /**Compute key from password*/
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

    /**Indicates if given password is compatible with the security key*/
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

    /**
     * Encrypt or decrypt data
     * @param mode Encryption or decryption mode
     * @param inputStream Stream to encrypt/decrypt
     * @param outputStream Stream where write the result
     */
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

    /**
     * Encrypt a value
     * @param string Value to encrypt
     * @return Encryption result
     */
    internal fun encrypt(string: String): String
    {
        if (!this.encrypted)
        {
            return string
        }

        val inputStream = StringInputStream(string)
        val outputStream = ByteArrayOutputStream()
        this.desOperation(Cipher.ENCRYPT_MODE, inputStream, outputStream)
        return Base64.getEncoder().encodeToString(outputStream.toByteArray())
    }

    /**
     * Decrypt a value
     * @param string Value to encrypt
     * @return Decryption result
     */
    internal fun decrypt(string: String): String
    {
        if (!this.encrypted)
        {
            return string
        }

        val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(string))
        val outputStream = StringOutputStream()
        this.desOperation(Cipher.DECRYPT_MODE, inputStream, outputStream)
        return outputStream.string
    }
}