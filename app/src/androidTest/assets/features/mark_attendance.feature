Feature: Mark Attendance
  @smoke
  Scenario: Mark a subject as present
    Given I have a subject named "Mathematics"
    When I tap on the "Mathematics" subject card
    Then I should see the attendance marking sheet
    When I tap "Present"
    Then I should see "Mathematics marked Present" in a snackbar
    And the attendance sheet should be dismissed

  @smoke
  Scenario: Mark a subject as absent
    Given I have a subject named "Mathematics"
    When I tap on the "Mathematics" subject card
    Then I should see the attendance marking sheet
    When I tap "Absent"
    Then I should see "Mathematics marked Absent" in a snackbar
    And the attendance sheet should be dismissed
