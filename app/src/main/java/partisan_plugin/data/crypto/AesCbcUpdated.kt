package partisan_plugin.data.crypto

import android.util.Base64
import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version
import partisan_plugin.data.Constants
import partisan_plugin.domain.entities.CipherTextIvMac
import partisan_plugin.domain.entities.SecretKeys
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/*
 * Copyright (c) 2014-2015 Tozny LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * Created by Isaac Potoczny-Jones on 11/12/14.
 */


object AesCbcUpdated {
    private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"
    private const val CIPHER = "AES"
    private const val AES_KEY_LENGTH_BITS = 256
    private const val IV_LENGTH_BYTES = 16
    private const val PBE_SALT_LENGTH_BITS = AES_KEY_LENGTH_BITS // same size as key output
    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val HMAC_KEY_LENGTH_BITS = 256
    const val BASE64_FLAGS = Base64.NO_WRAP
    private const val ITERATIONS = 10
    private const val PARALLELISM = 1


    /**
     * A function that generates random AES and HMAC keys and prints out exceptions but
     * doesn't throw them since none should be encountered. If they are
     * encountered, the return value is null.
     *
     * @return The AES and HMAC keys.
     * @throws GeneralSecurityException if AES is not implemented on this system,
     * or a suitable RNG is not available
     */
    @Throws(GeneralSecurityException::class)
    fun generateKey(): SecretKeys {
        val keyGen = KeyGenerator.getInstance(CIPHER)
        // No need to provide a SecureRandom or set a seed since that will
        // happen automatically.
        keyGen.init(AES_KEY_LENGTH_BITS)
        val confidentialityKey = keyGen.generateKey()

        //Now make the HMAC key
        val integrityKeyBytes = randomBytes(HMAC_KEY_LENGTH_BITS / 8) //to get bytes
        val integrityKey: SecretKey = SecretKeySpec(integrityKeyBytes, HMAC_ALGORITHM)
        return SecretKeys(confidentialityKey, integrityKey)
    }

    /**
     * A function that generates password-based AES and HMAC keys. It prints out exceptions but
     * doesn't throw them since none should be encountered. If they are
     * encountered, the return value is null.
     *
     * @param password The password to derive the keys from.
     * @return The AES and HMAC keys.
     * @throws GeneralSecurityException if AES is not implemented on this system,
     * or a suitable RNG is not available
     */
    @Throws(GeneralSecurityException::class)
    fun generateKeyFromPassword(password: String, salt: ByteArray?): SecretKeys {
        /*
       * I used Signal's implementation of Argon2 instead of PBKDF2 as a key derivation function.
       */
        val argon2 = Argon2.Builder(Version.V13)
                .type(Type.Argon2id)
                .memoryCost(MemoryCost.MiB(Constants.DEFAULT_MEMORY))
                .parallelism(PARALLELISM)
                .iterations(ITERATIONS)
                .hashLength((AES_KEY_LENGTH_BITS + HMAC_KEY_LENGTH_BITS)/8)
                .build()
        val keyBytes = argon2.hash(password.toByteArray(Charset.forName("UTF-8")),salt).hash
        // Split the random bytes into two parts:
        val confidentialityKeyBytes = copyOfRange(keyBytes, 0, AES_KEY_LENGTH_BITS / 8)
        val integrityKeyBytes = copyOfRange(keyBytes, AES_KEY_LENGTH_BITS / 8, AES_KEY_LENGTH_BITS / 8 + HMAC_KEY_LENGTH_BITS / 8)

        //Generate the AES key
        val confidentialityKey: SecretKey = SecretKeySpec(confidentialityKeyBytes, CIPHER)

        //Generate the HMAC key
        val integrityKey: SecretKey = SecretKeySpec(integrityKeyBytes, HMAC_ALGORITHM)
        return SecretKeys(confidentialityKey, integrityKey)
    }

    /**
     * Generates a random salt.
     * @return The random salt suitable for generateKeyFromPassword.
     */
    @Throws(GeneralSecurityException::class)
    fun generateSalt(): ByteArray {
        return randomBytes(PBE_SALT_LENGTH_BITS)
    }


    /**
     * Creates a random Initialization Vector (IV) of IV_LENGTH_BYTES.
     *
     * @return The byte array of this IV
     * @throws GeneralSecurityException if a suitable RNG is not available
     */
    @Throws(GeneralSecurityException::class)
    fun generateIv(): ByteArray {
        return randomBytes(IV_LENGTH_BYTES)
    }

    @Throws(GeneralSecurityException::class)
    private fun randomBytes(length: Int): ByteArray {
        val random = SecureRandom()
        val b = ByteArray(length)
        random.nextBytes(b)
        return b
    }


    /*
     * -----------------------------------------------------------------
     * Encryption
     * -----------------------------------------------------------------
     */
    /**
     * Generates a random IV and encrypts this plain text with the given key. Then attaches
     * a hashed MAC, which is contained in the CipherTextIvMac class.
     *
     * @param plaintext The text that will be encrypted, which
     * will be serialized with UTF-8
     * @param secretKeys The AES and HMAC keys with which to encrypt
     * @return a tuple of the IV, ciphertext, mac
     * @throws GeneralSecurityException if AES is not implemented on this system
     * @throws UnsupportedEncodingException if UTF-8 is not supported in this system
     */
    @Throws(UnsupportedEncodingException::class, GeneralSecurityException::class)
    fun encrypt(plaintext: String, secretKeys: SecretKeys): CipherTextIvMac {
        return encrypt(plaintext, secretKeys, "UTF-8")
    }

