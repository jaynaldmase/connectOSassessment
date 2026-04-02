Feature: The Internet
  This feature covers (some) Example pages on 'the-internet.herokuapp.com'

  @TEST_TI_0001
  Scenario: Verify that the Homepage displays the expected list of example links
    Given the page under test is loaded
    When the homepage is fully displayed
    Then the homepage should display the expected list of example links

  @TEST_TI_0002
  Scenario: Verify that Basic Auth allows validated access
    Given the page under test is loaded
    When the user navigates to Basic Auth
    And the user supplies valid credentials
    Then the Basic Auth page should display the congratulations message

  @TEST_TI_0003
  Scenario: Verify that Sortable Data Tables displays the expected Example 1 results
    Given the page under test is loaded
    When the user navigates to Sortable Data Tables
    Then the Sortable Data Tables page should display the expected Example 1 data
