package ru.tim.gesturerecognition

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MenuTest {
    @get:Rule
    var mActivityRule = ActivityScenarioRule(
        Menu::class.java
    )

    @Test
    fun on_camera_click() {
        onView(withId(R.id.cameraButton)).check(matches(isDisplayed()))
        onView(withId(R.id.cameraButton)).perform(click())
        onView(withId(R.id.preview_display_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun on_mic_click() {
        onView(withId(R.id.micButton)).check(matches(isDisplayed()))
        onView(withId(R.id.micButton)).perform(click())
    }
}