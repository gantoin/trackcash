package com.levodev.data.repository;


import com.levodev.data.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository
        extends
        JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

}
