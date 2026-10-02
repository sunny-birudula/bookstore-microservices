package com.example.userservice.service;

import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    public User create(User user) {
        user.setId(null);
        return repository.save(user);
    }

    @Transactional
    public User update(Long id, User input) {
        User existing = getById(id);
        existing.setName(input.getName());
        existing.setEmail(input.getEmail());
        existing.setPhone(input.getPhone());
        existing.setAddress(input.getAddress());
        return repository.save(existing);
    }

    public void delete(Long id) {
        User existing = getById(id);
        repository.delete(existing);
    }
}
