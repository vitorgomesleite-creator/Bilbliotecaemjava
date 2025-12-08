package com.biblioteca.repository;

import com.biblioteca.model.Book;
import java.util.List;

public class BookRepository extends GenericRepository<Book, Long> {
    public BookRepository() {
        super(Book.class);
    }

    public List<Book> findAvailable() {
        return run(session ->
                session.createQuery("from Book b where b.quantity > 0", Book.class)
                        .list()
        );
    }
}
