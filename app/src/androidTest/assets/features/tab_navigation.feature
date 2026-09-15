Feature: Tab Navigation
  @smoke
  Scenario: Navigate between tabs
    Given I have completed onboarding with an active session
    When I tap the Calendar tab
    Then I should see the calendar screen
    When I tap the Timetable tab
    Then I should see the timetable screen
    When I tap the Settings tab
    Then I should see the settings screen
    When I tap the Home tab
    Then I should see the home screen
