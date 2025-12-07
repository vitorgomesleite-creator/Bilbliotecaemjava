package com.biblioteca.repository;

import com.biblioteca.model.Loan;
import com.biblioteca.model.User;
import com.biblioteca.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class LoanRepository extends GenericRepository<Loan, Long> {
    public LoanRepository() { super(Loan.class); }

    public List<Loan> findOpenLoansByUser(User user) {
        return run(session ->
                session.createQuery("from Loan l where l.user = :u and l.returnDate is null", Loan.class)
                        .setParameter("u", user)
                        .list()
        );
    }

    public List<Loan> findAllOverdue(LocalDate date) {
        return run(session ->
                session.createQuery("from Loan l where l.returnDate is null and l.expectedReturnDate < :d", Loan.class)
                        .setParameter("d", date)
                        .list()
        );
    }
}
