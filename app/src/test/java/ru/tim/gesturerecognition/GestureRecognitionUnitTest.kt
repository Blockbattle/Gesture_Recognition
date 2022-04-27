package ru.tim.gesturerecognition

import android.graphics.Bitmap
import android.graphics.Color
import com.google.common.collect.ImmutableList
import com.google.common.reflect.TypeToken
import com.google.mediapipe.formats.proto.LandmarkProto
import com.google.mediapipe.framework.Packet
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class GestureRecognitionUnitTest {
    @Test
    fun get_gestures_from_server() {
        val landmarkList = ArrayList<LandmarkProto.NormalizedLandmark>()

        val landmarkClass = LandmarkProto.NormalizedLandmark::class.java
        val constructor = landmarkClass.getDeclaredConstructor()
        constructor.isAccessible = true

        val methodX = landmarkClass.getDeclaredMethod("setX", Float::class.java)
        methodX.isAccessible = true
        val methodY = landmarkClass.getDeclaredMethod("setY", Float::class.java)
        methodY.isAccessible = true

        val rand = Random(47)

        for (i in 1..21) {
            val landmark = constructor.newInstance()
            methodX.invoke(landmark, rand.nextFloat())
            methodY.invoke(landmark, rand.nextFloat())

            landmarkList.add(landmark)
        }

        val grClass = GestureRecognition.Companion::class.java
        val getGesturesFromServer = grClass.getDeclaredMethod(
            "getGesturesFromServer",
            List::class.java
        )
        getGesturesFromServer.isAccessible = true
        assertEquals((getGesturesFromServer.invoke(GestureRecognition.Companion, landmarkList) as String).split(" ").size, 28)
    }

    @Test
    fun get_letter() {
        val landmarkList = ArrayList<LandmarkProto.NormalizedLandmark>()
        val grClass = GestureRecognition.Companion::class.java
        val getLetter = grClass.getDeclaredMethod(
            "getLetter",
            List::class.java
        )
        assertEquals(getLetter.invoke(GestureRecognition.Companion, landmarkList) as Int, -1)
    }

    @Test
    fun get_letter2() {
        val landmarkList = ArrayList<LandmarkProto.NormalizedLandmark>()

        val landmarkClass = LandmarkProto.NormalizedLandmark::class.java
        val constructor = landmarkClass.getDeclaredConstructor()
        constructor.isAccessible = true

        val methodX = landmarkClass.getDeclaredMethod("setX", Float::class.java)
        methodX.isAccessible = true
        val methodY = landmarkClass.getDeclaredMethod("setY", Float::class.java)
        methodY.isAccessible = true

        val rand = Random(47)

        for (i in 1..21) {
            val landmark = constructor.newInstance()
            methodX.invoke(landmark, rand.nextFloat())
            methodY.invoke(landmark, rand.nextFloat())

            landmarkList.add(landmark)
        }
        val grClass = GestureRecognition.Companion::class.java
        val getLetter = grClass.getDeclaredMethod(
            "getLetter",
            List::class.java
        )
        assertEquals(getLetter.invoke(GestureRecognition.Companion, landmarkList) as Int, 25)
    }

}