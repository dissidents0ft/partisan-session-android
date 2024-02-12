package partisan_plugin.domain.entities

import android.util.Base64
import partisan_plugin.data.crypto.AesCbcUpdated

data class EncryptionDataContainer(val iv: String, val data: String) {
    fun getEncryptedIv(): ByteArray =
        Base64.decode(iv,AesCbcUpdated.BASE64_FLAGS)

    fun getEncryptedData(): ByteArray =
            Base64.decode(data,AesCbcUpdated.BASE64_FLAGS)


}