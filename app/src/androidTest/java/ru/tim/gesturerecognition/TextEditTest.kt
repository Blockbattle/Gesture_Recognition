package ru.tim.gesturerecognition

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.hamcrest.Matchers.not
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

class TextEditTest {
    @get:Rule
    var mActivityRule = ActivityScenarioRule(
        Menu::class.java
    )

    @Before
    fun setUp() {
        onView(ViewMatchers.withId(R.id.cameraButton)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.layout)).perform(ViewActions.click())
    }

    @Test
    fun on_close_click() {
        onView(ViewMatchers.withId(R.id.editText)).perform(typeText("wow"))
        onView(ViewMatchers.withId(R.id.closeButton)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.textView)).check(matches(not(withSubstring("wow"))))
    }

    @Test
    fun on_save_click() {
        onView(ViewMatchers.withId(R.id.editText)).perform(clearText())
        onView(ViewMatchers.withId(R.id.editText)).perform(typeText("okay"))
        onView(ViewMatchers.withId(R.id.saveButton)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.textView)).check(matches(withText("okay")))
    }
}