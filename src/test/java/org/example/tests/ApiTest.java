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

public class ApiTest {
    private Response movieResponse; // Variable de instancia para almacenar la respuesta de la película

    private String terrainFixture;
    private String gravityFixture;

    @BeforeClass
    public void setUpFixtures() {
        // Define las fixtures esperadas para terrains y gravity
        terrainFixture = "tundra, ice caves, mountain ranges";
        gravityFixture = "1.1 standard";
    }

    // Método de ayuda para verificar que un array JSON tiene más de un elemento
    private void assertElementCountGreaterThanOne(Response response, String elementName) {
        int elementCount = response.jsonPath().getList(elementName).size();
        assertTrue(elementCount > 1, elementName + " debería tener más de 1 elemento");
    }

    // Método de ayuda para validar el formato de fecha
    private boolean validateDateFormat(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(date, formatter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Método de ayuda para realizar la solicitud a la URL del planeta y verificar la respuesta
    private Response requestPlanetAndVerify(String planetUrl) {
        Response planetResponse = given().get(planetUrl);

        // Verificar la respuesta exitosa (código de estado 200)
        assertEquals(planetResponse.getStatusCode(), 200, "Código de estado inesperado");

        // Verificar que la gravedad coincida con el valor esperado
        String gravity = planetResponse.jsonPath().getString("gravity");
        assertEquals(gravity, gravityFixture, "La gravedad no coincide con el valor esperado");

        // Verificar que los terrains coincidan con los valores esperados
        String terrain = planetResponse.jsonPath().getString("terrain");

        // Verificar si el campo terrain no es nulo antes de procesar más
        if (terrain != null) {
            // Verificar que terrain coincida con el valor esperado
            assertEquals(terrain, terrainFixture, "Terrain no coincide con el valor esperado");
        } else {
            // Manejar el caso en que el campo terrain sea nulo (agregar el manejo de errores o el registro apropiado)
            fail("El campo terrain es nulo en la respuesta");
        }

        return planetResponse;
    }

    // Prueba 1 (funciona)
    @Test(groups = "group1")
    public void testGetPeopleDetails() {
        RestAssured.baseURI = "https://swapi.dev/api/";

        Response response = given().get("people/2/");

        assertEquals(response.getStatusCode(), 200, "Código de estado inesperado");
        String skinColor = response.jsonPath().getString("skin_color");
        assertEquals(skinColor, "gold", "Color de piel inesperado");

        int filmsCount = response.jsonPath().getList("films").size();
        assertEquals(filmsCount, 6, "Número de películas inesperado");
    }

    // Prueba 2 (funciona)
    @Test(dependsOnGroups = "group1")
    public void testSecondMovieDetails() {
        RestAssured.baseURI = "https://swapi.dev/api/";

        Response response = given().get("people/2/");
        String movieUrl = response.jsonPath().getString("films[1]");

        movieResponse = given().get(movieUrl);

        assertEquals(movieResponse.getStatusCode(), 200, "Código de estado inesperado");
        String releaseDate = movieResponse.jsonPath().getString("release_date");

        assertElementCountGreaterThanOne(movieResponse, "characters");
        assertElementCountGreaterThanOne(movieResponse, "planets");
        assertElementCountGreaterThanOne(movieResponse, "starships");
        assertElementCountGreaterThanOne(movieResponse, "vehicles");
        assertElementCountGreaterThanOne(movieResponse, "species");
    }

    // Prueba 3 (funciona)
    @Test(dependsOnGroups = "group1")
    public void testFirstPlanetInLastFilm() {
        RestAssured.baseURI = "https://swapi.dev/api/";

        Response peopleResponse = given().get("people/2/");
        String movieUrl = peopleResponse.jsonPath().getString("films[1]");

        movieResponse = given().get(movieUrl);

        assertEquals(movieResponse.getStatusCode(), 200, "Código de estado inesperado");

        String planetUrl = movieResponse.jsonPath().getString("planets[0]");
        Response planetResponse = requestPlanetAndVerify(planetUrl);
    }

    // Prueba 4 (funciona)
    @Test(dependsOnGroups = "group1")
    public void testPlanetUrlVerification() {
        RestAssured.baseURI = "https://swapi.dev/api/";

        Response peopleResponse = given().get("people/2/");
        String movieUrl = peopleResponse.jsonPath().getString("films[1]");

        movieResponse = given().get(movieUrl);

        assertEquals(movieResponse.getStatusCode(), 200, "Código de estado inesperado");

        String planetUrl = movieResponse.jsonPath().getString("planets[0]");
        Response planetResponse = requestPlanetAndVerify(planetUrl);

        // Extraer la URL del planeta de la respuesta del planeta
        String planetUrlFromResponse = planetResponse.jsonPath().getString("url");

        // Hacer una solicitud a la URL del planeta extraída
        Response planetUrlResponse = given().get(planetUrlFromResponse);

        // Verificar la respuesta exitosa (código de estado 200)
        assertEquals(planetUrlResponse.getStatusCode(), 200, "Código de estado inesperado");

        // Validar que la respuesta de la URL del planeta extraída sea exactamente la misma que la anterior
        assertEquals(planetUrlResponse.getBody().asString(), planetResponse.getBody().asString(),
                "Las respuestas de las dos solicitudes no son iguales");
    }

    // Prueba 5 (funciona)
    @Test
    public void testFilmWith404() {
        RestAssured.baseURI = "https://swapi.dev/api/";

        Response response = given().get("/films/7/");

        assertEquals(response.getStatusCode(), 404, "Código de estado inesperado");
    }
}
