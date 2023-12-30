package org.example.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.fail;

public class PeopleApiTest {
    private Response movieResponse; // Instance variable to store the movie response

    private String terrainFixture;
    private String gravityFixture;

    @BeforeClass
    public void setUpFixtures() {
        // Define the expected fixtures for terrains and gravity
        terrainFixture = "tundra, ice caves, mountain ranges";
        gravityFixture = "1.1 standard";
    }


    //test 1 (it works)
    @Test(groups = "group1")
    public void testGetPeopleDetails() {
        // Set the base URL for the API
        RestAssured.baseURI = "https://swapi.dev/api/";

        // Send a GET request to the endpoint people/2/
        Response response = given().get("people/2/");

        // Verify the success response (status code 200)
        assertEquals(response.getStatusCode(), 200, "Unexpected status code");

        // Verify the skin color is gold
        String skinColor = response.jsonPath().getString("skin_color");
        assertEquals(skinColor, "gold", "Unexpected skin color");

        // Verify the number of films it appears on is 6
        int filmsCount = response.jsonPath().getList("films").size();
        assertEquals(filmsCount, 6, "Unexpected number of films");
    }


    //test 2 (it works)
    @Test(dependsOnGroups = "group1")
    public void testSecondMovieDetails() {
        RestAssured.baseURI = "https://swapi.dev/api/";

        // Send a GET request to the endpoint people/2/
        Response response = given().get("people/2/");
        // Extract movie URL from the response
        String movieUrl = response.jsonPath().getString("films[1]");

        // Make a request to the movie URL
        movieResponse = given().get(movieUrl);

        // Verify the success response (status code 200)
        assertEquals(movieResponse.getStatusCode(), 200, "Unexpected status code");

        // Verify the release date format (adjust the date format as needed)
        String releaseDate = movieResponse.jsonPath().getString("release_date");
        // Add your own date format validation logic here

        // Verify each element includes more than 1 element
        assertElementCountGreaterThanOne(movieResponse, "characters");
        assertElementCountGreaterThanOne(movieResponse, "planets");
        assertElementCountGreaterThanOne(movieResponse, "starships");
        assertElementCountGreaterThanOne(movieResponse, "vehicles");
        assertElementCountGreaterThanOne(movieResponse, "species");
    }

    // Helper method to assert that a JSON array has more than one element
    private void assertElementCountGreaterThanOne(Response response, String elementName) {
        int elementCount = response.jsonPath().getList(elementName).size();
        assertTrue(elementCount > 1, elementName + " should have more than 1 element");
    }

    // Helper method to validate date format (adjust the pattern as needed)
    private boolean validateDateFormat(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //test 3 (it works)
    @Test(dependsOnGroups = "group1")
    public void testFirstPlanetInLastFilm() {
        //My annotations: considering putting this on a before method or before class
        RestAssured.baseURI = "https://swapi.dev/api/";

        // Send a GET request to the endpoint people/2/
        Response peopleResponse = given().get("people/2/");

        // Extract movie URL from the response
        String movieUrl = peopleResponse.jsonPath().getString("films[1]");

        // Make a request to the movie URL
        movieResponse = given().get(movieUrl);

        // Verify the success response (status code 200)
        assertEquals(movieResponse.getStatusCode(), 200, "Unexpected status code");

        // Extract the URL of the first planet from the last film's response
        String planetUrl = movieResponse.jsonPath().getString("planets[0]");

        // Make a request to the planet URL
        Response planetResponse = given().get(planetUrl);

        // Verify the success response (status code 200)
        assertEquals(planetResponse.getStatusCode(), 200, "Unexpected status code");

        // Verify the gravity matches the fixture value
        String gravity = planetResponse.jsonPath().getString("gravity");
        assertEquals(gravity, gravityFixture, "Gravity does not match the expected value");

        // Verify the terrains match the fixture values
        //solve problem, there is a null pointer exception
        String terrain = planetResponse.jsonPath().getString("terrain");

// Check if the terrain field is not null before further processing
        if (terrain != null) {
            // Verify that the terrain matches the fixture value
            assertEquals(terrain, terrainFixture, "Terrain does not match the expected value");
        } else {
            // Handle the case where the terrain field is null (add appropriate error handling or logging)
            fail("Terrain field is null in the response");
        }
    }


    //test 4 (it works)
    @Test(dependsOnGroups = "group1")
    public void testPlanetUrlVerification() {
        RestAssured.baseURI = "https://swapi.dev/api/";

        // Send a GET request to the endpoint people/2/
        Response peopleResponse = given().get("people/2/");

        // Extract movie URL from the response
        String movieUrl = peopleResponse.jsonPath().getString("films[1]");

        // Make a request to the movie URL
        movieResponse = given().get(movieUrl);

        // Verify the success response (status code 200)
        assertEquals(movieResponse.getStatusCode(), 200, "Unexpected status code");

        // Extract the URL of the first planet from the last film's response
        String planetUrl = movieResponse.jsonPath().getString("planets[0]");

        // Make a request to the planet URL
        Response planetResponse = given().get(planetUrl);


        // Extract the URL of the planet from the planet response
        String planetUrlFromResponse = planetResponse.jsonPath().getString("url");

        // Make a request to the extracted planet URL
        Response planetUrlResponse = given().get(planetUrlFromResponse);

        // Verify the success response (status code 200)
        assertEquals(planetUrlResponse.getStatusCode(), 200, "Unexpected status code");

        // Validate that the response from the extracted planet URL is exactly the same as the previous one
        assertEquals(planetUrlResponse.getBody().asString(), planetResponse.getBody().asString(),
                "Responses from the two requests are not the same");
    }

    //test 5 (it works)
    @Test
    public void testFilmWith404() {
        // Set the base URL for the API
        RestAssured.baseURI = "https://swapi.dev/api/";

        // Send a GET request to the endpoint /films/7/
        Response response = given().get("/films/7/");

        // Verify the expected 404 status code
        assertEquals(response.getStatusCode(), 404, "Unexpected status code");
    }

}
