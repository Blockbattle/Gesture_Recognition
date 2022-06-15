package ru.tim.gesturerecognition

import android.util.Log
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.solutions.hands.HandLandmark.INDEX_FINGER_MCP
import com.google.mediapipe.solutions.hands.HandLandmark.PINKY_MCP
import java.util.*
import kotlin.collections.ArrayList

/**
 * Класс для генерации текста.
 * Класс отвечает за генерацию из жестов слов и предложений, их проверку и исправление.
 */
class WordGenerator {
    companion object {
        val letters: ArrayList<String> = ArrayList()
        val wrists: ArrayList<LandmarkProto.NormalizedLandmark> = ArrayList()
        var text: String = ""

        /**
         * Проверяет и исправляет слова.
         * Используется Яндекс Спеллер для проверки текста на ошибки.
         * В случае обнаружения ошибки в слове используется первый предложенный спеллером вариант слова.
         * @return текст с исправленными ошибками.
         */
        fun wordProcessing(): String {
            val url =
                "https://speller.yandex.net/services/spellservice.json/checkText?lang=ru&text=$text"
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
                    if (letters.size >= 3) {
                        //преобразуем букву ш в щ если есть движение
                        if (letters[0][0] == 'ш' &&
                            wrists[wrists.size - 1].y - wrists[0].y > 0.15
                        ) {
                            if (text.isNotEmpty() && text.last() == 'ш') {
                                text = text.removeSuffix("ш")
                            }
                            letters[0] = "щ"
                        }

                        if (text.isNotEmpty() && text.last() != letters[0][0] || text.isEmpty())
                            //удаляем невозможные сочетания, которые могут возникать
                            if (!(text.isNotEmpty() &&
                                        (text.last() == 'ё' && letters[0][0] == 'е' ||
                                                text.last() == 'й' && letters[0][0] == 'и' ||
                                                text.last() == 'щ' && letters[0][0] == 'ш')
                                        )
                            ) {
                                text += letters[0]
                            }
                        if (letters[0][0] != 'ш') {
                            letters.clear()
                            wrists.clear()
                        }
                    }
                } else {
                    val lastL = letters.last()
                    val lastW = wrists.last()
                    letters.clear()
                    wrists.clear()
                    letters.add(lastL)
                    wrists.add(lastW)
                }
            }
        }
    }
}