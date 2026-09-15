package com.aditya.present.androidtest.steps

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class AttendanceSteps(
    private val composeRuleHolder: ComposeRuleHolder,
) {

    private val composeRule: ComposeTestRule
        get() = composeRuleHolder.composeRule

    @When("I tap on the {string} subject card")
    fun iTapSubjectCard(name: String) {
        composeRule.onNodeWithText(name).performClick()
        composeRule.waitForIdle()
    }

    @Then("I should see the attendance marking sheet")
    fun seeAttendanceMarkingSheet() {
        composeRule.onNodeWithText("Mark today's attendance").assertIsDisplayed()
    }

    @Then("I should see {string} in a snackbar")
    fun seeSnackbarMessage(message: String) {
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Then("the attendance sheet should be dismissed")
    fun attendanceSheetDismissed() {
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("Mark today's attendance").fetchSemanticsNodes().isEmpty()
        }
    }
}
