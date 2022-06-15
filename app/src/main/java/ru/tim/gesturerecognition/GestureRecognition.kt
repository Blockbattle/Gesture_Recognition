package ru.tim.gesturerecognition

import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.solutions.hands.HandLandmark

/**
 * Класс для распознвания жестов.
 */
class GestureRecognition {
    companion object {
        private enum class Letters {
            A, B, V, G, D, E, YO, ZH, Z, I, K, L, M, N, O, P,R, S, T,
            U, F, H, C, CH, SH, znak, Y, EE, YU, YA, space, JO, J
        }

        /**
         * Выбирает букву с наибольшей вероятностью и возвращает её индекс.
         * @param landmarks список координат основных точек руки
         * @param label метка левой или правой руки
         * @return индекс буквы или пробела или -1 в случае неудачного запроса к серверу
         */
        fun getLetter(landmarks: List<LandmarkProto.NormalizedLandmark>, label: String): Int {
            val s = getGesturesFromServer(landmarks, label)
            val subs = s.split(" ")
            if (subs.size != 33)// != 28)
                return -1
            var confidence: Float = 0f
            var res: Int = -1
            for ((i, sub) in subs.withIndex()) {
                if (sub.toFloat() > confidence) {
                    confidence = sub.toFloat()
                    res = i
                }
            }
            // Проверяем не й ли это
            if (res == Letters.I.ordinal &&
                (landmarks[HandLandmark.INDEX_FINGER_MCP].x
                        < landmarks[HandLandmark.PINKY_MCP].x && label == "Left"
                        || landmarks[HandLandmark.INDEX_FINGER_MCP].x
                        > landmarks[HandLandmark.PINKY_MCP].x && label == "Right")) {
                return Letters.J.ordinal
            }
            return res
        }

        /**
         * Делает запрос к серверу и получает вероятности соответствия букв жесту.
         * @param landmarks список координат основных точек руки
         * @param label метка левой или правой руки
         * @return строку с вероятностями соответствия букв жесту
         */
        private fun getGesturesFromServer(
            landmarks: List<LandmarkProto.NormalizedLandmark>,
            label: String
        ): String {
            var url = "https://rnn-rus-mden4735-dev.apps.sandbox-m2.ll9k.p1.openshiftapps.com/letter?"
            //var x = ""
            //var y = ""
            for (i in landmarks.indices) {
                //x += "${landmarks[i].x}, "
                //y += "${landmarks[i].y}, "
                url += if (label == "Right") {
                    if (i == 0)
                        "x=${1 - landmarks[i].x}&y=${landmarks[i].y}"
                    else
                        "&x=${1 - landmarks[i].x}&y=${landmarks[i].y}"
                } else {
                    if (i == 0)
                        "x=${landmarks[i].x}&y=${landmarks[i].y}"
                    else
                        "&x=${landmarks[i].x}&y=${landmarks[i].y}"
                }
            }
            //Log.i("info", x)
            //Log.i("info", y)
            val response = khttp.get(url)
            if (response.statusCode != 200)
                return ""
            return khttp.get(url).text
        }
    }
}