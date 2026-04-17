package books.infrastructure

import com.example.demo.DemoApplication  // <--- VÉRIFIE BIEN CET IMPORT
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import books.domain.model.Book
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [DemoApplication::class])
@ActiveProfiles("testIntegration")
class PostgresBookRepositoryIT : FunSpec() {

    @Autowired
    private lateinit var repository: PostgresBookRepository

    init {
        extension(SpringExtension)

        test("sauvegarder doit réellement insérer un livre en BDD") {
            val book = Book("Le Chuchoteur", "Donato Carrisi", false)
            repository.saveBook(book)
            val books = repository.getBooks()
            books.any { it.title == "Le Chuchoteur" } shouldBe true
        }
    }

    companion object {
        init {
            System.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/postgres")
            System.setProperty("spring.datasource.username", "postgres")
            System.setProperty("spring.datasource.password", "password")
            System.setProperty("spring.liquibase.enabled", "true")
        }
    }
}