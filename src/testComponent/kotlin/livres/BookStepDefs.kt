package livres

import com.example.demo.DemoApplication
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.ValidatableResponse
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@CucumberContextConfiguration
@SpringBootTest(
    classes = [DemoApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BookStepDefs { // <-- LE NOM DOIT ÊTRE BookStepDefs

    @LocalServerPort
    private var port: Int = 0

    private lateinit var lastResponse: ValidatableResponse

    private fun url(path: String) = "http://localhost:$port$path"

    @Before
    fun setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Given("l'utilisateur crée le livre {string} écrit par {string}")
    fun createBook(titre: String, auteur: String) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""{"titre": "$titre", "auteur": "$auteur"}""")
            .`when`()
            .post(url("/books"))
            .then()
            .statusCode(201)
    }

    @When("l'utilisateur récupère la liste des livres")
    fun getAllBooks() {
        lastResponse = RestAssured.given()
            .`when`()
            .get(url("/books"))
            .then()
            .statusCode(200)
    }

    @Then("la liste doit contenir le livre suivant")
    fun shouldContainBook(payload: List<Map<String, String>>) {
        val expectedTitre = payload[0]["titre"]
        val expectedAuteur = payload[0]["auteur"]

        val books: List<Map<String, String>> = lastResponse.extract().body().jsonPath().getList("")

        // On cherche manuellement dans la liste pour éviter les erreurs de syntaxe JsonPath
        val livreTrouve = books.find { it["titre"] == expectedTitre }

        livreTrouve shouldNotBe null
        livreTrouve!!["titre"] shouldBe expectedTitre
        livreTrouve["auteur"] shouldBe expectedAuteur
    }
}