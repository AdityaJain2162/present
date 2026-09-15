package com.aditya.present.androidtest.steps

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class SubjectSteps(
    private val composeRuleHolder: ComposeRuleHolder,
) {

    private val composeRule: ComposeTestRule
        get() = composeRuleHolder.composeRule

    @Given("I have a subject named {string}")
    fun haveSubjectNamed(name: String) {
        composeRule.waitForIdle()
        // If onboarding is showing, complete it first
        val welcomeNodes = composeRule.onAllNodesWithText("Get Started").fetchSemanticsNodes()
        if (welcomeNodes.isNotEmpty()) {
            composeRule.onNodeWithText("Get Started").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("College / Semester").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Create Semester").performClick()
            composeRule.waitForIdle()
        }

        // If empty state, add the subject
        val emptyStateNodes = composeRule.onAllNodesWithText("Add Your First Subject").fetchSemanticsNodes()
        if (emptyStateNodes.isNotEmpty()) {
            composeRule.onNodeWithText("Add Your First Subject").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Subject name").performTextInput(name)
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Add Subject").performClick()
            composeRule.waitForIdle()
        }
    }

    @When("I tap {string}")
    fun iTapText(text: String) {
        composeRule.onNodeWithText(text).performClick()
        composeRule.waitForIdle()
    }

    @When("I tap the add subject FAB")
    fun iTapAddSubjectFab() {
        composeRule.onNodeWithText("Add Subject").performClick()
        composeRule.waitForIdle()
    }

    @Then("I should see the add subject form")
    fun seeAddSubjectForm() {
        composeRule.onNodeWithText("Subject name").assertIsDisplayed()
    }

    @When("I enter {string} as the subject name")
    fun iEnterSubjectName(name: String) {
        composeRule.onNodeWithText("Subject name").performTextInput(name)
        composeRule.waitForIdle()
    }

    @When("I tap the save button")
    fun iTapSaveButton() {
        composeRule.onNodeWithText("Add Subject").performClick()
        composeRule.waitForIdle()
    }

    @Then("I should see {string} in the subject list")
    fun seeSubjectInList(name: String) {
        composeRule.onNodeWithText(name).assertIsDisplayed()
    }

    @Then("I should see {string} as the acronym")
    fun seeAcronym(acronym: String) {
        composeRule.onNodeWithText(acronym).assertIsDisplayed()
    }
}
