package org.DI;

public class BookService {

    @InjectDugi
    private BookRepository bookRepository;

    public BookRepository getBookRepository() {
        return bookRepository;
    }
}
