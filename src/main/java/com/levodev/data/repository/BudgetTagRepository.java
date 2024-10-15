package com.levodev.data.repository;


import com.levodev.data.domain.BudgetTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BudgetTagRepository extends JpaRepository<BudgetTag, Long>, JpaSpecificationExecutor<BudgetTag> {

}
