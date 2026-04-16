package livres

import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.springframework.boot.test.web.server.LocalServerPort
import io.kotest.matchers.shouldBe
import io.restassured.path.json.JsonPath

class BookStepDefs {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var lastResponse: ValidatableResponse

    @Before
    fun setup() {
        // On configure les propriétés statiques séparément
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Given("l'utilisateur crée le livre {string} écrit par {string}")
    fun createBook(titre: String, auteur: String) {
        given()
            .contentType(ContentType.JSON)
            .body("""{"titre": "$titre", "auteur": "$auteur"}""")
            .`when`()
            .post("/books") // RestAssured utilisera baseURI + port + /books
            .then()
            .statusCode(201)
    }

    @When("l'utilisateur récupère la liste des livres")
    fun getAllBooks() {
        lastResponse = given()
            .`when`()
            .get("/books")
            .then()
            .statusCode(200)
    }

    @Then("la liste doit contenir le livre suivant")
    fun shouldContainBook(payload: List<Map<String, String>>) {
        val expectedTitre = payload[0]["titre"]
        val expectedAuteur = payload[0]["auteur"]

        val body = lastResponse.extract().body().asString()
        // Utilisation de GPath pour chercher dans la liste si le livre existe
        JsonPath(body).getString("find { it.titre == '$expectedTitre' }.titre") shouldBe expectedTitre
        JsonPath(body).getString("find { it.titre == '$expectedTitre' }.auteur") shouldBe expectedAuteur
    }
}