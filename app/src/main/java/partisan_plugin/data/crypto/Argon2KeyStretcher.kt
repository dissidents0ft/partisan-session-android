package partisan_plugin.data.crypto

import at.favre.lib.armadillo.KeyStretchingFunction
import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version
import partisan_plugin.TopLevelFunctions.toByteArray
import partisan_plugin.data.Constants
import partisan_plugin.data.Constants.ITERATIONS
import partisan_plugin.data.Constants.PARALLELISM

/**
 * My KeyStretchingFunction based on Argon2 algorithm, recommended by OWASP. It uses Signal's implementation of Argon2.
 */
class Argon2KeyStretcher: KeyStretchingFunction {
    override fun stretch(salt: ByteArray?, password: CharArray?, outLengthByte: Int): ByteArray {
        val argon2 = Argon2.Builder(Version.V13)
                .type(Type.Argon2id)
                .memoryCost(MemoryCost.MiB(Constants.DEFAULT_MEMORY))
                .parallelism(PARALLELISM)
                .iterations(ITERATIONS)
                .hashLength(outLengthByte)
                .build()
        return argon2.hash(password!!.toByteArray(),salt).hash
    }

}