package books.driving.controller.dto

import books.domain.model.Book

data class BookDTO(val title: String, val author: String, val reserved: Boolean) {
    fun toDomain() = Book(title, author, reserved)

    companion object {
        fun fromDomain(l: Book) = BookDTO(l.title, l.author, l.reserved)
    }
}