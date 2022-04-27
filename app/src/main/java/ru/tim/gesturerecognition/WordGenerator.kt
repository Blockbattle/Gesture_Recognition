package ru.tim.gesturerecognition

import android.util.Log
import java.util.*

/**
 * Класс для генерации текста.
 * Класс отвечает за генерацию из жестов слов и предложений, их проверку и исправление.
 */
class WordGenerator {
    companion object {
        val letters: ArrayList<String> = ArrayList()
        var text: String = ""

        /**
         * Проверяет и исправляет слова.
         * Используется Яндекс Спеллер для проверки текста на ошибки.
         * В случае обнаружения ошибки в слове используется первый предложенный спеллером вариант слова.
         * @return текст с исправленными ошибками.
         */
        fun wordProcessing(): String {
            val url = "https://speller.yandex.net/services/spellservice.json/checkText?lang=en&text=$text"
            val response = khttp.get(url)
            if (response.statusCode != 200)
                return text

            val words = response.jsonArray
            if (words.length() == 0)
                return text
            else {
                var resText = ""
                var position = 0
                for (i in 0 until words.length()) {
                    val word = words.getJSONObject(i)
                    val newPosition = word.getInt("pos")
                    resText += text.substring(position, newPosition)

                    val newWord = word.getJSONArray("s").getString(0)
                    val oldWord = word.getString("word")
                    resText += newWord
                    position = newPosition + oldWord.length
                }
                resText += text.substring(position)

                return resText
            }
        }

        /**
         * Составляет текст из символов.
         * Обрабатывает получаемые буквы и знак пробела. Составляет из них текст.
         * Пропускает только три и больше повторяющихся подряд символов для
         * уменьшения искажений из-за ошибок классификатора жестов.
         */
        fun wordGeneration() {
            if (letters.size >= 2) {
                if (letters[letters.size - 1] == letters[letters.size - 2]) {
                    if (letters.size == 3) {
                        if (text.isNotEmpty() && text.last() != letters[0][0] || text.isEmpty())
                            text += letters[0]
                        letters.clear()
                    }
                } else {
                    val last: String = letters.last()
                    letters.clear()
                    letters.add(last)
                }
            }
            /*if (letters.size == 2) {
                if (letters[0] == letters[1]) {
                    if (text.isNotEmpty() && text.last() != letters[0][0] || text.isEmpty())
                        text += letters[0]
                    letters.clear()
                } else {
                    val last: String = letters.last()
                    letters.clear()
                    letters.add(last)
                }
            }*/
        }
    }
}