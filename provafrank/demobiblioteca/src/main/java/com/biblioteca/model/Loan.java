package com.biblioteca.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "loans")
public class Loan implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false) @JoinColumn(name="book_id")
    private Book book;

    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate returnDate; // null se não devolvido

    public Loan() {}
    public Loan(User user, Book book, LocalDate loanDate, LocalDate expectedReturnDate) {
        this.user = user; this.book = book; this.loanDate = loanDate; this.expectedReturnDate = expectedReturnDate;
    }

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public boolean isReturned() { return returnDate != null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        Loan loan = (Loan) o;
        return Objects.equals(getId(), loan.getId());
    }
    @Override
    public int hashCode() { return Objects.hash(getId()); }
}
