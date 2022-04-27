package ru.tim.gesturerecognition

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.*
import kotlin.collections.ArrayList

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class WordGeneratorUnitTest {
    @Before
    fun clear_variables() {
        WordGenerator.letters.clear()
        WordGenerator.text = ""
    }

    @Test
    fun word_processing1() {
        WordGenerator.text = "How cam I hel you?"
        val resText = WordGenerator.wordProcessing()
        assertEquals(resText, "How can I help you?")
    }
    @Test
    fun word_processing2() {
        WordGenerator.text = "How can I help you?"
        val resText = WordGenerator.wordProcessing()
        assertEquals(resText, "How can I help you?")
    }
    @Test
    fun word_generation1() {
        WordGenerator.text = "h"
        WordGenerator.letters.add("b")
        WordGenerator.letters.add("e")
        WordGenerator.wordGeneration()
        assertEquals(WordGenerator.text, "h")
        assertEquals(WordGenerator.letters, ArrayList<String>(listOf("e")))
    }
    @Test
    fun word_generation2() {
        WordGenerator.text = "hell"
        WordGenerator.letters.add("o")
        WordGenerator.letters.add("o")
        WordGenerator.letters.add("o")
        WordGenerator.wordGeneration()
        assertEquals(WordGenerator.text, "hello")
        assertEquals(WordGenerator.letters, ArrayList<String>())
    }
    @Test
    fun word_generation3() {
        WordGenerator.text = ""
        WordGenerator.letters.add("i")
        WordGenerator.letters.add("i")
        WordGenerator.letters.add("i")
        WordGenerator.wordGeneration()
        assertEquals(WordGenerator.text, "i")
        assertEquals(WordGenerator.letters, ArrayList<String>())
    }
    @Test
    fun word_generation4() {
        WordGenerator.text = "h"
        WordGenerator.letters.add("e")
        WordGenerator.wordGeneration()
        assertEquals(WordGenerator.text, "h")
        assertEquals(WordGenerator.letters, ArrayList<String>(listOf("e")))
    }
    @Test
    fun word_generation5() {
        WordGenerator.text = "hell"
        WordGenerator.letters.add("o")
        WordGenerator.letters.add("o")
        WordGenerator.wordGeneration()
        assertEquals(WordGenerator.text, "hell")
        assertEquals(WordGenerator.letters, ArrayList<String>(listOf("o", "o")))
    }
    @Test
    fun word_generation6() {
        WordGenerator.text = "hello"
        WordGenerator.letters.add("o")
        WordGenerator.letters.add("o")
        WordGenerator.letters.add("o")
        WordGenerator.wordGeneration()
        assertEquals(WordGenerator.text, "hello")
        assertEquals(WordGenerator.letters, ArrayList<String>())
    }
}