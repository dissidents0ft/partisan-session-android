package partisan_plugin.domain.entities

import android.util.Base64
import partisan_plugin.data.crypto.AesCbcUpdated
import javax.crypto.SecretKey

data class SecretKeys(val confidentialityKey: SecretKey, val integrityKey: SecretKey) {
    fun getEncoded(): Pair<ByteArray,ByteArray> {
        return Pair(confidentialityKey.encoded,integrityKey.encoded)
    }

    fun getStrings(): Pair<String,String> {
        return Pair(Base64.encodeToString(confidentialityKey.encoded,AesCbcUpdated.BASE64_FLAGS),Base64.encodeToString(integrityKey.encoded,AesCbcUpdated.BASE64_FLAGS))
    }
}
