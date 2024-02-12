package network.loki.messenger

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.util.Log
import org.junit.Test
import org.junit.runner.RunWith
import partisan_plugin.data.dataGenerators.GenerateRandomData

@RunWith(AndroidJUnit4::class)
@SmallTest
class RandomTests {
    @Test
    fun testDistinctNumbersGenerator() {
        for (size in 3..14) {
            val numbers = GenerateRandomData.generateRandomDistinctNumbers(size)
            Log.w("testRes_numbers",numbers.toString())
            assert(numbers.size == size)
            numbers.forEach { assert(it in 0..14) }
            assert(numbers.toSet().size == numbers.size)
        }
    }

    @Test
    fun testSeedGenerator() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val seed = GenerateRandomData.generateRandomSeed(context)
        Log.w("seed_result",seed)
    }
}