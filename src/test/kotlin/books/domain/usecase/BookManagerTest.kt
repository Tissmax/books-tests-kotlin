package books.domain.usecase

import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder 
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb 
import io.kotest.property.arbitrary.list 
import io.kotest.property.arbitrary.map 
import io.kotest.property.arbitrary.string 
import io.kotest.property.checkAll 
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import books.domain.model.Book
import books.domain.port.BookRepository
import io.mockk.clearMocks
import io.mockk.confirmVerified

class BookManagerTest : FunSpec({

    val repository = mockk<BookRepository>(relaxed = true)
    val useCase = BookManager(repository)

    test("Un livre doit être ajouté avec succès si les données sont valides") {
        val book = Book("Le Petit Prince", "Saint-Exupéry", false)
        useCase.addBook(book)
        verify { repository.saveBook(book) }
    }

    test("L'ajout d'un livre doit échouer si le titre est vide") {
        val livreSansTitre = Book("", "Saint-Exupéry", false)
        shouldThrowMessage("Le titre et l'auteur ne peuvent pas être vides") {
            useCase.addBook(livreSansTitre)
        }
    }

    test("L'ajout d'un livre doit échouer si l'auteur est vide") {
        val livreSansAuteur = Book("Le Petit Prince", "  ", false)
        shouldThrowMessage("Le titre et l'auteur ne peuvent pas être vides") {
            useCase.addBook(livreSansAuteur)
        }
    }

    test("La liste doit être retournée triée par titre par ordre alphabétique") {
        val livreA = Book("Antigone", "Anouilh", false)
        val livreM = Book("Miserables", "Hugo", false)
        val livreZ = Book("Zola", "Emile", false)

        every { repository.getBooks() } returns listOf(livreZ, livreA, livreM)

        val resultat = useCase.getBooksOrderedByTitle()

        resultat[0].title shouldBe "Antigone"
        resultat[1].title shouldBe "Miserables"
        resultat[2].title shouldBe "Zola"
    }

    test("La liste des livres retournés contient tous les éléments de la liste stockée") {
        val arbLivre = Arb.string(minSize = 1, maxSize = 20).map { titre ->
            Book(titre, "Auteur Aléatoire", false)
        }

        checkAll(Arb.list(arbLivre, 1..50)) { listeGeneree ->
            every { repository.getBooks() } returns listeGeneree

            val resultat = useCase.getBooksOrderedByTitle()

            resultat.size shouldBe listeGeneree.size
            resultat shouldContainExactlyInAnyOrder listeGeneree
        }
    }

    test("La réservation d'un livre non réservé doit mettre à jour le statut et sauvegarder") {
        // 1. Arrange : On crée une instance propre
        val book = Book("L'Étranger", "Camus", false)
        every { repository.updateBook(any()) } answers { firstArg() }

        // 2. Act
        val result = useCase.reserveBook(book.title)

        // 3. Assert
        result.reserved shouldBe true
        verify(exactly = 1) { repository.updateBook(any()) }
    }

    test("Réserver un livre déjà réservé ne doit pas appeler le repository") {
        // 1. Arrange : Le livre est DÉJÀ réservé
        val bookDejaReserve = Book("L'Étranger", "Camus", true)

        // On nettoie les appels précédents au cas où le mock est partagé
        confirmVerified(repository)

        // 2. Act
        val result = useCase.reserveBook(bookDejaReserve.title)

        // 3. Assert
        result.reserved shouldBe true
        // On vérifie spécifiquement qu'updateBook n'a pas été appelé pour CE test
        verify(exactly = 0) { repository.updateBook(any()) }
    }

    test("Le retour d'un livre réservé doit changer son statut à false et sauvegarder") {
        // Arrange
        val book = Book("1984", "George Orwell", true)
        every { repository.updateBook(any()) } answers { firstArg() }

        // Act
        val result = useCase.returnBook(book.title)

        // Assert
        result.reserved shouldBe false
        verify(exactly = 1) {
            repository.updateBook(withArg {
                it.reserved shouldBe false
                it.title shouldBe "1984"
            })
        }
    }

    test("Le retour d'un livre non réservé ne doit pas déclencher de mise à jour") {
        // Arrange
        val bookNonReserve = Book("Le Meilleur des mondes", "Aldous Huxley", false)

        // Crucial : On s'assure que ce test ne voit pas les appels des tests précédents
        clearMocks(repository)

        // Act
        val result = useCase.returnBook(bookNonReserve.title)

        // Assert
        result.reserved shouldBe false
        verify(exactly = 0) { repository.updateBook(any()) }
    }
})