Feature: Onboarding and Empty State
  @smoke
  Scenario: New user sees onboarding welcome
    Given the app is freshly launched
    Then I should see the onboarding welcome screen

  @smoke
  Scenario: User completes onboarding and sees empty dashboard
    Given the app is freshly launched
    When I tap "Get Started"
    Then I should see the session type selection
    When I tap "College / Semester"
    Then I should see the session details form
    When I tap "Create Semester"
    Then I should see the home screen with no subjects
    And I should see "Welcome to Present!"
