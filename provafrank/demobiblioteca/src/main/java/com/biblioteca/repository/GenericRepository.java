package com.biblioteca.repository;

import com.biblioteca.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public abstract class GenericRepository<T, ID extends Serializable> {
    private final Class<T> clazz;

    public GenericRepository(Class<T> clazz) { this.clazz = clazz; }

    protected <R> R run(Function<Session, R> function) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        try {
            return function.apply(s);
        } finally {
            s.close();
        }
    }

    public void save(T entity) {
        run(session -> {
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(entity);
            tx.commit();
            return null;
        });
    }

    public void delete(T entity) {
        run(session -> {
            Transaction tx = session.beginTransaction();
            session.delete(entity);
            tx.commit();
            return null;
        });
    }

    public T findById(ID id) {
        return run(session -> session.get(clazz, id));
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return run(session -> session.createQuery("from " + clazz.getSimpleName()).list());
    }
}
