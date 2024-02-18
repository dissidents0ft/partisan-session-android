package org.thoughtcrime.securesms

import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import partisan_plugin.TopLevelFunctions.removePrefix
import partisan_plugin.TopLevelFunctions.startsWith
import partisan_plugin.TopLevelFunctions.toByteArray
import partisan_plugin.TopLevelFunctions.trim
import partisan_plugin.data.dataGenerators.GenerateRandomData


class CharArrayFunctionsTests {
    @Test
    fun testCharArrayToByteArray() {
        val string = GenerateRandomData.generateRandomName()
        val charArray = string.toCharArray()
        val byteArray = charArray.toByteArray()
        assertEquals(string, byteArray.decodeToString())
        assertNotEquals(string, String(charArray))
    }

    @Test
    fun testCharArrayStartsWith() {
        val string = GenerateRandomData.generateRandomName()
        val prefix = string.substring(0, string.length / 2)
        val suffix = string.substring(string.length / 2, string.length)
        assert(string.toCharArray().startsWith(prefix.toCharArray()))
        assertEquals(false, string.toCharArray().startsWith(suffix.toCharArray()))
        "dssds".toCharArray().startsWith("dasadsdasdas".toCharArray())
    }

    @Test
    fun testCharArrayRemovePrefix() {
        val string = GenerateRandomData.generateRandomName()
        val prefix = string.substring(0, string.length / 2)
        val prefixRemoved = String(string.toCharArray().removePrefix(prefix.toCharArray()))
        assertEquals(string.removePrefix(prefix), prefixRemoved)
        "dssds".toCharArray().removePrefix("dasadsdasdas".toCharArray())
    }

    @Test
    fun trimCharArrayTest() {
        val string = "  test  "
        val trimmed = String(string.toCharArray().trim())
        assertEquals(string.trim(), trimmed)
    }
}