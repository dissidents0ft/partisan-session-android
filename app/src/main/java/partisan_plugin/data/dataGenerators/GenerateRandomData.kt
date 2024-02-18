package partisan_plugin.data.dataGenerators

import android.content.Context
import org.session.libsignal.crypto.MnemonicCodec
import org.session.libsignal.utilities.Hex
import org.session.libsignal.utilities.hexEncodedPrivateKey
import org.thoughtcrime.securesms.crypto.IdentityKeyUtil
import org.thoughtcrime.securesms.crypto.KeyPairUtilities
import org.thoughtcrime.securesms.crypto.MnemonicUtilities
import partisan_plugin.data.Constants
import java.security.SecureRandom
import kotlin.random.Random

/**
 * Class for fake data generation.
 **/
object GenerateRandomData  {
    private const val NAME_MIN_LENGTH = 10
    private const val NAME_MAX_LENGTH = 20
    /**
     * Function for generating fake Session seed using built-in functions of Session
     *  @param context Android context
     *  @return seed fake Session seed
     **/
    fun generateRandomSeed(context: Context): String {
        val seed = KeyPairUtilities.generate().seed
        var hexEncodedSeed = Hex.toStringCondensed(seed)
        if (hexEncodedSeed == null) {
            hexEncodedSeed = IdentityKeyUtil.getIdentityKeyPair(context).hexEncodedPrivateKey // Legacy account
        }
        val loadFileContents: (String) -> String = { fileName ->
            MnemonicUtilities.loadFileContents(context, fileName)
        }
        return MnemonicCodec(loadFileContents).encode(hexEncodedSeed, MnemonicCodec.Language.Configuration.english)
    }

    /**
     * Function for generating random string, used for test purposes only
     *  @return random string
     **/

    fun generateRandomName(): String =
        getRandomString(Random.nextInt(NAME_MIN_LENGTH, NAME_MAX_LENGTH))


    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
    }

    /**
     * Function for generating random distinct numbers using java.security.SecureRandom(), used for generating ids for real accounts
     *  @param size size of list of numbers to generate
     *  @return list of random numbers. Each number in list is less than database size, size of list is selected by user.
     **/
    fun generateRandomDistinctNumbers(size: Int): List<Int> {
        val result = mutableListOf<Int>()
        val indexes = (0 until Constants.DEFAULT_DATABASE_SIZE).toMutableList()
        var limit = Constants.DEFAULT_DATABASE_SIZE-1
        while (result.size<size) {
            val element = SecureRandom().nextInt(limit)
            result.add(indexes[element])
            val lastElement = indexes[limit]
            indexes[limit] = element
            indexes[element] = lastElement
            limit--
        }
        return result.toList()
    }
}