    /**
     * Generates a random IV and encrypts this plain text with the given key. Then attaches
     * a hashed MAC, which is contained in the CipherTextIvMac class.
     *
     * @param plaintext The bytes that will be encrypted
     * @param secretKeys The AES and HMAC keys with which to encrypt
     * @return a tuple of the IV, ciphertext, mac
     * @throws GeneralSecurityException if AES is not implemented on this system
     * @throws UnsupportedEncodingException if the specified encoding is invalid
     */
    @Throws(UnsupportedEncodingException::class, GeneralSecurityException::class)
    fun encrypt(plaintext: String, secretKeys: SecretKeys, encoding: String?): CipherTextIvMac {
        return encrypt(plaintext.toByteArray(charset(encoding!!)), secretKeys)
    }

    /**
     * Generates a random IV and encrypts this plain text with the given key. Then attaches
     * a hashed MAC, which is contained in the CipherTextIvMac class.
     *
     * @param plaintext The text that will be encrypted
     * @param secretKeys The combined AES and HMAC keys with which to encrypt
     * @return a tuple of the IV, ciphertext, mac
     * @throws GeneralSecurityException if AES is not implemented on this system
     */
    @Throws(GeneralSecurityException::class)
    fun encrypt(plaintext: ByteArray?, secretKeys: SecretKeys): CipherTextIvMac {
        var iv = generateIv()
        val aesCipherForEncryption = Cipher.getInstance(CIPHER_TRANSFORMATION)
        aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKeys.confidentialityKey, IvParameterSpec(iv))

        /*
         * Now we get back the IV that will actually be used. Some Android
         * versions do funny stuff w/ the IV, so this is to work around bugs:
         */
        iv = aesCipherForEncryption.iv
        val byteCipherText = aesCipherForEncryption.doFinal(plaintext)
        val ivCipherConcat = CipherTextIvMac.ivCipherConcat(iv, byteCipherText)
        val integrityMac = generateMac(ivCipherConcat, secretKeys.integrityKey)
        return CipherTextIvMac(byteCipherText, iv, integrityMac)
    }


    /*
     * -----------------------------------------------------------------
     * Decryption
     * -----------------------------------------------------------------
     */
    /**
     * AES CBC decrypt.
     *
     * @param civ The cipher text, IV, and mac
     * @param secretKeys The AES and HMAC keys
     * @param encoding The string encoding to use to decode the bytes after decryption
     * @return A string derived from the decrypted bytes (not base64 encoded)
     * @throws GeneralSecurityException if AES is not implemented on this system
     * @throws UnsupportedEncodingException if the encoding is unsupported
     */
    @Throws(UnsupportedEncodingException::class, GeneralSecurityException::class)
    fun decryptString(civ: CipherTextIvMac, secretKeys: SecretKeys, encoding: String?): String? {
        val result = decrypt(civ, secretKeys)
        return result?.let {String(result, charset(encoding!!)) }
    }

    /**
     * AES CBC decrypt.
     *
     * @param civ The cipher text, IV, and mac
     * @param secretKeys The AES and HMAC keys
     * @return A string derived from the decrypted bytes, which are interpreted
     * as a UTF-8 String
     * @throws GeneralSecurityException if AES is not implemented on this system
     * @throws UnsupportedEncodingException if UTF-8 is not supported
     */
    @Throws(UnsupportedEncodingException::class, GeneralSecurityException::class)
    fun decryptString(civ: CipherTextIvMac, secretKeys: SecretKeys): String? {
        return decryptString(civ, secretKeys, "UTF-8")
    }

    /**
     * AES CBC decrypt.
     *
     * @param civ the cipher text, iv, and mac
     * @param secretKeys the AES and HMAC keys
     * @return The raw decrypted bytes
     * @throws GeneralSecurityException if MACs don't match or AES is not implemented
     */
    @Throws(GeneralSecurityException::class)
    fun decrypt(civ: CipherTextIvMac, secretKeys: SecretKeys): ByteArray? {
        val ivCipherConcat = CipherTextIvMac.ivCipherConcat(civ.iv, civ.cypherText)
        val computedMac = generateMac(ivCipherConcat, secretKeys.integrityKey)
        return if (constantTimeEq(computedMac, civ.mac)) {
            val aesCipherForDecryption = Cipher.getInstance(CIPHER_TRANSFORMATION)
            aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKeys.confidentialityKey,
                    IvParameterSpec(civ.iv))
            aesCipherForDecryption.doFinal(civ.cypherText)
        } else {
            null
        }
    }

    /*
     * -----------------------------------------------------------------
     * Helper Code
     * -----------------------------------------------------------------
     */

    /*
     * -----------------------------------------------------------------
     * Helper Code
     * -----------------------------------------------------------------
     */
    /**
     * Generate the mac based on HMAC_ALGORITHM
     * @param integrityKey The key used for hmac
     * @param byteCipherText the cipher text
     * @return A byte array of the HMAC for the given key and ciphertext
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun generateMac(byteCipherText: ByteArray?, integrityKey: SecretKey?): ByteArray {
        //Now compute the mac for later integrity checking
        val sha256_HMAC = Mac.getInstance(HMAC_ALGORITHM)
        sha256_HMAC.init(integrityKey)
        return sha256_HMAC.doFinal(byteCipherText)
    }


    /**
     * Simple constant-time equality of two byte arrays. Used for security to avoid timing attacks.
     * @param a
     * @param b
     * @return true iff the arrays are exactly equal.
     */
    private fun constantTimeEq(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }


    /**
     * Copy the elements from the start to the end
     *
     * @param from  the source
     * @param start the start index to copy
     * @param end   the end index to finish
     * @return the new buffer
     */
    private fun copyOfRange(from: ByteArray, start: Int, end: Int): ByteArray {
        val length = end - start
        val result = ByteArray(length)
        System.arraycopy(from, start, result, 0, length)
        return result
    }

}