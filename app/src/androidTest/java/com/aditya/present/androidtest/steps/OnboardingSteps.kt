package com.aditya.present.androidtest.steps

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class OnboardingSteps(
    private val composeRuleHolder: ComposeRuleHolder,
) {

    private val composeRule: ComposeTestRule
        get() = composeRuleHolder.composeRule

    @Given("the app is freshly launched")
    fun appIsFreshlyLaunched() {
        composeRule.waitForIdle()
    }

    @Given("I have completed onboarding with an active session")
    fun completedOnboarding() {
        composeRule.waitForIdle()
        // If onboarding is showing, complete it
        val welcomeNodes = composeRule.onAllNodesWithText("Get Started").fetchSemanticsNodes()
        if (welcomeNodes.isNotEmpty()) {
            composeRule.onNodeWithText("Get Started").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("College / Semester").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Create Semester").performClick()
            composeRule.waitForIdle()
        }
    }

    @Then("I should see the onboarding welcome screen")
    fun seeOnboardingWelcome() {
        composeRule.onNodeWithText("Present").assertIsDisplayed()
    }

    @Then("I should see the session type selection")
    fun seeSessionTypeSelection() {
        composeRule.onNodeWithText("College / Semester").assertIsDisplayed()
    }

    @Then("I should see the session details form")
    fun seeSessionDetailsForm() {
        composeRule.onNodeWithText("Create Semester").assertIsDisplayed()
    }

    @Then("I should see the home screen with no subjects")
    fun seeHomeScreenNoSubjects() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Welcome to Present!").assertIsDisplayed()
    }

    @Then("I should see the home screen")
    fun seeHomeScreen() {
        composeRule.waitForIdle()
        // Home screen has the app name "Present" in the top bar
        composeRule.onNodeWithText("Present").assertIsDisplayed()
    }
}
