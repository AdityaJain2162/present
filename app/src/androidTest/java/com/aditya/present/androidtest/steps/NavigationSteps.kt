package com.aditya.present.androidtest.steps

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class NavigationSteps(
    private val composeRuleHolder: ComposeRuleHolder,
) {

    private val composeRule: ComposeTestRule
        get() = composeRuleHolder.composeRule

    @When("I tap the Calendar tab")
    fun iTapCalendarTab() {
        composeRule.onNodeWithText("Calendar").performClick()
        composeRule.waitForIdle()
    }

    @When("I tap the Timetable tab")
    fun iTapTimetableTab() {
        composeRule.onNodeWithText("Timetable").performClick()
        composeRule.waitForIdle()
    }

    @When("I tap the Settings tab")
    fun iTapSettingsTab() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForIdle()
    }

    @When("I tap the Home tab")
    fun iTapHomeTab() {
        composeRule.onNodeWithText("Home").performClick()
        composeRule.waitForIdle()
    }

    @Then("I should see the calendar screen")
    fun seeCalendarScreen() {
        composeRule.waitForIdle()
        // Calendar has month summary or day headers
        composeRule.onNodeWithText("Month Summary").assertIsDisplayed()
    }

    @Then("I should see the timetable screen")
    fun seeTimetableScreen() {
        composeRule.waitForIdle()
        // Timetable has day strip
        composeRule.onNodeWithText("Timetable").assertIsDisplayed()
    }

    @Then("I should see the settings screen")
    fun seeSettingsScreen() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
    }
}
