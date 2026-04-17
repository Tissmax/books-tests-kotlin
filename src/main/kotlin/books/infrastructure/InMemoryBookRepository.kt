package books.infrastructure

import books.domain.model.Book
import books.domain.port.BookRepository

class InMemoryBookRepository : BookRepository {
    private val storage = mutableListOf<Book>()

    override fun saveBook(book: Book) {
        storage.add(book)
    }

    override fun getBooks(): List<Book> = storage

    override fun getBookByTitle(title: String): Book? {
        return storage.firstOrNull { it.title == title }
    }

    override fun updateBook(book: Book): Book {
        storage.removeIf { it.title == book.title }
        storage.add(book)
        return book
    }

}