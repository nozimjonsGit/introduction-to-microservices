Feature: Resource upload

  Scenario: Upload and process a valid MP3 file
    Given a valid MP3 file
    When the MP3 is uploaded
    Then the response status is 200 and response contains an id of the created resource
    Then the song metadata eventually matches:
      | name     | Yoshligimga qaytgim keladi (UzHits.Net)  |
      | artist   | Xurshid Rasulov                          |
      | album    | UzHits.Net                               |
      | duration | 04:18                                    |
      | year     | 2018                                     |
    And the original MP3 can be re-downloaded intact
