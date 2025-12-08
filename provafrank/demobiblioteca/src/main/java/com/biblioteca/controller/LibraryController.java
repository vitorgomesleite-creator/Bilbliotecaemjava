package com.biblioteca.controller;

import com.biblioteca.model.Book;
import com.biblioteca.model.Loan;
import com.biblioteca.model.User;
import com.biblioteca.repository.BookRepository;
import com.biblioteca.repository.LoanRepository;
import com.biblioteca.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LibraryController {
    private final BookRepository bookRepo = new BookRepository();
    private final UserRepository userRepo = new UserRepository();
    private final LoanRepository loanRepo = new LoanRepository();

    private final int MAX_LOANS_PER_USER = 5;
    private final int MAX_LOAN_DAYS = 14;
    private final double FINE_PER_DAY = 1.5; // exemplo

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Book CRUD
    public void saveBook(Book b) { bookRepo.save(b); }
    public void deleteBook(Book b) { bookRepo.delete(b); }
    public List<Book> listBooks() { return bookRepo.findAll(); }
    public List<Book> listAvailableBooks() { return bookRepo.findAvailable(); }
    public Book findBook(Long id) { return bookRepo.findById(id); }

    // User CRUD
    public void saveUser(User u) { userRepo.save(u); }
    public void deleteUser(User u) { userRepo.delete(u); }
    public List<User> listUsers() { return userRepo.findAll(); }
    public User findUser(Long id) { return userRepo.findById(id); }

    // Loans
    public List<Loan> listLoans() { return loanRepo.findAll(); }
    public List<Loan> listOpenLoansByUser(User u) { return loanRepo.findOpenLoansByUser(u); }

    public String loanBook(User user, Book book, LocalDate loanDate, int quantity) {
        if (user == null || book == null) return "Usuário ou livro inválido.";
        if (quantity <= 0) return "Quantidade deve ser maior que 0.";
        
        // Recarregar do banco para evitar detached entity
        user = userRepo.findById(user.getId());
        book = bookRepo.findById(book.getId());
        if (user == null || book == null) return "Usuário ou livro não encontrado no banco.";
        
        List<Loan> open = loanRepo.findOpenLoansByUser(user);
        System.out.println("DEBUG: User " + user.getId() + " tem " + open.size() + " empréstimos abertos. MAX=" + MAX_LOANS_PER_USER);
        
        if (open.size() >= MAX_LOANS_PER_USER) {
            return "Usuário já possui " + open.size() + " empréstimos (máx " + MAX_LOANS_PER_USER + ").";
        }
        if (book.getQuantity() < quantity) return "Apenas " + book.getQuantity() + " exemplares disponíveis (tentou: " + quantity + ").";

        LocalDate expected = loanDate.plusDays(MAX_LOAN_DAYS);
        
        // Criar UM empréstimo com a quantidade
        Loan loan = new Loan(user, book, loanDate, expected, quantity);
        loanRepo.save(loan);

        // decrementar quantidade total
        book.setQuantity(book.getQuantity() - quantity);
        bookRepo.save(book);
        return "Empréstimo registrado: " + quantity + " exemplar(es). Devolução prevista: " + expected.format(formatter);
    }

    public String returnBook(Loan loan, LocalDate returnDate) {
        if (loan == null) return "Empréstimo inválido.";
        if (loan.isReturned()) return "Empréstimo já devolvido.";

        loan.setReturnDate(returnDate);
        loanRepo.save(loan);

        // aumentar quantidade
        Book book = loan.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepo.save(book);

        // calcular multa se atrasado
        if (returnDate.isAfter(loan.getExpectedReturnDate())) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), returnDate);
            double fine = days * FINE_PER_DAY;
            return "Devolvido com atraso de " + days + " dias. Multa: R$ " + String.format("%.2f", fine);
        } else {
            return "Devolução registrada dentro do prazo.";
        }
    }

    public List<Loan> listOverdue(LocalDate today) { return loanRepo.findAllOverdue(today); }
}
