package com.biblioteca.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "books")
public class Book implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String title;
    private String theme;
    private String author;
    @Column(unique=true) private String isbn;
    private String publicationDate; // dd/MM/yyyy stored as String for simplicity
    private int quantity;

    public Book() {}

    public Book(String title, String theme, String author, String isbn, String publicationDate, int quantity) {
        this.title = title;
        this.theme = theme;
        this.author = author;
        this.isbn = isbn;
        this.publicationDate = publicationDate;
        this.quantity = quantity;
    }

    // getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getPublicationDate() { return publicationDate; }
    public void setPublicationDate(String publicationDate) { this.publicationDate = publicationDate; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(getId(), book.getId());
    }
    @Override
    public int hashCode() { return Objects.hash(getId()); }
}
