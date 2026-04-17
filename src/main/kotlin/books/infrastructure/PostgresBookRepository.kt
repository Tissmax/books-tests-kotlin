package books.infrastructure

import books.domain.model.Book
import books.domain.port.BookRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class PostgresBookRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) : BookRepository {

    override fun getBooks(): List<Book> {
        val sql = "SELECT title, author, reserved FROM livres"
        return jdbcTemplate.query(sql, MapSqlParameterSource()) { rs, _ ->
            Book(
                title = rs.getString("title"),
                author = rs.getString("author"),
                reserved = rs.getBoolean("reserved"),
            )
        }
    }

    override fun getBookByTitle(title: String): Book? {
        val sql = "SELECT * FROM livres WHERE title = :title"
        val params = MapSqlParameterSource("title", title)
        return jdbcTemplate.query(sql, params) { rs, _ ->
            Book(
                title = rs.getString("title"),
                author = rs.getString("author"),
                reserved = rs.getBoolean("reserved"),
            )
        }.firstOrNull()
    }

    override fun updateBook(book: Book): Book {
        val sql = "UPDATE livres SET reserved = :reserved WHERE title = :title"
        val params = mapOf(
            "title" to book.title,
            "author" to book.author,
            "reserved" to book.reserved
        )
        jdbcTemplate.update(sql, params)
        return book
    }

    override fun saveBook(book: Book) {
        val sql = "INSERT INTO livres (title, author, reserved) VALUES (:title, :author, :reserved)"
        val params = mapOf(
            "title" to book.title,
            "author" to book.author,
            "reserved" to book.reserved
        )
        jdbcTemplate.update(sql, params)
    }
}