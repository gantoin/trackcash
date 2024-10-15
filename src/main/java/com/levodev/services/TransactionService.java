package com.levodev.services;

import com.levodev.data.domain.Transaction;
import com.levodev.data.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Optional<Transaction> get(Long id) {
        return repository.findById(id);
    }

    public Transaction update(Transaction entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Transaction> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Transaction> list(Pageable pageable, Specification<Transaction> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
