package books

import books.domain.model.Book
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
import io.restassured.parsing.Parser

@CucumberContextConfiguration
@SpringBootTest(
    classes = [DemoApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class BookStepDefs {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var lastResponse: ValidatableResponse

    private fun url(path: String) = "http://localhost:$port$path"

    @Before
    fun setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
    }

    @Given("l'utilisateur crée le livre {string} écrit par {string}")
    fun createBook(title: String, author: String) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""{"title": "$title", "author": "$author", "reserved": false}""")
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
        val expectedTitle = payload[0]["title"]
        val expectedAuthor = payload[0]["author"]

        val books: List<Map<String, String>> = lastResponse.extract().body().jsonPath().getList("")

        val livreTrouve = books.find { it["title"] == expectedTitle }

        livreTrouve shouldNotBe null
        livreTrouve!!["title"] shouldBe expectedTitle
        livreTrouve["author"] shouldBe expectedAuthor
    }

    @Given("l'utilisateur reserve le livre {string} écrit par {string}")
    fun bookReservationSetup(title: String, author: String) {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("""{"title": "$title", "author": "$author", "reserved": false}""")
            .`when`()
            .post(url("/books"))
            .then()
            .statusCode(201)
    }

    @When("l'utilisateur réserve le livre {string}")
    fun bookReservation(title: String) {
        lastResponse = RestAssured.given()
            .`when`()
            .contentType(ContentType.JSON)
            .queryParam("title", title)
            .patch(url("/books/reserve"))
            .then()
            .statusCode(200)
    }

    @Then("le livre doit etre reserve")
    fun bookReserved(payload: List<Map<String, String>>) {
        // On récupère les valeurs attendues depuis le tableau Cucumber
        val expectedTitle = payload[0]["title"]
        val expectedAuthor = payload[0]["author"]

        // On extrait l'objet Book renvoyé par ton API
        val book: Book = lastResponse.extract().body().`as`(Book::class.java)

        book shouldNotBe null
        book.title shouldBe expectedTitle
        book.author shouldBe expectedAuthor
        book.reserved shouldBe true
    }

    @When("l'utilisateur retourne le livre {string}")
    fun bookReturn(title: String) {
        lastResponse = RestAssured.given()
            .`when`()
            .contentType(ContentType.JSON)
            .queryParam("title", title)
            .patch(url("/books/return"))
            .then()
            .statusCode(200)
    }

    @Then("le livre doit etre retourne")
    fun bookReturned(payload: List<Map<String, String>>) {
        val expectedTitle = payload[0]["title"]
        val expectedAuthor = payload[0]["author"]

        val book: Book = lastResponse.extract().body().`as`(Book::class.java)

        book shouldNotBe null
        book.title shouldBe expectedTitle
        book.author shouldBe expectedAuthor
        book.reserved shouldBe false
    }
}