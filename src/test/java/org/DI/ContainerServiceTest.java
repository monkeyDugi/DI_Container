package org.DI;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ContainerServiceTest {

    @Test
    public void getObject_BookRepository() {
        Class<BookRepository> bookRepositoryClass = BookRepository.class;
        BookRepository bookRepository = ContainerService.getObject(bookRepositoryClass);
        assertNotNull(bookRepository);
    }

    @Test
    public void getObject_BookService() {
        Class<BookService> bookServiceClass = BookService.class;
        BookService bookService = ContainerService.getObject(bookServiceClass);
        assertNotNull(bookService);
        assertNotNull(bookService.getBookRepository());
    }
}