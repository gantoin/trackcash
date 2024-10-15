package com.levodev.services;

import com.levodev.data.BudgetTag;
import com.levodev.data.BudgetTagRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class BudgetTagService {

    private final BudgetTagRepository repository;

    public BudgetTagService(BudgetTagRepository repository) {
        this.repository = repository;
    }

    public Optional<BudgetTag> get(Long id) {
        return repository.findById(id);
    }

    public BudgetTag update(BudgetTag entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<BudgetTag> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<BudgetTag> list(Pageable pageable, Specification<BudgetTag> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
