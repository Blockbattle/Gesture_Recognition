package ru.tim.gesturerecognition

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule

class CameraTest {

    @get:Rule
    var mActivityRule = ActivityScenarioRule(
        Menu::class.java
    )

    @Before
    fun setUp() {
        Espresso.onView(ViewMatchers.withId(R.id.cameraButton)).perform(ViewActions.click())
    }

    @Test
    fun on_text_click() {
        Espresso.onView(ViewMatchers.withId(R.id.layout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.editText))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun on_mic_click() {
        Espresso.onView(ViewMatchers.withId(R.id.switchMode)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("OK"))
            .inRoot(RootMatchers.isDialog()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun on_camera_click() {
        Espresso.onView(ViewMatchers.withId(R.id.switchCamera)).perform(ViewActions.click())
    }

    @Test
    fun on_check_click() {
        Espresso.onView(ViewMatchers.withId(R.id.checkText)).perform(ViewActions.click())
    }
}