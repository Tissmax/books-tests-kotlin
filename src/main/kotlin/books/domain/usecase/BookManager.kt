package books.domain.usecase

import books.domain.model.Book
import books.domain.port.BookRepository
import org.springframework.web.client.HttpClientErrorException

class BookManager(private val repository: BookRepository) {

    fun addBook(book: Book) {
        if (book.title.isBlank() || book.author.isBlank()) {
            throw IllegalArgumentException("Le titre et l'auteur ne peuvent pas être vides")
        }
        repository.saveBook(book)
    }
    fun getBooksOrderedByTitle(): List<Book> {
        return repository.getBooks().sortedBy { it.title.lowercase() }
    }
    fun reserveBook(title: String): Book {
        val book = this.getBookByTitle(title)
        if (book.reserved) return book
        book.reserved = true
        return repository.updateBook(book)
    }
    fun returnBook(title: String): Book {
        val book = this.getBookByTitle(title)
        if (!book.reserved) return book
        book.reserved = false
        return repository.updateBook(book)
    }

    fun getBookByTitle(title: String): Book {
        val book: Book? = repository.getBookByTitle(title)

        if (book != null) {
            return book
        } else {
            throw IllegalArgumentException("Livre introuvable : $title")
        }
    }

}