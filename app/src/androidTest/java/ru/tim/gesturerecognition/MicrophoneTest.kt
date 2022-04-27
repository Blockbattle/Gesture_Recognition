package ru.tim.gesturerecognition

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class MicrophoneTest {

    @get:Rule
    var mActivityRule = ActivityScenarioRule(
        Menu::class.java
    )

    @Before
    fun setUp() {
        onView(withId(R.id.micButton)).perform(click())
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val uiObject = mDevice.findObject(UiSelector().text("Please speak"))
        if (uiObject.exists()) {
            mDevice.pressBack()
        }
    }

    @Test
    fun on_text_click() {
        onView(withId(R.id.layout)).perform(click())
        onView(withId(R.id.editText)).check(matches(isDisplayed()))
    }

    @Test
    fun on_mic_click() {
        onView(withId(R.id.micButton)).perform(click())
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val uiObject = mDevice.findObject(UiSelector().text("Please speak"))
        assertTrue(uiObject.exists())
    }

    @Test
    fun on_camera_click() {
        onView(withId(R.id.switchButton)).perform(click())
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun on_check_click() {
        onView(withId(R.id.checkButton)).perform(click())
    }
}