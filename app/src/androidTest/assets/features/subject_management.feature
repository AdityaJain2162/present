Feature: Subject Management
  @smoke
  Scenario: Add a new subject from empty state
    Given I have completed onboarding with an active session
    When I tap "Add Your First Subject"
    Then I should see the add subject form
    When I enter "Mathematics" as the subject name
    And I tap the save button
    Then I should see "Mathematics" in the subject list
    And I should see "MAT" as the acronym

  @smoke
  Scenario: Add a subject from the FAB
    Given I have a subject named "Physics"
    When I tap the add subject FAB
    Then I should see the add subject form
    When I enter "Chemistry" as the subject name
    And I tap the save button
    Then I should see "Chemistry" in the subject list
