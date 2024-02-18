package partisan_plugin

import java.nio.CharBuffer
import java.nio.charset.Charset

/**
 * My functions to add some String functionality to CharArray, because CharArray is better than String for storing passwords.
 */
object TopLevelFunctions {

    fun CharArray.toByteArray(): ByteArray {
        val array = ByteArray(this.size)
        val buffer = CharBuffer.wrap(this)
        Charset.forName("UTF-8").encode(buffer).get(array)
        this.clear()
        return array
    }

    fun CharArray.clear() {
        this.fill(Char(1))
    }

    fun CharArray.startsWith(array: CharArray): Boolean {
        if (array.size>size) return false
        return this.copyOfRange(0,array.size).contentEquals(array)
    }

    fun CharArray.removePrefix(array: CharArray): CharArray {
        if (this.startsWith(array)) {
            val result = this.copyOfRange(array.size, size)
            this.clear()
            return result
        }
        return this
    }

    fun CharArray.trim(): CharArray{
        val newSize = this.count { it!=' ' }
        val result = CharArray(newSize)
        var i = 0
        this.forEach { if (it!=' '){
                result[i]=it
                i++
            }
        }
        this.clear()
        return result
    }
}