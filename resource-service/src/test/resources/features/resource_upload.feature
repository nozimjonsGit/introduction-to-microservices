Feature: Resource upload

  Scenario: Upload an MP3 and emit Kafka event
    Given a valid MP3 file
    When I POST the payload to "/resources"
    Then the response JSON contains an "id"
    And a message with that id is published to the Kafka topic "resource-uploaded"