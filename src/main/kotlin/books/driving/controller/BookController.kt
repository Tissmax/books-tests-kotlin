package books.driving.controller

import books.domain.usecase.BookManager
import books.driving.controller.dto.BookDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val bookManager: BookManager) {

    @GetMapping
    fun getBooks(): List<BookDTO> {
        return bookManager.getBooksOrderedByTitle().map { BookDTO.fromDomain(it) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@RequestBody book: BookDTO) {
        bookManager.addBook(book.toDomain())
    }

    @PatchMapping("/reserve")
    @ResponseStatus(HttpStatus.OK)
    fun reserveBook(@RequestParam title: String): BookDTO {
        return BookDTO.fromDomain(bookManager.reserveBook(title))
    }

    @PatchMapping("/return")
    @ResponseStatus(HttpStatus.OK)
    fun returnBook(@RequestParam title: String): BookDTO {
        return BookDTO.fromDomain(bookManager.returnBook(title))
    }

}