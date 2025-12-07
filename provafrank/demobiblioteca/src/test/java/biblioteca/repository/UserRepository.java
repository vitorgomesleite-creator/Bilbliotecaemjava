package com.biblioteca.repository;

import com.biblioteca.model.User;

public class UserRepository extends GenericRepository<User, Long> {
    public UserRepository() { super(User.class); }
}
