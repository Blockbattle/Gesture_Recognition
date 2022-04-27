package ru.tim.gesturerecognition

import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.solutions.hands.HandsResult

/**
 * Класс для распознвания жестов.
 */
class GestureRecognition {
    companion object {

        /**
         * Выбирает букву с наибольшей вероятностью и возвращает её индекс.
         * @param landmarks список координат основных точек руки
         * @return индекс буквы или пробела или -1 в случае неудачного запроса к серверу
         */
        fun getLetter(landmarks: List<LandmarkProto.NormalizedLandmark>):Int {
            val s = getGesturesFromServer(landmarks)
            val subs = s.split(" ")
            if (subs.size != 28)
                return -1
            var confidence: Float = 0f
            var res: Int = -1
            for ((i, sub) in subs.withIndex()) {
                if (sub.toFloat() > confidence) {
                    confidence = sub.toFloat()
                    res = i
                }
            }

            return res
        }

        /**
         * Делает запрос к серверу и получает вероятности соответствия букв жесту.
         * @param landmarks список координат основных точек руки
         * @return строку с вероятностями соответствия букв жесту
         */
        private fun getGesturesFromServer(landmarks: List<LandmarkProto.NormalizedLandmark>):String {
            var url = "https://gesture-server-mden4735-dev.apps.sandbox-m2.ll9k.p1.openshiftapps.com/letter?"
            for (i in landmarks.indices) {
                url += if (i == 0)
                    "x=${landmarks[i].x}&y=${landmarks[i].y}"
                else
                    "&x=${landmarks[i].x}&y=${landmarks[i].y}"
            }
            val response = khttp.get(url)
            if (response.statusCode != 200)
                return ""
            return khttp.get(url).text
        }
    }
}