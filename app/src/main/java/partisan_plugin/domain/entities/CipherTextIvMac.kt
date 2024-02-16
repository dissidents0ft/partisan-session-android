package partisan_plugin.domain.entities

import android.util.Base64
import partisan_plugin.data.crypto.AesCbcUpdated

data class CipherTextIvMac(val cypherText: ByteArray, val iv: ByteArray, val mac: ByteArray) {

    constructor(cypherText: String,iv: String, mac: String): this(Base64.decode(cypherText,AesCbcUpdated.BASE64_FLAGS),Base64.decode(iv,AesCbcUpdated.BASE64_FLAGS),Base64.decode(mac,AesCbcUpdated.BASE64_FLAGS))

    fun getCypherString() = Base64.encodeToString(cypherText,AesCbcUpdated.BASE64_FLAGS)

    fun getIvString() =  Base64.encodeToString(iv,AesCbcUpdated.BASE64_FLAGS)

    fun getMacString() =  Base64.encodeToString(mac,AesCbcUpdated.BASE64_FLAGS)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CipherTextIvMac

        if (!cypherText.contentEquals(other.cypherText)) return false
        if (!iv.contentEquals(other.iv)) return false
        return mac.contentEquals(other.mac)
    }

    override fun hashCode(): Int {
        var result = cypherText.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + mac.contentHashCode()
        return result
    }


    companion object {
        fun ivCipherConcat(iv: ByteArray, cipherText: ByteArray): ByteArray {
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            return combined
        }
    }
}
