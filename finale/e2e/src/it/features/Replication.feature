Feature: Store names and values

  Scenario: Save records on replicas
    Given user stores name="key1" and value="value1" in service "api1"
    And user waits '3' seconds
    Then the value for name="key1" in service "api1" equals "value1"
    And the value for name="key1" in service "api2" equals "value1"
    And the value for name="key1" in service "api3" equals "value1"

  Scenario: Update records on replicas
    Given user stores name="key2" and value="value1" in service "api1"
    And user stores name="key2" and value="value2" in service "api1"
    And user waits '3' seconds
    Then the value for name="key2" in service "api1" equals "value2"
    And the value for name="key2" in service "api2" equals "value2"
    And the value for name="key2" in service "api3" equals "value2"

  Scenario: Delete records on replicas
    Given user stores name="key3" and value="value1" in service "api1"
    Given user deletes name="key3" in service "api1"
    And user waits '3' seconds
    Then the value for name="key3" in service "api1" is empty
    And the value for name="key3" in service "api2" is empty
    And the value for name="key3" in service "api3" is empty