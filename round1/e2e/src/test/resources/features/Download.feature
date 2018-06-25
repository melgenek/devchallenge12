Feature: Upload/Download

  Scenario: Single server file upload
    Given user creates a file with text "Text 1"
    And uploads this file to "http://localhost:8080" with id "file1" and version "v1"
    When user downloads file "file1" from "http://localhost:8080"
    Then file contains "Text 1"

  Scenario: Replicated file upload
    Given user creates a file with text "Text 1"
    And uploads this file to "http://localhost:8080" with id "file2" and version "v1"
    When user downloads file "file2" from "http://localhost:8080"
    Then file contains "Text 1"
    And user drinks coffee for a while
    When user downloads file "file2" from "http://localhost:8081"
    Then file contains "Text 1"
    When user downloads file "file2" from "http://localhost:8082"
    Then file contains "Text 1"

  Scenario: Versioned file upload
    Given user creates a file with text "Text 1"
    And uploads this file to "http://localhost:8080" with id "file3" and version "v1"
    And user creates a file with text "Text 123"
    And uploads this file to "http://localhost:8080" with id "file3" and version "v2"
    When user downloads file "file3" from "http://localhost:8080"
    Then file contains "Text 123"
    When user downloads file "file3" with version "v1" from "http://localhost:8080"
    Then file contains "Text 1"

  Scenario: Replication of updated file
    Given user creates a file with text "Text 1"
    And uploads this file to "http://localhost:8080" with id "file4" and version "v1"
    And user drinks coffee for a while
    And user creates a file with text "Text 22"
    And uploads this file to "http://localhost:8080" with id "file4" and version "v1"
    And user drinks coffee for a while
    When user downloads file "file4" with version "v1" from "http://localhost:8080"
    Then file contains "Text 22"
    When user downloads file "file4" with version "v1" from "http://localhost:8081"
    Then file contains "Text 22"
    When user downloads file "file4" with version "v1" from "http://localhost:8082"
    Then file contains "Text 22"
