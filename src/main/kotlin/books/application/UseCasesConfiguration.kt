package books.application

import books.domain.port.BookRepository
import books.domain.usecase.BookManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCasesConfiguration {

    @Bean
    fun bookManager(bookRepository: BookRepository): BookManager {
        return BookManager(bookRepository)
    }
}