package ru.tim.gesturerecognition

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class TextDialogTest {
    @get:Rule
    var mActivityRule = ActivityScenarioRule(
        Menu::class.java
    )

    @Before
    fun setUp() {
        Espresso.onView(withId(R.id.cameraButton)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.layout)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.editText)).perform(ViewActions.clearText())
        Espresso.onView(withId(R.id.editText)).perform(ViewActions.typeText("how are you"))
        Espresso.onView(withId(R.id.saveButton)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.switchMode)).perform(ViewActions.click())
    }

    @Test
    fun check_text() {
        Espresso.onView(withText("how are you"))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun ok_button() {
        Espresso.onView(withId(R.id.buttonOk))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.buttonOk)).perform(ViewActions.click())
    }
}