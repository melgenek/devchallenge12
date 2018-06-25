Feature: Minify server should respond with minified css

  Background:
    Given user clears the cache

  Scenario: Response to normal pages contains css
    Given user requests to minify:
      | http://nginx:8081/page1/ |
    Then response contains css for pages:
      | url                      | css             |
      | http://nginx:8081/page1/ | .red{color:red} |


  Scenario: Cache page with long processing time
    Given user requests to minify:
      | http://nginx:8081/page2/ |
    And response contains no pages
    And user waits '15' seconds
    And user requests to minify:
      | http://nginx:8081/page2/ |
    Then response contains css for pages:
      | url                      | css                |
      | http://nginx:8081/page2/ | .black{color:#000} |


  Scenario: Cache is cleared
    Given user requests to minify:
      | http://nginx:8081/page2/ |
    And response contains no pages
    And user waits '15' seconds
    And user clears the cache
    And user requests to minify:
      | http://nginx:8081/page2/ |
    Then response contains no pages


  Scenario: Redirect pages have no response is cleared
    Given user requests to minify:
      | http://nginx:8081/redirect1 |
    Then response contains no pages
