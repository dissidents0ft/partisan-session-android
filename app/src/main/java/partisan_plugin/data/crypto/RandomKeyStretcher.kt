package partisan_plugin.data.crypto

import at.favre.lib.armadillo.KeyStretchingFunction
import partisan_plugin.data.Constants.CIPHER
import javax.crypto.KeyGenerator


/**
 * My KeyStretchingFunction for creating fake encrypted data that just return random key and don't save it in anywhere
 */
class RandomKeyStretcher: KeyStretchingFunction {
    override fun stretch(salt: ByteArray?, password: CharArray?, outLengthByte: Int): ByteArray {
        val keyGen = KeyGenerator.getInstance(CIPHER)
        keyGen.init(outLengthByte * 8)
        return keyGen.generateKey().encoded
    }
}