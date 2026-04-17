package books.domain.port

import books.domain.model.Book

interface BookRepository {
    fun saveBook(book: Book)
    fun getBooks(): List<Book>
    fun getBookByTitle(title: String): Book?
    fun updateBook(book: Book): Book
}