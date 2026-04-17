package books.driving.controller

import com.example.demo.DemoApplication // <-- AJOUTE CET IMPORT
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.verify
import books.domain.model.Book
import books.domain.usecase.BookManager
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [BookController::class], properties = ["spring.main.allow-bean-definition-overriding=true"])
@org.springframework.test.context.ContextConfiguration(classes = [DemoApplication::class])
class BookControllerTest : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var bookManager: BookManager



    init {
        it("GET /books doit retourner la liste en JSON") {
            val book = Book("Le Hobbit", "Tolkien", false)
            every { bookManager.getBooksOrderedByTitle() } returns listOf(book)

            mockMvc.perform(get("/books"))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Le Hobbit"))
        }

        it("POST /books doit créer un livre et retourner 201") {
            val json = """ { "title": "1984", "author": "Orwell", "reserved": false } """
            every { bookManager.addBook(any()) } returns Unit

            mockMvc.perform(
                post("/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
                .andExpect(status().isCreated)

            verify { bookManager.addBook(any()) }
        }

        it("PATCH /books/reserve doit modifier le statut reservé du livre à true") {
            // 1. Préparation des données
            val title = "1984"
            val reservedBook = Book(title, "Orwell", true)

            // 2. Configuration du Mock : On définit ce que reserveBook doit retourner
            every { bookManager.reserveBook(title) } returns reservedBook

            // 3. Exécution de l'appel
            val responseContent = mockMvc.perform(
                patch("/books/reserve")
                    .param("title", title)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

            // 4. Transformation et Assertions
            val book = objectMapper.readValue(responseContent, Book::class.java)

            // Vérifie que le service a bien été appelé avec le bon titre
            verify { bookManager.reserveBook(title) }

            // Vérifie le contenu de la réponse (L'objet réel)
            book.title shouldBe title
            book.reserved shouldBe true
        }

        it("PATCH /books/return doit modifier le statut reservé du livre à false") {
            // 1. Préparation des données simulées
            val title = "1984"
            val returnedBook = Book(title, "Orwell", false) // Le livre que le service est censé renvoyer

            // 2. Configuration du Mock (Stubbing)
            // On dit au mock : "Quand on t'appelle pour rendre ce livre, renvoie l'objet avec reserved = false"
            every { bookManager.returnBook(title) } returns returnedBook

            // 3. Exécution de l'appel PATCH
            val responseContent = mockMvc.perform(
                patch("/books/return")
                    .param("title", title)
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isOk)
                .andReturn().response.contentAsString

            // 4. Désérialisation de la réponse
            val book = objectMapper.readValue(responseContent, Book::class.java)

            // 5. Assertions et Vérifications
            verify { bookManager.returnBook(title) } // Vérifie que le service a bien été sollicité

            // Assertion Kotest (ou JUnit) pour vérifier l'état de l'objet reçu
            book.reserved shouldBe false
            book.title shouldBe title
        }
    }
}